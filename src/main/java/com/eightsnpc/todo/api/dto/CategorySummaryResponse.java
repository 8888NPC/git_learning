package com.eightsnpc.todo.api.dto;

import com.eightsnpc.todo.finance.CategorySummary;
import java.math.BigDecimal;

public record CategorySummaryResponse(
        long categoryId, String categoryName, String type, BigDecimal total, long count) {
    public static CategorySummaryResponse from(CategorySummary category) {
        return new CategorySummaryResponse(category.categoryId(), category.categoryName(),
                category.type().name(), category.total(), category.count());
    }
}
