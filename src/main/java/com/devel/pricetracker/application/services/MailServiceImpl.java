package com.devel.pricetracker.application.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class MailServiceImpl implements MailService {

    public void sendAdmin(String subject, String body) {
        this.sendMail(subject, body, admin);
    }

    public void sendMail(String subject, String body, String to) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.connectiontimeout", connectiontimeout);
        props.put("mail.smtp.timeout", timeout);
        props.put("mail.smtp.writetimeout", writetimeout);
        props.put("mail.smtp.starttls.enable", smtpEnable);
        props.put("mail.smtp.starttls.required", smtpRequired);
        props.put("mail.debug", mailDebug);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(from);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(body);
        simpleMailMessage.setTo(to);

        if (enabled) {
            mailSender.send(simpleMailMessage);
        }
    }

    @Value("${app.mail.enabled}")
    private boolean enabled;

    @Value("${app.mail.debug}")
    private boolean mailDebug;

    @Value("${app.mail.admin}")
    private String admin;

    @Value("${app.mail.from}")
    private String from;
    
    @Value("${app.mail.host}")
    private String host;

    @Value("${app.mail.username}")
    private String username;

    @Value("${app.mail.password}")
    private String password;

    @Value("${app.mail.properties.transport.protocol}")
    private String protocol;
    
    @Value("${app.mail.properties.smtp.port}")
    private int port;

    @Value("${app.mail.properties.smtp.connectiontimeout}")
    private int connectiontimeout;
    
    @Value("${app.mail.properties.smtp.timeout}")
    private int timeout;
    
    @Value("${app.mail.properties.smtp.writetimeout}")
    private int writetimeout;

    @Value("${app.mail.properties.smtp.auth}")
    private boolean smtpAuth;

    @Value("${app.mail.properties.smtp.starttls.enable}")
    private boolean smtpEnable;

    @Value("${app.mail.properties.smtp.starttls.required}")
    private boolean smtpRequired;
}
