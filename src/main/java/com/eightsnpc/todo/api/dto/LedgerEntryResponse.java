package com.eightsnpc.todo.api.dto;

import com.eightsnpc.todo.finance.LedgerEntry;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerEntryResponse(
        long id, long categoryId, String categoryName, String type,
        BigDecimal amount, LocalDate occurredOn, String note) {
    public static LedgerEntryResponse from(LedgerEntry entry) {
        return new LedgerEntryResponse(entry.id(), entry.categoryId(), entry.categoryName(),
                entry.type().name(), entry.amount(), entry.occurredOn(), entry.note());
    }
}
