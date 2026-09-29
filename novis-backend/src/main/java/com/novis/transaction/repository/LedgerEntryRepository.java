package com.novis.transaction.repository;

import com.novis.transaction.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM LedgerEntry e WHERE e.account.id = :accountId AND e.entryType = :entryType")
    BigDecimal sumAmountByAccountIdAndEntryType(@Param("accountId") Long accountId, @Param("entryType") LedgerEntry.EntryType entryType);
}
