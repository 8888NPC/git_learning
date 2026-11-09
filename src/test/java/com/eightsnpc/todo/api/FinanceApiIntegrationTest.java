package com.eightsnpc.todo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.eightsnpc.todo.finance.persistence.mapper.LedgerEntryMapper;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinanceApiIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private LedgerEntryMapper entryMapper;

    @BeforeEach
    void clearEntries() {
        entryMapper.delete(new QueryWrapper<>());
    }

    @Test
    void createEntryReturnsCreatedRecord() throws Exception {
        mockMvc.perform(post("/api/v1/ledger/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entry(5, "35.80", "2026-11-02", "午餐")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(
                        "/api/v1/ledger/entries/\\d+")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryName").value("饮食"))
                .andExpect(jsonPath("$.data.type").value("EXPENSE"))
                .andExpect(jsonPath("$.data.amount").value(35.80));
    }

    @Test
    void listEntriesSupportsDateCategoryTypeAndPaginationFilters() throws Exception {
        createDirect(5, "20.00", "2026-11-02", "午餐");
        createDirect(6, "8.50", "2026-11-03", "公交");
        createDirect(1, "500.00", "2026-11-03", "工资");

        mockMvc.perform(get("/api/v1/ledger/entries")
                        .param("page", "1").param("size", "1")
                        .param("from", "2026-11-03").param("to", "2026-11-03")
                        .param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].categoryName").value("交通"));
    }

    @Test
    void getEntryReturnsItsDetails() throws Exception {
        long id = createDirect(1, "5000.00", "2026-11-01", "月薪");
        mockMvc.perform(get("/api/v1/ledger/entries/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.type").value("INCOME"));
    }

    @Test
    void updateEntryChangesCategoryAmountDateAndNote() throws Exception {
        long id = createDirect(5, "20.00", "2026-11-01", "旧备注");
        mockMvc.perform(put("/api/v1/ledger/entries/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entry(6, "18.25", "2026-11-04", "地铁")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryName").value("交通"))
                .andExpect(jsonPath("$.data.amount").value(18.25))
                .andExpect(jsonPath("$.data.occurredOn").value("2026-11-04"))
                .andExpect(jsonPath("$.data.note").value("地铁"));
    }

    @Test
    void deleteEntryRemovesIt() throws Exception {
        long id = createDirect(8, "44.00", "2026-11-01", null);
        mockMvc.perform(delete("/api/v1/ledger/entries/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/v1/ledger/entries/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void summaryAggregatesIncomeExpenseAndCategoryTotals() throws Exception {
        createDirect(1, "3000.00", "2026-11-01", "工资");
        createDirect(2, "500.50", "2026-11-05", "奖金");
        createDirect(5, "100.25", "2026-11-03", "餐饮");
        createDirect(5, "20.00", "2026-10-30", "上月消费");

        mockMvc.perform(get("/api/v1/ledger/summary")
                        .param("from", "2026-11-01").param("to", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.incomeTotal").value(3500.50))
                .andExpect(jsonPath("$.data.expenseTotal").value(100.25))
                .andExpect(jsonPath("$.data.balance").value(3400.25))
                .andExpect(jsonPath("$.data.categories.length()").value(14));
    }

    @Test
    void zeroAndOverPrecisionAmountsReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/ledger/entries").contentType(MediaType.APPLICATION_JSON)
                        .content(entry(5, "0", "2026-11-01", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
        mockMvc.perform(post("/api/v1/ledger/entries").contentType(MediaType.APPLICATION_JSON)
                        .content(entry(5, "1.234", "2026-11-01", null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestedIncomeAndExpenseCategoriesAreAvailable() throws Exception {
        mockMvc.perform(post("/api/v1/ledger/entries").contentType(MediaType.APPLICATION_JSON)
                        .content(entry(10, "88.00", "2026-11-01", "水电费")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.categoryName").value("生活缴费"));
        mockMvc.perform(post("/api/v1/ledger/entries").contentType(MediaType.APPLICATION_JSON)
                        .content(entry(14, "20.00", "2026-11-01", "红包")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.categoryName").value("红包"))
                .andExpect(jsonPath("$.data.type").value("INCOME"));
    }

    @Test
    void unknownCategoryReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/ledger/entries").contentType(MediaType.APPLICATION_JSON)
                        .content(entry(999, "10.00", "2026-11-01", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }

    @Test
    void reversedAndMalformedDateParametersReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/ledger/entries")
                        .param("from", "2026-11-10").param("to", "2026-11-01"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/ledger/summary").param("from", "yesterday"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }

    @Test
    void missingEntryReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/ledger/entries/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("LEDGER_ENTRY_NOT_FOUND"));
    }

    private long createDirect(long categoryId, String amount, String date, String note) throws Exception {
        var result = mockMvc.perform(post("/api/v1/ledger/entries")
                        .contentType(MediaType.APPLICATION_JSON).content(entry(categoryId, amount, date, note)))
                .andExpect(status().isCreated()).andReturn();
        String body = result.getResponse().getContentAsString();
        var matcher = Pattern.compile("\\\"id\\\":(\\d+)").matcher(body);
        if (!matcher.find()) throw new AssertionError("Created response did not contain an id: " + body);
        return Long.parseLong(matcher.group(1));
    }

    private static String entry(long categoryId, String amount, String date, String note) {
        String noteValue = note == null ? "null" : "\"" + note.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        return "{\"categoryId\":" + categoryId + ",\"amount\":" + amount
                + ",\"occurredOn\":\"" + date + "\",\"note\":" + noteValue + "}";
    }
}
