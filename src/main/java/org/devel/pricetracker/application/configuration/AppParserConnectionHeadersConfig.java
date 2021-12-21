package org.devel.pricetracker.application.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.parser.connection.headers")
public class AppParserConnectionHeadersConfig {

    private String userAgent;
    private String accept;
    private String acceptLanguage;
    private String acceptEncoding;
    private String dnt;
    private String connection;
    private String upgradeInsecureRequests;
    private String pragma;
    private String cacheControl;
}
