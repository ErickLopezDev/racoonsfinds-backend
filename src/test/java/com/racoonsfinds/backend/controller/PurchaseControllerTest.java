package com.racoonsfinds.backend.controller;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.purchase.PurchaseResponseDto;
import com.racoonsfinds.backend.service.int_.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    @Mock private PurchaseService purchaseService;
    @InjectMocks private PurchaseController purchaseController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(purchaseController).build();
    }

    @Test
    void purchaseFromCart_ShouldReturnCreated() throws Exception {
        PurchaseResponseDto dto = new PurchaseResponseDto(); dto.setId(1L);
        when(purchaseService.purchaseFromCart(any())).thenReturn(ResponseEntity.status(201).body(new ApiResponse<>("ok", dto)));

        mockMvc.perform(post("/api/purchases/from-cart").param("description", "d"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void purchaseOne_ShouldReturnCreated() throws Exception {
        PurchaseResponseDto dto = new PurchaseResponseDto(); dto.setId(5L);
        when(purchaseService.purchaseOne(eq(9L), any())).thenReturn(ResponseEntity.status(201).body(new ApiResponse<>("ok", dto)));

        mockMvc.perform(post("/api/purchases/one/{cartId}", 9).param("description", "d"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").value(5));
    }
}

