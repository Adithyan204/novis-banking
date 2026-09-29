package com.novis.fraud.service;

import com.novis.fraud.entity.FraudFlag;
import com.novis.transaction.entity.Transaction;

public interface FraudRule {
    record Result(boolean flagged, String ruleName, FraudFlag.Severity severity, String reason) {}
    
    Result evaluate(Transaction transaction);
}
