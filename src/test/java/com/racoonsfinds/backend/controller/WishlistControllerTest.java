package com.racoonsfinds.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.racoonsfinds.backend.dto.wishlist.WishlistRequestDto;
import com.racoonsfinds.backend.dto.wishlist.WishlistResponseDto;
import com.racoonsfinds.backend.service.int_.WishlistService;
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
class WishlistControllerTest {

    @Mock private WishlistService wishlistService;
    @InjectMocks private WishlistController wishlistController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(wishlistController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void addToWishlist_ShouldReturnOk() throws Exception {
        WishlistRequestDto req = new WishlistRequestDto(); req.setProductId(10L);
        WishlistResponseDto resp = new WishlistResponseDto();
        resp.setId(1L); resp.setUserId(1L); resp.setProductId(10L);
        resp.setProductName("Prod"); resp.setProductImage("URL"); resp.setProductPrice(new BigDecimal("1.00"));

        when(wishlistService.addToWishlist(any(WishlistRequestDto.class))).thenReturn(resp);

        mockMvc.perform(post("/api/wishlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.productId").value(10));

        verify(wishlistService).addToWishlist(any(WishlistRequestDto.class));
    }

    @Test
    void getUserWishlist_ShouldReturnList() throws Exception {
        WishlistResponseDto item = new WishlistResponseDto(); item.setId(2L);
        when(wishlistService.getUserWishlist()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/wishlist"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value(2));
    }

    @Test
    void removeFromWishlist_ShouldReturnOk() throws Exception {
        doNothing().when(wishlistService).removeFromWishlist(10L);
        mockMvc.perform(delete("/api/wishlist/{productId}", 10L))
            .andExpect(status().isOk());
        verify(wishlistService).removeFromWishlist(10L);
    }
}

