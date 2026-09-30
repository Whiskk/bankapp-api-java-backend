package com.example.bankapp.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document("customers")
public record Customer(
	@Id @MongoId(targetType = FieldType.OBJECT_ID) String id,
	String name,
	String username,
	String passwordHash,
	Boolean admin) {

	public Customer {
		admin = Boolean.TRUE.equals(admin);
	}

	public Customer(String id, String name, String username, String passwordHash) {
		this(id, name, username, passwordHash, false);
	}
}