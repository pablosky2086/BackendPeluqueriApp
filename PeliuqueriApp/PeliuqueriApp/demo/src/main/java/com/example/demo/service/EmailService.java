package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    // --- CORREO 1: EL ENLACE ---
    public void enviarEnlaceConfirmacion(String emailDestino, String token) {
        String enlace =  baseUrl + "/api/auth/reset-password?token=" + token;
        String asunto = "Confirma tu solicitud de nueva contraseña";
        String html = """
                <h2>¿Has pedido una nueva contraseña?</h2>
                <p>Para generar una contraseña nueva automáticamente, pulsa el botón:</p>
                <a href='%s' style='background:blue; color:white; padding:10px;'>GENERAR NUEVA CLAVE</a>
                <p>Si no fuiste tú, ignora este mensaje.</p>
                """.formatted(enlace);

        enviar(emailDestino, asunto, html);
    }

    // --- CORREO 2: LA NUEVA CONTRASEÑA ---
    public void enviarNuevaPassword(String emailDestino, String passProvisional) {
        String asunto = "Aquí tienes tu nueva contraseña";
        String html = """
                <h2>Contraseña Restablecida</h2>
                <p>Tu nueva contraseña provisional es:</p>
                <h1 style='background:#eee; padding:10px;'>%s</h1>
                <p>Por favor, entra y cámbiala lo antes posible.</p>
                """.formatted(passProvisional);

        enviar(emailDestino, asunto, html);
    }

    // --- NUEVO MÉTODO: AVISO CANCELACIÓN ---
    public void enviarAvisoCancelacion(String email, String nombre, String motivo, String fechaHora) {
        String asunto = "AVISO URGENTE: Incidencia en su cita - PeluqueriApp";
        String html = """
            <div style='font-family: sans-serif; border: 1px solid #ccc; padding: 20px; border-radius: 8px;'>
                <h2 style='color: #d32f2f;'>Su cita ha sido afectada</h2>
                <p>Hola <strong>%s</strong>,</p>
                <p>Le informamos que su cita programada para el <strong>%s</strong> no podrá realizarse por una incidencia en el centro.</p>
                <p style='background-color: #f8d7da; padding: 10px; border-radius: 4px; color: #721c24;'>
                    <strong>Motivo:</strong> %s
                </p>
                <p>La cita sigue registrada en su historial, pero le rogamos que 
                   <strong>no acuda al centro</strong> y reserve una nueva fecha disponible.</p>
                <p>Sentimos las molestias.</p>
            </div>
            """.formatted(nombre, fechaHora, motivo);

        enviar(email, asunto, html);
    }

    private void enviar(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            // Tu truco para no recibir respuestas
            helper.setReplyTo("no-reply@tucorreo.com");
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error enviando correo", e);
        }
    }
}