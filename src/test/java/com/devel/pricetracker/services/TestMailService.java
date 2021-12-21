package com.devel.pricetracker.services;


import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import com.devel.pricetracker.application.models.repository.ItemPriceRepository;
import com.devel.pricetracker.application.services.ItemPriceService;
import com.devel.pricetracker.application.services.MailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("tests")
@Sql("/db.tests.data/init.sql")
public class TestMailService {

    @Autowired
    public TestMailService(MailService mailService) {
        this.mailService = mailService;
    }

//    @Test
    public void testSendAdmin() {
        mailService.sendAdmin("test", "test body");
    }

    private final MailService mailService;
}
