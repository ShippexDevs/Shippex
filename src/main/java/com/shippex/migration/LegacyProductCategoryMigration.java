package com.shippex.migration;

import com.shippex.model.Category;
import com.shippex.model.Product;
import com.shippex.repository.CategoryRepository;
import com.shippex.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

/** Opt-in, rerunnable repair tool. It only writes categoryId. */
@Component
@ConditionalOnProperty(name = "shippex.migration.category-links.enabled", havingValue = "true")
public class LegacyProductCategoryMigration implements ApplicationRunner {
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final MongoTemplate mongo;
    private final boolean apply;
    private final String aliases;

    public LegacyProductCategoryMigration(CategoryRepository categories, ProductRepository products,
            MongoTemplate mongo,
            @Value("${shippex.migration.category-links.apply:false}") boolean apply,
            @Value("${shippex.migration.category-links.aliases:}") String aliases) {
        this.categories = categories;
        this.products = products;
        this.mongo = mongo;
        this.apply = apply;
        this.aliases = aliases;
    }

    @Override public void run(ApplicationArguments args) {
        List<Category> allCategories = categories.findAll();
        Map<String, List<Category>> keys = new HashMap<>();
        for (Category category : allCategories) {
            add(keys, normalize(category.getName()), category);
            add(keys, normalize(category.getSlug()), category);
        }
        Map<String, String> aliasMap = parseAliases();
        int examined = 0, valid = 0, matched = 0, unmatched = 0, ambiguous = 0;
        for (Product product : products.findAll()) {
            examined++;
            if (product.getCategoryId() != null && categories.existsById(product.getCategoryId())) {
                valid++;
                continue;
            }
            String legacyName = normalize(product.getCategory());
            String legacySlug = normalize(product.getCategorySlug());
            String aliasTarget = aliasMap.get(legacyName);
            if (aliasTarget == null) aliasTarget = aliasMap.get(legacySlug);
            List<Category> candidates = aliasTarget == null ? union(keys.get(legacyName), keys.get(legacySlug))
                    : union(keys.get(normalize(aliasTarget)), keys.get(normalize(aliasTarget)));
            if (candidates.size() == 1) {
                Category category = candidates.get(0);
                matched++;
                System.out.printf("MATCH product=%s categoryId=%s legacyCategory=%s legacySlug=%s%n",
                        product.getId(), category.getId(), product.getCategory(), product.getCategorySlug());
                if (apply) {
                    mongo.updateFirst(Query.query(Criteria.where("_id").is(product.getId())),
                            new Update().set("categoryId", category.getId()), Product.class);
                }
            } else if (candidates.size() > 1) {
                ambiguous++;
                System.out.printf("AMBIGUOUS product=%s legacyCategory=%s legacySlug=%s candidates=%s%n",
                        product.getId(), product.getCategory(), product.getCategorySlug(),
                        candidates.stream().map(Category::getId).toList());
            } else {
                unmatched++;
                System.out.printf("UNMATCHED product=%s legacyCategory=%s legacySlug=%s%n",
                        product.getId(), product.getCategory(), product.getCategorySlug());
            }
        }
        System.out.printf("Category migration mode=%s examined=%d alreadyValid=%d matched=%d unmatched=%d ambiguous=%d%n",
                apply ? "APPLY" : "DRY_RUN", examined, valid, matched, unmatched, ambiguous);
    }

    private Map<String, String> parseAliases() {
        if (aliases == null || aliases.isBlank()) return Map.of();
        return Arrays.stream(aliases.split(","))
                .map(String::trim).filter(value -> value.contains("="))
                .map(value -> value.split("=", 2))
                .collect(Collectors.toMap(pair -> normalize(pair[0]), pair -> pair[1].trim(), (first, second) -> second));
    }

    private static void add(Map<String, List<Category>> index, String key, Category category) {
        if (!key.isBlank()) index.computeIfAbsent(key, ignored -> new ArrayList<>()).add(category);
    }
    private static List<Category> union(List<Category> first, List<Category> second) {
        Map<String, Category> unique = new LinkedHashMap<>();
        if (first != null) first.forEach(category -> unique.put(category.getId(), category));
        if (second != null) second.forEach(category -> unique.put(category.getId(), category));
        return new ArrayList<>(unique.values());
    }
    private static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }
}
