package com.racoonsfinds.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.wishlist.WishlistRequestDto;
import com.racoonsfinds.backend.dto.wishlist.WishlistResponseDto;
import com.racoonsfinds.backend.service.int_.WishlistService;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<ApiResponse<WishlistResponseDto>> addToWishlist(@RequestBody WishlistRequestDto dto) {
        return ResponseUtil.ok("Producto añadido a la lista de deseos", wishlistService.addToWishlist(dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistResponseDto>>> getUserWishlist() {
        return ResponseUtil.ok("Lista de deseos obtenida correctamente", wishlistService.getUserWishlist());
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(@PathVariable Long productId) {
        wishlistService.removeFromWishlist(productId);
        return ResponseUtil.ok("Producto eliminado de la lista de deseos correctamente");
    }
}
