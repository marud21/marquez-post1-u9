package com.clinica.recordatorios;

import com.clinica.recordatorios.channel.gateway.NotificadorSmsGateway;
import com.clinica.recordatorios.domain.CanalNotificacion;
import com.clinica.recordatorios.domain.Cita;
import com.clinica.recordatorios.repository.CitaRepository;
import com.clinica.recordatorios.service.CitaReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class CitaReminderServiceIntegrationTest {

    @TestConfiguration
    static class ClockDeterministaConfig {
        // Nombre distinto de "clock": reutilizar el nombre del bean de ClockConfig provoca
        // BeanDefinitionOverrideException (Spring Boot no permite sobrescribir definiciones por defecto).
        @Bean
        @Primary
        public Clock clockFijo() {
            return Clock.fixed(Instant.parse("2026-08-10T09:00:00Z"), ZoneId.of("UTC"));
        }
    }

    @Autowired
    private CitaReminderService citaReminderService;

    @Autowired
    private CitaRepository citaRepository;

    // Mock solo del proveedor externo; la base de datos (H2) es real
    @MockBean
    private NotificadorSmsGateway notificadorSmsGateway;

    @BeforeEach
    void limpiar() {
        citaRepository.deleteAll();
    }

    @Test
    void enviarRecordatoriosPendientes_contextoCompletoDeSpring_persisteYNotificaCitasEnVentana() {
        citaRepository.save(new Cita(null, "Carla Ríos", "3011112222",
                LocalDateTime.of(2026, 8, 10, 18, 0), CanalNotificacion.SMS, false));
        citaRepository.save(new Cita(null, "Fuera de ventana", "3019998888",
                LocalDateTime.of(2026, 8, 12, 18, 0), CanalNotificacion.SMS, false));

        int enviados = citaReminderService.enviarRecordatoriosPendientes();

        assertEquals(1, enviados);
        verify(notificadorSmsGateway, times(1)).enviarSms(eq("3011112222"), anyString());
        assertTrue(citaRepository.findAll().stream()
                .filter(c -> c.getPacienteNombre().equals("Carla Ríos")).findFirst().orElseThrow()
                .isRecordatorioEnviado());
        assertFalse(citaRepository.findAll().stream()
                .filter(c -> c.getPacienteNombre().equals("Fuera de ventana")).findFirst().orElseThrow()
                .isRecordatorioEnviado());
    }

    @Test
    void enviarRecordatorioUrgente_beanCanalUrgenteDelComposicionRoot_usaElGatewayDeSms() {
        Cita cita = citaRepository.save(new Cita(null, "Pedro Díaz", "3205554444",
                LocalDateTime.of(2026, 8, 10, 10, 30), CanalNotificacion.EMAIL, false));

        citaReminderService.enviarRecordatorioUrgente(cita.getId());

        verify(notificadorSmsGateway).enviarSms(eq("3205554444"), contains("URGENTE"));
        assertTrue(citaRepository.findById(cita.getId()).orElseThrow().isRecordatorioEnviado());
    }
}
