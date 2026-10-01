package com.example.bankapp.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import com.example.bankapp.models.Customer;
import com.example.bankapp.models.ProfileUpdateRequest;
import com.example.bankapp.repos.AccountRepository;
import com.example.bankapp.repos.AccountTransactionRepository;
import com.example.bankapp.repos.InMemoryCustomerRepository;
import com.example.bankapp.security.JwtAuthenticationFilter;
import com.example.bankapp.security.JwtService;

class CustomerProfileTest {

    private InMemoryCustomerRepository customers;
    private PasswordEncoder passwordEncoder;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        customers = new InMemoryCustomerRepository();
        passwordEncoder = new BCryptPasswordEncoder();
        service = new CustomerService(
                customers, mock(AccountRepository.class), mock(AccountTransactionRepository.class), passwordEncoder);
        customers.save(new Customer("customer-1", "Ada", "ada", passwordEncoder.encode("oldpassword")));
    }

    @Test
    void nameEditKeepsCredentialsAndAdminStatus() {
        Customer updated = service.updateOwnProfile("ada", new ProfileUpdateRequest("Ada Byron", "ada", null, null));

        assertEquals("Ada Byron", updated.name());
        assertEquals("ada", updated.username());
        assertTrue(passwordEncoder.matches("oldpassword", updated.passwordHash()));
        assertFalse(updated.admin());
    }

    @Test
    void usernameAndPasswordChangeNeedCurrentPasswordAndKeepIdInJwt() {
        JwtService jwtService = new JwtService("profile-test-signing-key-with-32-bytes", 3600000);
        String token = jwtService.createToken("customer-1");

        Customer updated = service.updateOwnProfile("ada", new ProfileUpdateRequest(
                "Ada", "ada-new", "oldpassword", "newpassword"));

        assertEquals("customer-1", updated.id());
        assertEquals("customer-1", jwtService.extractCustomerId(token));
        assertEquals("ada-new", customers.findById("customer-1").orElseThrow().username());
        assertFalse(passwordEncoder.matches("oldpassword", updated.passwordHash()));
        assertTrue(passwordEncoder.matches("newpassword", updated.passwordHash()));
    }

    @Test
    void existingJwtResolvesRenamedCustomer() throws Exception {
        JwtService jwtService = new JwtService("profile-test-signing-key-with-32-bytes", 3600000);
        String token = jwtService.createToken("customer-1");
        service.updateOwnProfile("ada", new ProfileUpdateRequest("Ada", "ada-new", "oldpassword", null));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/accounts/me");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        try {
            new JwtAuthenticationFilter(jwtService, customers).doFilter(
                    request, new MockHttpServletResponse(), new MockFilterChain());

            assertEquals("ada-new", SecurityContextHolder.getContext().getAuthentication().getName());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void sensitiveEditRejectsMissingOrWrongCurrentPassword() {
        ResponseStatusException missing = assertThrows(ResponseStatusException.class, () ->
                service.updateOwnProfile("ada", new ProfileUpdateRequest("Ada", "new-ada", null, null)));
        ResponseStatusException wrong = assertThrows(ResponseStatusException.class, () ->
                service.updateOwnProfile("ada", new ProfileUpdateRequest("Ada", "ada", "incorrect", "newpassword")));

        assertEquals(HttpStatus.FORBIDDEN, missing.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, wrong.getStatusCode());
        assertEquals("ada", customers.findById("customer-1").orElseThrow().username());
    }

    @Test
    void usernameConflictDoesNotChangeProfile() {
        customers.save(new Customer("customer-2", "Grace", "grace", passwordEncoder.encode("somepassword")));

        ResponseStatusException conflict = assertThrows(ResponseStatusException.class, () ->
                service.updateOwnProfile("ada", new ProfileUpdateRequest("Ada", "grace", "oldpassword", null)));

        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        assertEquals("ada", customers.findById("customer-1").orElseThrow().username());
    }
}