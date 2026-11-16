package com.clinica.recordatorios.channel.gateway;

import org.springframework.stereotype.Component;

@Component
public class TwilioNotificadorSmsGateway implements NotificadorSmsGateway {

    @Override
    public void enviarSms(String numeroDestino, String texto) {
        // En produccion: llamada HTTP a la API del proveedor de SMS. Se simula para no
        // depender de credenciales externas ni de red.
        System.out.printf("[SMS a %s] %s%n", numeroDestino, texto);
    }
}
