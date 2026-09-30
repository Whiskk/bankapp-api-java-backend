package com.example.bankapp.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.example.bankapp.models.Customer;

@Repository
public class MongoCustomerRepository implements CustomerRepository {

    private final MongoTemplate mongoTemplate;

    public MongoCustomerRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Customer> findAll() {
        return mongoTemplate.findAll(Customer.class);
    }

    @Override
    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, Customer.class));
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        Query query = Query.query(Criteria.where("username").is(username));
        return Optional.ofNullable(mongoTemplate.findOne(query, Customer.class));
    }

    @Override
    public Customer save(Customer customer) {
        return mongoTemplate.save(customer);
    }

    @Override
    public boolean deleteById(String id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return mongoTemplate.remove(query, Customer.class).getDeletedCount() > 0;
    }
}