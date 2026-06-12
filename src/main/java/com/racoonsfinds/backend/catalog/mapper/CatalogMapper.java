package com.racoonsfinds.backend.catalog.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

import com.racoonsfinds.backend.catalog.domain.Product;
import com.racoonsfinds.backend.catalog.dto.ProductRequestDto;
import com.racoonsfinds.backend.catalog.dto.ProductResponseDto;
import com.racoonsfinds.backend.catalog.dto.ProductUpdateRequest;
import com.racoonsfinds.backend.shared.utils.ModelMapperFactory;

/**
 * Mapper del dominio catalog. Concentra los typeMaps de Product para no acoplar
 * `shared` a tipos de catalog.
 */
public final class CatalogMapper {

    private static final ModelMapper mapper = ModelMapperFactory.standard();

    static {
        // === PRODUCTOS (DTO → Entity) ===
        // emptyTypeMap + skips antes de implicitMappings: con STANDARD, `categoryId`
        // se mapearía implícitamente a `category.id` y entraría en conflicto con
        // skip(setCategory). Declarar los skips primero evita ese choque.
        mapper.emptyTypeMap(ProductRequestDto.class, Product.class)
                .addMappings(m -> {
                    m.skip(Product::setId);
                    m.skip(Product::setVersion);
                    m.skip(Product::setUser);
                    m.skip(Product::setCategory);
                })
                .implicitMappings();

        mapper.emptyTypeMap(ProductUpdateRequest.class, Product.class)
                .addMappings(m -> {
                    m.skip(Product::setId);
                    m.skip(Product::setVersion);
                    m.skip(Product::setCategory);
                    m.skip(Product::setUser);
                })
                .implicitMappings();

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
    }

    private CatalogMapper() {
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
