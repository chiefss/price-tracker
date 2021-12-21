package org.devel.pricetracker.config;

import org.devel.pricetracker.AbstractFunctionalSpringBootTest;
import org.devel.pricetracker.application.configuration.AppMailConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AppMailConfigTest extends AbstractFunctionalSpringBootTest {

    @Autowired
    private AppMailConfig appMailConfig;

    @Test
    void testConfig() {
        Assertions.assertFalse(appMailConfig.isEnabled());
        Assertions.assertFalse(appMailConfig.isDebug());
        Assertions.assertNotNull(appMailConfig.getAdmin());
        Assertions.assertNotNull(appMailConfig.getFrom());
        Assertions.assertNotNull(appMailConfig.getHost());
        Assertions.assertNotNull(appMailConfig.getUsername());
        Assertions.assertNotNull(appMailConfig.getPassword());
        Assertions.assertNotNull(appMailConfig.getPropertiesTransportProtocol());
        Assertions.assertTrue(appMailConfig.getPropertiesSmtpPort() > 0);
        Assertions.assertTrue(appMailConfig.getPropertiesSmtpConnectiontimeout() > 0);
        Assertions.assertTrue(appMailConfig.getPropertiesSmtpTimeout() > 0);
        Assertions.assertTrue(appMailConfig.getPropertiesSmtpWritetimeout() > 0);
        Assertions.assertTrue(appMailConfig.isPropertiesSmtpAuth());
        Assertions.assertTrue(appMailConfig.isPropertiesSmtpStarttlsEnable());
        Assertions.assertTrue(appMailConfig.isPropertiesSmtpStarttlsRequired());
    }
}
