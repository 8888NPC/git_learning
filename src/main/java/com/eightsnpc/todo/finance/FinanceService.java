package com.eightsnpc.todo.finance;

import com.eightsnpc.todo.PageResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface FinanceService {
    LedgerEntry create(long categoryId, BigDecimal amount, LocalDate occurredOn, String note);

    PageResult<LedgerEntry> page(
            long page, long size, LocalDate from, LocalDate to, Long categoryId, EntryType type);

    Optional<LedgerEntry> findById(long id);

    Optional<LedgerEntry> update(
            long id, long categoryId, BigDecimal amount, LocalDate occurredOn, String note);

    boolean delete(long id);

    FinanceSummary summarize(LocalDate from, LocalDate to);
}
