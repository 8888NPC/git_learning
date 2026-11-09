package com.eightsnpc.todo.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerEntry(
        long id,
        long categoryId,
        String categoryName,
        EntryType type,
        BigDecimal amount,
        LocalDate occurredOn,
        String note) {
}
