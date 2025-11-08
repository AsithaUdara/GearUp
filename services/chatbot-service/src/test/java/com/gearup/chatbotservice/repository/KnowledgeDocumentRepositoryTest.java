package com.gearup.chatbotservice.repository;

import com.gearup.chatbotservice.config.TestRabbitMQConfig;
import com.gearup.chatbotservice.entity.KnowledgeDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for KnowledgeDocument
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestRabbitMQConfig.class)
@DisplayName("KnowledgeDocumentRepository Tests")
class KnowledgeDocumentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private KnowledgeDocumentRepository repository;
    
    /**
     * Helper method to create a 768-dimensional test embedding
     */
    private float[] createTestEmbedding() {
        float[] embedding = new float[768];
        for (int i = 0; i < 768; i++) {
            embedding[i] = (float) (i / 1000.0); // Simple pattern: 0.000, 0.001, 0.002, ...
        }
        return embedding;
    }
    
    @BeforeEach
    void setUp() {
        // Clear repository before each test to ensure isolation
        repository.deleteAll();
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save and retrieve knowledge document")
    void testSaveAndFind() {
        // Given
        KnowledgeDocument document = KnowledgeDocument.builder()
                .content("How to change oil in a car")
                .source("maintenance-guide.pdf")
                .category("maintenance")
                .build();

        // When
        KnowledgeDocument saved = repository.save(document);
        entityManager.flush();
        Optional<KnowledgeDocument> found = repository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("How to change oil in a car");
        assertThat(found.get().getSource()).isEqualTo("maintenance-guide.pdf");
        assertThat(found.get().getCategory()).isEqualTo("maintenance");
    }

    @Test
    @DisplayName("Should find documents by category")
    void testFindByCategory() {
        // Given
        KnowledgeDocument doc1 = KnowledgeDocument.builder()
                .content("Brake maintenance tips")
                .source("guide1.pdf")
                .category("maintenance")
                .build();

        KnowledgeDocument doc2 = KnowledgeDocument.builder()
                .content("Tire pressure guidelines")
                .source("guide2.pdf")
                .category("maintenance")
                .build();

        KnowledgeDocument doc3 = KnowledgeDocument.builder()
                .content("Booking appointment process")
                .source("guide3.pdf")
                .category("booking")
                .build();

        repository.save(doc1);
        repository.save(doc2);
        repository.save(doc3);
        entityManager.flush();

        // When
        List<KnowledgeDocument> maintenanceDocs = repository.findByCategory("maintenance");

        // Then
        assertThat(maintenanceDocs).hasSize(2);
        assertThat(maintenanceDocs).allMatch(doc -> "maintenance".equals(doc.getCategory()));
    }

    @Test
    @DisplayName("Should find documents by source")
    void testFindBySource() {
        // Given
        KnowledgeDocument doc1 = KnowledgeDocument.builder()
                .content("Content 1")
                .source("manual.pdf")
                .category("general")
                .build();

        KnowledgeDocument doc2 = KnowledgeDocument.builder()
                .content("Content 2")
                .source("manual.pdf")
                .category("general")
                .build();

        repository.save(doc1);
        repository.save(doc2);
        entityManager.flush();

        // When
        List<KnowledgeDocument> docs = repository.findBySource("manual.pdf");

        // Then
        assertThat(docs).hasSize(2);
        assertThat(docs).allMatch(doc -> "manual.pdf".equals(doc.getSource()));
    }

    @Test
    @DisplayName("Should save document with embedding")
    void testSaveWithEmbedding() {
        // Given
        KnowledgeDocument document = KnowledgeDocument.builder()
                .content("Test content")
                .source("test.pdf")
                .category("test")
                .build();

        float[] embedding = createTestEmbedding();
        document.setEmbeddingFromArray(embedding);

        // When
        KnowledgeDocument saved = repository.save(document);
        entityManager.flush();
        entityManager.clear(); // Clear to force reload from DB

        Optional<KnowledgeDocument> found = repository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmbedding()).isNotNull();
        assertThat(found.get().getEmbeddingAsArray()).isNotNull();
    }

    @Test
    @DisplayName("Should update document")
    void testUpdate() {
        // Given
        KnowledgeDocument document = KnowledgeDocument.builder()
                .content("Original content")
                .source("source.pdf")
                .category("category1")
                .build();

        KnowledgeDocument saved = repository.save(document);
        entityManager.flush();

        // When
        saved.setContent("Updated content");
        saved.setCategory("category2");
        repository.save(saved);
        entityManager.flush();
        entityManager.clear();

        Optional<KnowledgeDocument> updated = repository.findById(saved.getId());

        // Then
        assertThat(updated).isPresent();
        assertThat(updated.get().getContent()).isEqualTo("Updated content");
        assertThat(updated.get().getCategory()).isEqualTo("category2");
    }

    @Test
    @DisplayName("Should delete document")
    void testDelete() {
        // Given
        KnowledgeDocument document = KnowledgeDocument.builder()
                .content("To be deleted")
                .source("temp.pdf")
                .category("temp")
                .build();

        KnowledgeDocument saved = repository.save(document);
        entityManager.flush();
        Long id = saved.getId();

        // When
        repository.delete(saved);
        entityManager.flush();

        // Then
        Optional<KnowledgeDocument> found = repository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should count all documents")
    void testCount() {
        // Given
        repository.save(KnowledgeDocument.builder()
                .content("Content 1").source("s1.pdf").category("c1").build());
        repository.save(KnowledgeDocument.builder()
                .content("Content 2").source("s2.pdf").category("c2").build());
        repository.save(KnowledgeDocument.builder()
                .content("Content 3").source("s3.pdf").category("c3").build());
        entityManager.flush();

        // When
        long count = repository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should return empty list for non-existent category")
    void testFindByNonExistentCategory() {
        // When
        List<KnowledgeDocument> docs = repository.findByCategory("non-existent");

        // Then
        assertThat(docs).isEmpty();
    }
}
