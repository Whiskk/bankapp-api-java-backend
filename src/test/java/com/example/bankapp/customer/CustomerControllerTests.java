package com.example.bankapp.customer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.bankapp.controllers.CustomerController;
import com.example.bankapp.repos.InMemoryCustomerRepository;
import com.example.bankapp.services.CustomerService;

class CustomerControllerTests {

    private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(
                                new CustomerController(new CustomerService(new InMemoryCustomerRepository()))).build();
        }

    @Test
    void customerLifecycleWorks() throws Exception {
        String customerId = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada Lovelace\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ada Lovelace"))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\\\"id\\\":([0-9]+).*", "$1");

        mockMvc.perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ada Lovelace"));

        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada Byron\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ada Byron"));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ada Byron"));

        mockMvc.perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isNotFound());
    }
}