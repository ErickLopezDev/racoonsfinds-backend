package com.racoonsfinds.backend.catalog;

import com.racoonsfinds.backend.catalog.domain.Product;
import com.racoonsfinds.backend.catalog.repository.ProductRepository;
import com.racoonsfinds.backend.identity.port.UserDirectoryPort;
import com.racoonsfinds.backend.platform.storage.S3Service;
import com.racoonsfinds.backend.shared.event.ReviewStatsChangedEvent;
import com.racoonsfinds.backend.support.PostgresTestContainer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica la denormalización dirigida por eventos: cuando review publica
 * ReviewStatsChangedEvent, el listener de catalog persiste las estadísticas
 * sobre el Product (lecturas locales, sin cruzar el boundary).
 *
 * El listener es @ApplicationModuleListener (asíncrono, after-commit); se usa
 * la API Scenario de Spring Modulith para publicar dentro de una transacción y
 * esperar el cambio de estado. Postgres real via Testcontainers.
 */
@ApplicationModuleTest
class ReviewStatsDenormalizationTest extends PostgresTestContainer {

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private UserDirectoryPort userDirectoryPort;

    @MockitoBean
    private S3Service s3Service;

    @Test
    void denormalizesReviewStatsOntoProduct(Scenario scenario) {
        Product product = new Product();
        product.setName("Termo");
        product.setStock(10);
        product.setEliminado(false);
        Product saved = productRepository.save(product);

        scenario.publish(new ReviewStatsChangedEvent(saved.getId(), 4.5, 3L))
                .andWaitForStateChange(
                        () -> productRepository.findById(saved.getId()).orElseThrow().getReviewCount())
                .andVerify(reviewCount -> {
                    Product reloaded = productRepository.findById(saved.getId()).orElseThrow();
                    assertThat(reloaded.getReviewCount()).isEqualTo(3L);
                    assertThat(reloaded.getAverageRating()).isEqualTo(4.5);
                });
    }
}
