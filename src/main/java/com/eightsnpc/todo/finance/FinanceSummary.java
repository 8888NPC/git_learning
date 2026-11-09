package com.eightsnpc.todo.finance;

import java.math.BigDecimal;
import java.util.List;

public record FinanceSummary(
        BigDecimal incomeTotal,
        BigDecimal expenseTotal,
        BigDecimal balance,
        List<CategorySummary> categories) {
}
