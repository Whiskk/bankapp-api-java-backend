package com.example.bankapp.models;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document("accounts")
public record Account(
	@Id @MongoId(targetType = FieldType.OBJECT_ID) String id,
	String userId,
	AccountType accountType,
	BigDecimal balance) {
}