package com.racoonsfinds.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

    static final ApplicationModules modules = ApplicationModules.of(RacoonsfindsBackendApplication.class);

    /**
     * Falla si algún módulo viola los boundaries (acceso a internals de otro
     * módulo, dependencias cíclicas, etc.). Los boundaries son ejecutables.
     */
    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    /**
     * Genera la documentación de la arquitectura en target/spring-modulith-docs:
     * diagramas PlantUML (C4) por módulo y el "module canvas". Material listo
     * para el README y la presentación del proyecto.
     */
    @Test
    void writesDocumentationSnippets() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases();
    }
}
