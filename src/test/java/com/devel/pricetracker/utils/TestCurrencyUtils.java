package com.devel.pricetracker.utils;


import com.devel.pricetracker.application.utils.CurrencyUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("tests")
@Sql("/db.tests.data/init.sql")
public class TestCurrencyUtils {

    @Test
    public void testFormatCurrency() {
        String currency = CurrencyUtils.formatCurrency(123456.789f);
        Assertions.assertEquals("123 456,79", currency);
    }

}
