package com.eightsnpc.todo.finance.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eightsnpc.todo.finance.persistence.entity.LedgerEntryEntity;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LedgerEntryMapper extends BaseMapper<LedgerEntryEntity> {
    @Select("<script>SELECT c.id AS category_id, c.name AS category_name, c.entry_type AS entry_type, "
            + "COALESCE(SUM(e.amount), 0) AS total_amount, COUNT(e.id) AS entry_count "
            + "FROM finance_categories c LEFT JOIN ledger_entries e ON e.category_id = c.id "
            + "<if test='from != null'>AND e.occurred_on &gt;= #{from}</if> "
            + "<if test='to != null'>AND e.occurred_on &lt;= #{to}</if> "
            + "GROUP BY c.id, c.name, c.entry_type "
            + "ORDER BY c.entry_type, c.id</script>")
    List<LedgerSummaryRow> summarizeByCategory(
            @Param("from") LocalDate from, @Param("to") LocalDate to);
}
