package com.eightsnpc.todo.api.dto;

import com.eightsnpc.todo.finance.FinanceSummary;
import java.math.BigDecimal;
import java.util.List;

public record FinanceSummaryResponse(
        BigDecimal incomeTotal, BigDecimal expenseTotal, BigDecimal balance,
        List<CategorySummaryResponse> categories) {
    public static FinanceSummaryResponse from(FinanceSummary summary) {
        return new FinanceSummaryResponse(summary.incomeTotal(), summary.expenseTotal(), summary.balance(),
                summary.categories().stream().map(CategorySummaryResponse::from).toList());
    }
}
