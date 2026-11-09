package com.eightsnpc.todo.finance.persistence.mapper;

import java.math.BigDecimal;

public class LedgerSummaryRow {
    private Long categoryId;
    private String categoryName;
    private String entryType;
    private BigDecimal totalAmount;
    private Long entryCount;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Long getEntryCount() { return entryCount; }
    public void setEntryCount(Long entryCount) { this.entryCount = entryCount; }
}
