package com.example.bankapp.account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.bankapp.controllers.AccountController;
import com.example.bankapp.models.Customer;
import com.example.bankapp.repos.InMemoryAccountRepository;
import com.example.bankapp.repos.InMemoryAccountTransactionRepository;
import com.example.bankapp.repos.InMemoryCustomerRepository;
import com.example.bankapp.services.AccountService;

class AccountControllerTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InMemoryCustomerRepository customerRepository = new InMemoryCustomerRepository();
        customerRepository.save(new Customer("test-customer", "Ada Lovelace", null, null));
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController(new AccountService(
                new InMemoryAccountRepository(),
                new InMemoryAccountTransactionRepository(),
                customerRepository))).build();
    }

    @Test
    void customerCanHaveMultipleAccountsAndManageMoney() throws Exception {
        String savingsId = createAccount("SAVINGS");
        String checkingId = createAccount("CHECKING");

        mockMvc.perform(post("/api/accounts/{id}/deposit", savingsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":500.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500));

        mockMvc.perform(post("/api/accounts/{id}/withdraw", savingsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":200.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(300));

        mockMvc.perform(get("/api/accounts/{id}", savingsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(300));

        mockMvc.perform(get("/api/accounts/{id}/transactions", savingsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[0].amount").value(500))
                .andExpect(jsonPath("$[1].type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$[1].amount").value(200));

        mockMvc.perform(post("/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromAccountId\":\"" + savingsId
                                + "\",\"toAccountId\":\"" + checkingId + "\",\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savingsId))
                .andExpect(jsonPath("$.balance").value(200));

        mockMvc.perform(get("/api/accounts/{id}", checkingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100));

        mockMvc.perform(get("/api/accounts/{id}/transactions", savingsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[2].type").value("TRANSFER"))
                .andExpect(jsonPath("$[2].relatedAccountId").value(checkingId));

        mockMvc.perform(get("/api/accounts/{id}/transactions", checkingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TRANSFER"))
                .andExpect(jsonPath("$[0].relatedAccountId").value(savingsId));
    }

    private String createAccount(String accountType) throws Exception {
        return mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"test-customer\",\"accountType\":\"" + accountType + "\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }
}