package com.example.bankapp.models;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document("transactions")
public record AccountTransaction(
        @Id @MongoId(targetType = FieldType.OBJECT_ID) String id,
        String accountId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String relatedAccountId,
        Instant createdAt
) {
}