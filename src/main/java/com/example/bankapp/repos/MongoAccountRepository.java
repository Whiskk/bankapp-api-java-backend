package com.example.bankapp.repos;

import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.example.bankapp.models.Account;

@Repository
public class MongoAccountRepository implements AccountRepository {

    private final MongoTemplate mongoTemplate;

    public MongoAccountRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<Account> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, Account.class));
    }

    @Override
    public Account save(Account account) {
        return mongoTemplate.save(account);
    }
}