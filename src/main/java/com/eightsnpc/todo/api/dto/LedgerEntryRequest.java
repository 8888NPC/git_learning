package com.eightsnpc.todo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerEntryRequest(
        @NotNull(message = "分类不能为空") @Positive(message = "分类编号必须为正数") Long categoryId,
        @NotNull(message = "金额不能为空")
                @DecimalMin(value = "0.00", inclusive = false, message = "金额必须大于 0")
                @Digits(integer = 10, fraction = 2, message = "金额最多 10 位整数和 2 位小数") BigDecimal amount,
        @NotNull(message = "发生日期不能为空") LocalDate occurredOn,
        @Size(max = 255, message = "备注不能超过 255 个字符") String note) {
}
