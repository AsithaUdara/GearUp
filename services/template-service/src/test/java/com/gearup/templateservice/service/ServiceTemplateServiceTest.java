package com.gearup.templateservice.service;

import com.gearup.templateservice.dto.ServiceTemplateDto;
import com.gearup.templateservice.exception.ResourceNotFoundException;
import com.gearup.templateservice.model.ServiceTemplate;
import com.gearup.templateservice.repository.ServiceTemplateRepository;
import com.gearup.templateservice.messaging.TemplateEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceTemplateService Unit Tests")
class ServiceTemplateServiceTest {

    @Mock
    private ServiceTemplateRepository repository;
    @Mock
    private TemplateEventPublisher eventPublisher;

    @InjectMocks
    private ServiceTemplateService service;

    private ServiceTemplateDto createDto;
    private ServiceTemplate existing;

    @BeforeEach
    void setUp() {
        createDto = ServiceTemplateDto.builder()
                .name("Deluxe Wash")
                .description("Full exterior and interior")
                .price(new BigDecimal("49.99"))
                .durationMinutes(90)
                .active(true)
                .build();

        existing = new ServiceTemplate();
        existing.setId(1L);
        existing.setName("Deluxe Wash");
        existing.setDescription("Full exterior and interior");
        existing.setPrice(new BigDecimal("49.99"));
        existing.setDurationMinutes(90);
        existing.setActive(true);
    }

    @Test
    @DisplayName("Create template publishes created event")
    void create_PublishesEvent() {
        when(repository.existsByNameIgnoreCase("Deluxe Wash")).thenReturn(false);
        when(repository.save(any(ServiceTemplate.class))).thenAnswer(invocation -> {
            ServiceTemplate saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ServiceTemplateDto result = service.create(createDto, "admin-uid");

        assertThat(result.getId()).isEqualTo(10L);
        ArgumentCaptor<ServiceTemplateDto> dtoCaptor = ArgumentCaptor.forClass(ServiceTemplateDto.class);
        verify(eventPublisher).publishCreated(dtoCaptor.capture(), eq("admin-uid"));
        assertThat(dtoCaptor.getValue().getName()).isEqualTo("Deluxe Wash");
    }

    @Test
    @DisplayName("Update template publishes updated event")
    void update_PublishesEvent() {
        ServiceTemplateDto updateDto = ServiceTemplateDto.builder()
                .name("Deluxe Wash")
                .description("Updated desc")
                .price(new BigDecimal("59.99"))
                .durationMinutes(95)
                .active(true)
                .build();

    when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(ServiceTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceTemplateDto result = service.update(1L, updateDto, "admin-uid");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("59.99"));
        verify(eventPublisher).publishUpdated(any(ServiceTemplateDto.class), eq("admin-uid"));
    }

    @Test
    @DisplayName("Delete template publishes deleted event")
    void delete_PublishesEvent() {
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        service.delete(1L, "admin-uid");
        verify(eventPublisher).publishDeleted(eq(1L), eq("Deluxe Wash"), eq("admin-uid"));
    }

    @Test
    @DisplayName("FindById throws when missing")
    void findById_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Template not found");
    }
}
