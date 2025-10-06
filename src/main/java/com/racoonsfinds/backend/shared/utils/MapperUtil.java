package com.racoonsfinds.backend.shared.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

public class MapperUtil {

    private static final ModelMapper mapper = new ModelMapper();

    private MapperUtil() {
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