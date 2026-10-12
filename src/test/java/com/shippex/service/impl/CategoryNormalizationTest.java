package com.shippex.service.impl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CategoryNormalizationTest {
    @Test void slugNormalizesSpacesCapitalizationAndDiacritics() {
        assertEquals("integration-test-chocolates", CategoryServiceImpl.slugify("  Integration Test Chocolates "));
        assertEquals("creme-brulee", CategoryServiceImpl.slugify("Crème brûlée"));
    }
}
