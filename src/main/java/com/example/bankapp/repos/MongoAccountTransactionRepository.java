package com.example.bankapp.repos;

import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.example.bankapp.models.AccountTransaction;

@Repository
public class MongoAccountTransactionRepository implements AccountTransactionRepository {

    private final MongoTemplate mongoTemplate;

    public MongoAccountTransactionRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<AccountTransaction> findByAccountId(String accountId) {
        Query query = Query.query(Criteria.where("accountId").is(accountId));
        return mongoTemplate.find(query, AccountTransaction.class);
    }

    @Override
    public AccountTransaction save(AccountTransaction transaction) {
        return mongoTemplate.save(transaction);
    }

    @Override
    public long deleteByAccountIdIn(Collection<String> accountIds) {
        Query query = Query.query(Criteria.where("accountId").in(accountIds));
        return mongoTemplate.remove(query, AccountTransaction.class).getDeletedCount();
    }
}