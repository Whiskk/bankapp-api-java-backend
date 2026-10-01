package com.example.bankapp.controllers;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.bankapp.models.Customer;
import com.example.bankapp.models.CustomerRequest;
import com.example.bankapp.models.CustomerResponse;
import com.example.bankapp.models.ProfileUpdateRequest;
import com.example.bankapp.services.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers().stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @GetMapping("/me")
    public CustomerResponse getMyProfile(Authentication authentication) {
        return CustomerResponse.from(customerService.getCustomerByUsername(authentication.getName()));
    }

    @PutMapping("/me")
    public CustomerResponse updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return CustomerResponse.from(customerService.updateOwnProfile(authentication.getName(), request));
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomerById(@PathVariable String id) {
        return CustomerResponse.from(customerService.getCustomerById(id));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> postCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customer = customerService.createCustomer(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(customer.id())
                .toUri();
        return ResponseEntity.created(location).body(CustomerResponse.from(customer));
    }

    @PutMapping("/{id}")
    public CustomerResponse putCustomer(@PathVariable String id, @Valid @RequestBody CustomerRequest request) {
        return CustomerResponse.from(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}