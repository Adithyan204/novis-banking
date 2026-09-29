package com.novis.fraud.service;

import com.novis.fraud.entity.FraudFlag;
import com.novis.fraud.repository.FraudFlagRepository;
import com.novis.transaction.entity.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FraudRuleEngine {

    private final List<FraudRule> rules;
    private final FraudFlagRepository fraudFlagRepository;

    public List<FraudRule.Result> evaluate(Transaction transaction) {
        return rules.stream()
                .map(rule -> rule.evaluate(transaction))
                .filter(FraudRule.Result::flagged)
                .collect(Collectors.toList());
    }

    public void createFlags(Transaction transaction, List<FraudRule.Result> flaggedResults) {
        for (FraudRule.Result result : flaggedResults) {
            FraudFlag flag = FraudFlag.builder()
                    .transaction(transaction)
                    .ruleTriggered(result.ruleName())
                    .severity(result.severity())
                    .reviewNote(result.reason())
                    .build();
            fraudFlagRepository.save(flag);
        }
    }

    public FraudRuleEngine(List<FraudRule> rules, FraudFlagRepository fraudFlagRepository) {
        this.rules = rules;
        this.fraudFlagRepository = fraudFlagRepository;
    }
}
