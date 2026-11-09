package com.eightsnpc.todo.api;

import com.eightsnpc.todo.PageResult;
import com.eightsnpc.todo.api.dto.ApiResponse;
import com.eightsnpc.todo.api.dto.FinanceSummaryResponse;
import com.eightsnpc.todo.api.dto.LedgerEntryRequest;
import com.eightsnpc.todo.api.dto.LedgerEntryResponse;
import com.eightsnpc.todo.api.error.LedgerEntryNotFoundException;
import com.eightsnpc.todo.finance.EntryType;
import com.eightsnpc.todo.finance.FinanceService;
import com.eightsnpc.todo.finance.LedgerEntry;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {
    private final FinanceService financeService;

    public LedgerController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/entries")
    public ResponseEntity<ApiResponse<LedgerEntryResponse>> create(
            @Valid @RequestBody LedgerEntryRequest request) {
        LedgerEntry entry = financeService.create(request.categoryId(), request.amount(),
                request.occurredOn(), request.note());
        return ResponseEntity.created(URI.create("/api/v1/ledger/entries/" + entry.id()))
                .body(ApiResponse.success(LedgerEntryResponse.from(entry)));
    }

    @GetMapping("/entries")
    public ApiResponse<PageResult<LedgerEntryResponse>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) EntryType type) {
        PageResult<LedgerEntry> entries = financeService.page(page, size, from, to, categoryId, type);
        List<LedgerEntryResponse> records = entries.records().stream().map(LedgerEntryResponse::from).toList();
        return ApiResponse.success(new PageResult<>(entries.current(), entries.size(), entries.total(), records));
    }

    @GetMapping("/entries/{id}")
    public ApiResponse<LedgerEntryResponse> get(@PathVariable long id) {
        return financeService.findById(id).map(LedgerEntryResponse::from).map(ApiResponse::success)
                .orElseThrow(() -> new LedgerEntryNotFoundException(id));
    }

    @PutMapping("/entries/{id}")
    public ApiResponse<LedgerEntryResponse> update(
            @PathVariable long id, @Valid @RequestBody LedgerEntryRequest request) {
        return financeService.update(id, request.categoryId(), request.amount(), request.occurredOn(), request.note())
                .map(LedgerEntryResponse::from).map(ApiResponse::success)
                .orElseThrow(() -> new LedgerEntryNotFoundException(id));
    }

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable long id) {
        if (!financeService.delete(id)) throw new LedgerEntryNotFoundException(id);
        return ResponseEntity.ok(ApiResponse.successWithoutData());
    }

    @GetMapping("/summary")
    public ApiResponse<FinanceSummaryResponse> summary(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ApiResponse.success(FinanceSummaryResponse.from(financeService.summarize(from, to)));
    }
}
