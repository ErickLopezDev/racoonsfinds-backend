package com.racoonsfinds.backend.shared.utils;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

/**
 * Construye instancias de ModelMapper con la configuración genérica del proyecto.
 * Cada dominio crea la suya y registra sus propios typeMaps, evitando que `shared`
 * dependa de tipos de dominio.
 */
public final class ModelMapperFactory {

    private ModelMapperFactory() {
    }

    public static ModelMapper standard() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STANDARD);
        return mapper;
    }
}
