package com.racoonsfinds.backend.shared.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import com.racoonsfinds.backend.dto.products.ProductRequestDto;
import com.racoonsfinds.backend.dto.products.ProductResponseDto;
import com.racoonsfinds.backend.dto.products.ProductUpdateRequest;
import com.racoonsfinds.backend.dto.review.ReviewResponseDto;
import com.racoonsfinds.backend.dto.user.UserDto;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.model.Review;
import com.racoonsfinds.backend.model.User;

public class MapperUtil {

    private static final ModelMapper mapper = new ModelMapper();

    private MapperUtil() {
    }

    static {
        mapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STANDARD);

        // === PRODUCTOS (DTO → Entity) ===
        mapper.typeMap(ProductRequestDto.class, Product.class).addMappings(m -> {
            m.skip(Product::setId);
            m.skip(Product::setVersion);
            m.skip(Product::setUser);
            m.skip(Product::setCategory);
        });

        mapper.typeMap(ProductUpdateRequest.class, Product.class).addMappings(m -> {
            m.skip(Product::setId);
            m.skip(Product::setVersion);
            m.skip(Product::setCategory);
            m.skip(Product::setUser);
        });

        // === PRODUCTOS (Entity → DTO) ===
        // Campos con lógica adicional (S3, lazy relations, queries) se resuelven en el service
        mapper.typeMap(Product.class, ProductResponseDto.class).addMappings(m -> {
            m.skip(ProductResponseDto::setCategoryId);
            m.skip(ProductResponseDto::setCategoryName);
            m.skip(ProductResponseDto::setUserId);
            m.skip(ProductResponseDto::setUserName);
            m.skip(ProductResponseDto::setImage);
            m.skip(ProductResponseDto::setAverageRating);
            m.skip(ProductResponseDto::setReviewCount);
        });

        // === REVIEW (Entity → DTO) ===
        // STANDARD resuelve product.id→productId, user.id→userId
        // userName necesita mapeo explícito: user.username ≠ user.name
        mapper.typeMap(Review.class, ReviewResponseDto.class).addMappings(m -> {
            m.map(src -> src.getUser().getUsername(), ReviewResponseDto::setUserName);
        });

        // === USUARIO (DTO → Entity) ===
        mapper.typeMap(UserDto.class, User.class).addMappings(m -> {
            m.skip(User::setId);
            m.skip(User::setPassword);
        });
    }

    public static <D, T> D map(final T entity, Class<D> outClass) {
        return mapper.map(entity, outClass);
    }

    public static <D, T> List<D> mapList(final List<T> entityList, Class<D> outClass) {
        return entityList.stream()
                .map(entity -> map(entity, outClass))
                .collect(Collectors.toList());
    }

    public static <T, D> void map(final D sourceDto, T targetEntity) {
        mapper.map(sourceDto, targetEntity);
    }
}
