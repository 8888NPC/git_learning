package com.eightsnpc.todo.finance;

import java.math.BigDecimal;

public record CategorySummary(
        long categoryId,
        String categoryName,
        EntryType type,
        BigDecimal total,
        long count) {
}
