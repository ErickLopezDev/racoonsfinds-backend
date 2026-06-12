package com.racoonsfinds.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    static final ApplicationModules modules = ApplicationModules.of(RacoonsfindsBackendApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }
}
