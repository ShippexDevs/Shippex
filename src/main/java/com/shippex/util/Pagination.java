package com.shippex.util;

import java.util.List;

public final class Pagination {
    private Pagination() {
    }

    public static <T> List<T> slice(List<T> items, int offset, int limit) {
        if (offset >= items.size()) {
            return List.of();
        }
        return items.subList(offset, Math.min(items.size(), offset + limit));
    }
}
