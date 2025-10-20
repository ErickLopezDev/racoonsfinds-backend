package com.racoonsfinds.backend.shared.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import com.racoonsfinds.backend.dto.products.ProductRequestDto;
import com.racoonsfinds.backend.dto.products.ProductUpdateRequest;
import com.racoonsfinds.backend.dto.user.UserDto;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.model.User;

public class MapperUtil {

    private static final ModelMapper mapper = new ModelMapper();

    private MapperUtil() {
    }
    static {
    mapper.getConfiguration()
        .setSkipNullEnabled(true)
        .setMatchingStrategy(MatchingStrategies.STRICT);

    // === PRODUCTOS ===
    mapper.typeMap(ProductUpdateRequest.class, Product.class).addMappings(m -> {
        m.skip(Product::setId);
        m.skip(Product::setVersion);
        m.skip(Product::setCategory);
        m.skip(Product::setUser);
    });

    mapper.typeMap(ProductRequestDto.class, Product.class).addMappings(m -> {
        m.skip(Product::setId);
        m.skip(Product::setVersion);
        m.skip(Product::setUser);
        m.skip(Product::setCategory);
    });

    // === USUARIO ===
    mapper.typeMap(UserDto.class, User.class).addMappings(m -> {
        m.skip(User::setId);
        m.skip(User::setPassword);   // Evita que el dto borre la contraseña
    });
}

    // (DTO → Entity o Entity → DTO)
    public static <D, T> D map(final T entity, Class<D> outClass) {
        return mapper.map(entity, outClass);
    }

    // List<Entity> → List<DTO>)
    public static <D, T> List<D> mapList(final List<T> entityList, Class<D> outClass) {
        return entityList.stream()
                .map(entity -> map(entity, outClass))
                .collect(Collectors.toList());
    }

    // Mapear DTO sobre entidad existente
    public static <T, D> void map(final D sourceDto, T targetEntity) {
        mapper.map(sourceDto, targetEntity);
    }
}