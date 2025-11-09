package com.gearup.chatbotservice.entity;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for KnowledgeDocument entity
 */
@DisplayName("KnowledgeDocument Entity Tests")
class KnowledgeDocumentTest {

    @Test
    @DisplayName("Should convert float array to embedding string")
    void testSetEmbeddingFromArray() {
        // Given
        KnowledgeDocument document = new KnowledgeDocument();
        float[] embeddingArray = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f};

        // When
        document.setEmbeddingFromArray(embeddingArray);

        // Then
        assertThat(document.getEmbedding()).isNotNull();
        assertThat(document.getEmbedding()).startsWith("[");
        assertThat(document.getEmbedding()).endsWith("]");
        assertThat(document.getEmbedding()).contains("0.1");
        assertThat(document.getEmbedding()).contains("0.5");
    }

    @Test
    @DisplayName("Should convert embedding string back to float array")
    void testGetEmbeddingAsArray() {
        // Given
        KnowledgeDocument document = new KnowledgeDocument();
        float[] originalArray = {0.1f, 0.2f, 0.3f};
        document.setEmbeddingFromArray(originalArray);

        // When
        float[] retrievedArray = document.getEmbeddingAsArray();

        // Then
        assertThat(retrievedArray).isNotNull();
        assertThat(retrievedArray).hasSize(3);
        assertThat(retrievedArray[0]).isCloseTo(0.1f, org.assertj.core.data.Offset.offset(0.001f));
        assertThat(retrievedArray[1]).isCloseTo(0.2f, org.assertj.core.data.Offset.offset(0.001f));
        assertThat(retrievedArray[2]).isCloseTo(0.3f, org.assertj.core.data.Offset.offset(0.001f));
    }

    @Test
    @DisplayName("Should handle null embedding array")
    void testSetNullEmbeddingArray() {
        // Given
        KnowledgeDocument document = new KnowledgeDocument();

        // When
        document.setEmbeddingFromArray(null);

        // Then
        assertThat(document.getEmbedding()).isNull();
    }

    @Test
    @DisplayName("Should return null for null embedding string")
    void testGetNullEmbeddingArray() {
        // Given
        KnowledgeDocument document = new KnowledgeDocument();
        document.setEmbedding(null);

        // When
        float[] result = document.getEmbeddingAsArray();

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for empty embedding string")
    void testGetEmptyEmbeddingArray() {
        // Given
        KnowledgeDocument document = new KnowledgeDocument();
        document.setEmbedding("");

        // When
        float[] result = document.getEmbeddingAsArray();

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should round-trip embedding conversion")
    void testRoundTripConversion() {
        // Given
        KnowledgeDocument document = new KnowledgeDocument();
        float[] originalArray = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};

        // When
        document.setEmbeddingFromArray(originalArray);
        float[] retrievedArray = document.getEmbeddingAsArray();

        // Then
        assertThat(retrievedArray).hasSize(originalArray.length);
        for (int i = 0; i < originalArray.length; i++) {
            assertThat(retrievedArray[i]).isCloseTo(originalArray[i], 
                org.assertj.core.data.Offset.offset(0.001f));
        }
    }

    @Test
    @DisplayName("Should handle large embedding arrays")
    void testLargeEmbeddingArray() {
        // Given
        KnowledgeDocument document = new KnowledgeDocument();
        float[] largeArray = new float[768]; // Standard embedding size
        for (int i = 0; i < 768; i++) {
            largeArray[i] = (float) Math.random();
        }

        // When
        document.setEmbeddingFromArray(largeArray);
        float[] retrieved = document.getEmbeddingAsArray();

        // Then
        assertThat(retrieved).hasSize(768);
    }
}
