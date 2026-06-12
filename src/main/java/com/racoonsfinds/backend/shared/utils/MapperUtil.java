package com.racoonsfinds.backend.shared.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

/**
 * Mapper genérico (sin typeMaps de dominio). Para mapeos con reglas específicas
 * cada dominio define su propio mapper (ej. CatalogMapper, ReviewMapper).
 */
public class MapperUtil {

    private static final ModelMapper mapper = ModelMapperFactory.standard();

    private MapperUtil() {
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
