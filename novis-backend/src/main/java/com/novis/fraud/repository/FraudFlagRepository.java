package com.novis.fraud.repository;

import com.novis.fraud.entity.FraudFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudFlagRepository extends JpaRepository<FraudFlag, Long> {
    List<FraudFlag> findByStatus(FraudFlag.FlagStatus status);
    Optional<FraudFlag> findByTransactionId(Long transactionId);
}
