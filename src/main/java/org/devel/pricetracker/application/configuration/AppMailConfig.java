package org.devel.pricetracker.application.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.mail")
public class AppMailConfig {

    private boolean enabled;
    private boolean debug;
    private String admin;
    private String from;
    private String host;
    private String username;
    private String password;
    @Value("${app.mail.properties.transport.protocol}")
    private String propertiesTransportProtocol;
    @Value("${app.mail.properties.smtp.port}")
    private int propertiesSmtpPort;
    @Value("${app.mail.properties.smtp.connectiontimeout}")
    private int propertiesSmtpConnectiontimeout;
    @Value("${app.mail.properties.smtp.timeout}")
    private int propertiesSmtpTimeout;
    @Value("${app.mail.properties.smtp.writetimeout}")
    private int propertiesSmtpWritetimeout;
    @Value("${app.mail.properties.smtp.auth}")
    private boolean propertiesSmtpAuth;
    @Value("${app.mail.properties.smtp.starttls.enable}")
    private boolean propertiesSmtpStarttlsEnable;
    @Value("${app.mail.properties.smtp.starttls.required}")
    private boolean propertiesSmtpStarttlsRequired;
}
