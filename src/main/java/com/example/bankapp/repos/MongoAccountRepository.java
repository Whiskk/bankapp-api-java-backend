package com.example.bankapp.repos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
    public List<Account> findByUserId(String userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        return mongoTemplate.find(query, Account.class);
    }

    @Override
    public Account save(Account account) {
        return mongoTemplate.save(account);
    }

    @Override
    public boolean deleteByIdIfZeroBalance(String id) {
        Query query = Query.query(Criteria.where("_id").is(id).and("balance").is(BigDecimal.ZERO));
        return mongoTemplate.remove(query, Account.class).getDeletedCount() > 0;
    }

    @Override
    public boolean deleteByUserId(String userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        return mongoTemplate.remove(query, Account.class).getDeletedCount() > 0;
    }
}