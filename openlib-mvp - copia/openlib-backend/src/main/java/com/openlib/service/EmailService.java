package com.openlib.service;

import com.openlib.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOrderConfirmation(Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(order.getBillingEmail());
        message.setSubject("Confirmación de pedido OpenLib #" + order.getId());
        message.setText(buildOrderBody(order));

        try {
            mailSender.send(message);
            log.info("Correo de confirmación enviado a {} para pedido {}", order.getBillingEmail(), order.getId());
        } catch (MailException ex) {
            log.error("No se pudo enviar el correo de confirmación al cliente {}: {}", order.getBillingEmail(), ex.getMessage());
            throw ex;
        }
    }

    private String buildOrderBody(Order order) {
        StringBuilder body = new StringBuilder();
        body.append("Hola ").append(order.getBillingFullName()).append(",\n\n");
        body.append("Tu pedido #").append(order.getId()).append(" se ha realizado correctamente. ");
        body.append("Gracias por comprar en OpenLib.\n\n");
        body.append("Resumen del pedido:\n");

        order.getItems().forEach(item ->
            body.append("- ")
                .append(item.getBook().getTitle())
                .append(" : $")
                .append(item.getPrice())
                .append("\n")
        );

        body.append("\nTotal: $").append(order.getTotalAmount()).append("\n");
        body.append("\nEntregaremos tu pedido digitalmente en breve.\n\n");
        body.append("Saludos,\nEl equipo de OpenLib");
        return body.toString();
    }
}
