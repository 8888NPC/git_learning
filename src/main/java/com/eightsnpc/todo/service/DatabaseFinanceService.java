package com.eightsnpc.todo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eightsnpc.todo.PageResult;
import com.eightsnpc.todo.finance.CategorySummary;
import com.eightsnpc.todo.finance.EntryType;
import com.eightsnpc.todo.finance.FinanceService;
import com.eightsnpc.todo.finance.FinanceSummary;
import com.eightsnpc.todo.finance.LedgerEntry;
import com.eightsnpc.todo.finance.persistence.entity.FinanceCategoryEntity;
import com.eightsnpc.todo.finance.persistence.entity.LedgerEntryEntity;
import com.eightsnpc.todo.finance.persistence.mapper.FinanceCategoryMapper;
import com.eightsnpc.todo.finance.persistence.mapper.LedgerEntryMapper;
import com.eightsnpc.todo.finance.persistence.mapper.LedgerSummaryRow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseFinanceService implements FinanceService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private final FinanceCategoryMapper categoryMapper;
    private final LedgerEntryMapper entryMapper;

    public DatabaseFinanceService(FinanceCategoryMapper categoryMapper, LedgerEntryMapper entryMapper) {
        this.categoryMapper = categoryMapper;
        this.entryMapper = entryMapper;
    }

    @Override
    @Transactional
    public LedgerEntry create(long categoryId, BigDecimal amount, LocalDate occurredOn, String note) {
        FinanceCategoryEntity category = requireCategory(categoryId);
        validateEntry(amount, occurredOn);
        LedgerEntryEntity entity = new LedgerEntryEntity();
        entity.setCategoryId(categoryId);
        entity.setAmount(amount.setScale(2, RoundingMode.UNNECESSARY));
        entity.setOccurredOn(occurredOn);
        entity.setNote(normalizeNote(note));
        entryMapper.insert(entity);
        return toDomain(entity, category);
    }

    @Override
    public PageResult<LedgerEntry> page(
            long page, long size, LocalDate from, LocalDate to, Long categoryId, EntryType type) {
        validatePage(page, size);
        validateRange(from, to);
        if (categoryId != null) {
            FinanceCategoryEntity category = requireCategory(categoryId);
            if (type != null && !type.name().equals(category.getEntryType())) {
                throw new IllegalArgumentException("所选分类与收支类型不匹配");
            }
        }

        LambdaQueryWrapper<LedgerEntryEntity> query = new LambdaQueryWrapper<LedgerEntryEntity>()
                .ge(from != null, LedgerEntryEntity::getOccurredOn, from)
                .le(to != null, LedgerEntryEntity::getOccurredOn, to)
                .eq(categoryId != null, LedgerEntryEntity::getCategoryId, categoryId)
                .orderByDesc(LedgerEntryEntity::getOccurredOn)
                .orderByDesc(LedgerEntryEntity::getId);
        if (type != null) {
            List<Long> ids = categoryMapper.selectList(new LambdaQueryWrapper<FinanceCategoryEntity>()
                            .eq(FinanceCategoryEntity::getEntryType, type.name()))
                    .stream().map(FinanceCategoryEntity::getId).toList();
            if (ids.isEmpty()) {
                query.eq(LedgerEntryEntity::getCategoryId, -1L);
            } else {
                query.in(categoryId == null, LedgerEntryEntity::getCategoryId, ids);
            }
        }
        Page<LedgerEntryEntity> result = entryMapper.selectPage(new Page<>(page, size), query);
        Map<Long, FinanceCategoryEntity> categories = categoriesFor(result.getRecords());
        List<LedgerEntry> records = result.getRecords().stream()
                .map(entry -> toDomain(entry, categories.get(entry.getCategoryId())))
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public Optional<LedgerEntry> findById(long id) {
        LedgerEntryEntity entity = entryMapper.selectById(id);
        if (entity == null) return Optional.empty();
        return Optional.of(toDomain(entity, requireCategory(entity.getCategoryId())));
    }

    @Override
    @Transactional
    public Optional<LedgerEntry> update(
            long id, long categoryId, BigDecimal amount, LocalDate occurredOn, String note) {
        LedgerEntryEntity entity = entryMapper.selectById(id);
        if (entity == null) return Optional.empty();
        FinanceCategoryEntity category = requireCategory(categoryId);
        validateEntry(amount, occurredOn);
        entity.setCategoryId(categoryId);
        entity.setAmount(amount.setScale(2, RoundingMode.UNNECESSARY));
        entity.setOccurredOn(occurredOn);
        entity.setNote(normalizeNote(note));
        entryMapper.updateById(entity);
        return Optional.of(toDomain(entity, category));
    }

    @Override
    @Transactional
    public boolean delete(long id) {
        return entryMapper.deleteById(id) > 0;
    }

    @Override
    public FinanceSummary summarize(LocalDate from, LocalDate to) {
        validateRange(from, to);
        List<LedgerSummaryRow> rows = entryMapper.summarizeByCategory(from, to);
        List<CategorySummary> categories = rows.stream()
                .map(row -> new CategorySummary(row.getCategoryId(), row.getCategoryName(),
                        EntryType.valueOf(row.getEntryType()), money(row.getTotalAmount()), row.getEntryCount()))
                .toList();
        BigDecimal income = categories.stream().filter(c -> c.type() == EntryType.INCOME)
                .map(CategorySummary::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = categories.stream().filter(c -> c.type() == EntryType.EXPENSE)
                .map(CategorySummary::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FinanceSummary(money(income), money(expense), money(income.subtract(expense)), categories);
    }

    private FinanceCategoryEntity requireCategory(long id) {
        FinanceCategoryEntity category = categoryMapper.selectById(id);
        if (category == null) throw new IllegalArgumentException("不存在该收支分类");
        return category;
    }

    private Map<Long, FinanceCategoryEntity> categoriesFor(List<LedgerEntryEntity> entries) {
        List<Long> ids = entries.stream().map(LedgerEntryEntity::getCategoryId).distinct().toList();
        if (ids.isEmpty()) return Map.of();
        return categoryMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(FinanceCategoryEntity::getId, Function.identity()));
    }

    private static void validateEntry(BigDecimal amount, LocalDate occurredOn) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2) {
            throw new IllegalArgumentException("金额必须大于 0，且最多保留两位小数");
        }
        if (amount.precision() - amount.scale() > 10) {
            throw new IllegalArgumentException("金额整数部分不能超过 10 位");
        }
        if (occurredOn == null) throw new IllegalArgumentException("发生日期不能为空");
    }

    private static void validateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
    }

    private static void validatePage(long page, long size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("分页参数必须满足 page >= 1 且 size 在 1 到 100 之间");
        }
    }

    private static String normalizeNote(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }

    private static BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private static LedgerEntry toDomain(LedgerEntryEntity entity, FinanceCategoryEntity category) {
        return new LedgerEntry(entity.getId(), entity.getCategoryId(), category.getName(),
                EntryType.valueOf(category.getEntryType()), entity.getAmount(), entity.getOccurredOn(), entity.getNote());
    }
}
