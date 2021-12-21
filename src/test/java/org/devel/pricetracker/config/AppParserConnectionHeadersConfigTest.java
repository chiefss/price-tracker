package org.devel.pricetracker.config;

import org.devel.pricetracker.AbstractFunctionalSpringBootTest;
import org.devel.pricetracker.application.configuration.AppParserConnectionHeadersConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AppParserConnectionHeadersConfigTest extends AbstractFunctionalSpringBootTest {

    @Autowired
    private AppParserConnectionHeadersConfig appParserConnectionHeadersConfig;

    @Test
    void testConfig() {
        Assertions.assertNotNull(appParserConnectionHeadersConfig.getUserAgent());
        Assertions.assertNotNull(appParserConnectionHeadersConfig.getAccept());
        Assertions.assertNotNull(appParserConnectionHeadersConfig.getAcceptLanguage());
        Assertions.assertNotNull(appParserConnectionHeadersConfig.getAcceptLanguage());
        Assertions.assertNotNull(appParserConnectionHeadersConfig.getDnt());
        Assertions.assertNotNull(appParserConnectionHeadersConfig.getConnection());
        Assertions.assertNotNull(appParserConnectionHeadersConfig.getUpgradeInsecureRequests());
        Assertions.assertNotNull(appParserConnectionHeadersConfig.getPragma());
        Assertions.assertNotNull(appParserConnectionHeadersConfig.getCacheControl());
    }
}
