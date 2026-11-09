package com.eightsnpc.todo.api.error;

import org.springframework.http.HttpStatus;

public class LedgerEntryNotFoundException extends BusinessException {
    public LedgerEntryNotFoundException(long id) {
        super("LEDGER_ENTRY_NOT_FOUND", HttpStatus.NOT_FOUND, "未找到编号为 " + id + " 的账目");
    }
}
