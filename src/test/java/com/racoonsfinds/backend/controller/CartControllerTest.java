package com.racoonsfinds.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.racoonsfinds.backend.dto.cart.CartRequestDto;
import com.racoonsfinds.backend.dto.cart.CartResponseDto;
import com.racoonsfinds.backend.service.int_.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock private CartService cartService;

    @InjectMocks private CartController cartController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void addToCart_ShouldReturnOk() throws Exception {
        CartRequestDto req = new CartRequestDto();
        req.setProductId(10L); req.setAmount(2);

        CartResponseDto resp = new CartResponseDto();
        resp.setId(1L); resp.setUserId(1L); resp.setProductId(10L);
        resp.setProductName("Prod"); resp.setProductImage("URL");
        resp.setProductPrice(new BigDecimal("9.99")); resp.setAmount(2);

        when(cartService.addToCart(any(CartRequestDto.class))).thenReturn(resp);

        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.productId").value(10));

        verify(cartService).addToCart(any(CartRequestDto.class));
    }

    @Test
    void getUserCart_ShouldReturnList() throws Exception {
        CartResponseDto item = new CartResponseDto(); item.setId(1L);
        when(cartService.getUserCart()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/cart"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void removeFromCart_ShouldReturnOk() throws Exception {
        doNothing().when(cartService).removeFromCart(10L);
        mockMvc.perform(delete("/api/cart/{productId}", 10L))
            .andExpect(status().isOk());
        verify(cartService).removeFromCart(10L);
    }

    @Test
    void clearCart_ShouldReturnOk() throws Exception {
        doNothing().when(cartService).clearCart();
        mockMvc.perform(delete("/api/cart/clear"))
            .andExpect(status().isOk());
        verify(cartService).clearCart();
    }
}

