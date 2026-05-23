package com.nikhil.project.uber.uberApp.services.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class EmailSenderServiceImplTest {

    private final JavaMailSender javaMailSender = mock(JavaMailSender.class);
    private final EmailSenderServiceImpl emailSenderService = new EmailSenderServiceImpl(javaMailSender);

    @Test
    void sendEmail_toSingleRecipient_buildsAndSendsMessage() {
        emailSenderService.sendEmail("to@example.com", "Subject", "Body");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).containsExactly("to@example.com");
        assertThat(captor.getValue().getSubject()).isEqualTo("Subject");
        assertThat(captor.getValue().getText()).isEqualTo("Body");
    }

    @Test
    void sendEmail_toMultipleRecipients_buildsBccMessage() {
        String[] recipients = {"a@example.com", "b@example.com"};

        emailSenderService.sendEmail(recipients, "Subject", "Body");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());
        assertThat(captor.getValue().getBcc()).containsExactly(recipients);
        assertThat(captor.getValue().getTo()).isNull();
    }

    @Test
    void sendEmail_whenMailSenderThrows_doesNotPropagateException() {
        doThrow(new RuntimeException("smtp down")).when(javaMailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailSenderService.sendEmail("to@example.com", "Subject", "Body"));
    }
}
