package com.racoonsfinds.backend.review.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

import com.racoonsfinds.backend.review.domain.Review;
import com.racoonsfinds.backend.review.dto.ReviewResponseDto;
import com.racoonsfinds.backend.shared.utils.ModelMapperFactory;

/**
 * Mapper del dominio review. Concentra el typeMap de Review para no acoplar
 * `shared` a tipos de review/identity.
 */
public final class ReviewMapper {

    private static final ModelMapper mapper = ModelMapperFactory.standard();

    static {
        // === REVIEW (Entity → DTO) ===
        // STANDARD resuelve product.id→productId, user.id→userId
        // userName necesita mapeo explícito: user.username ≠ user.name
        mapper.typeMap(Review.class, ReviewResponseDto.class).addMappings(m -> {
            m.map(src -> src.getUser().getUsername(), ReviewResponseDto::setUserName);
        });
    }

    private ReviewMapper() {
    }

    public static <D, T> D map(final T entity, Class<D> outClass) {
        return mapper.map(entity, outClass);
    }

    public static <D, T> List<D> mapList(final List<T> entityList, Class<D> outClass) {
        return entityList.stream()
                .map(entity -> map(entity, outClass))
                .collect(Collectors.toList());
    }
}
