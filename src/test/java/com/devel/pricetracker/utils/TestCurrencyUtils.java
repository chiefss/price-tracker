package com.devel.pricetracker.utils;


import com.devel.pricetracker.application.utils.CurrencyUtils;
import javassist.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

@SpringBootTest
@ActiveProfiles("tests")
@Sql("/db.tests.data/init.sql")
public class TestCurrencyUtils {

    @Test
    public void testFormatCurrency() {
        String currency = CurrencyUtils.formatCurrency(123456.789f);
        Assertions.assertEquals("123 456,79", currency);
    }

    @Test
    public void testGetCurrencySubstring() throws IOException {
        Assertions.assertThrows(IOException.class, () -> CurrencyUtils.getCurrencySubstring("f asdf c"));
        Float currency;
        currency = CurrencyUtils.getCurrencySubstring("f 123456 c");
        Assertions.assertEquals(123456f, currency);
        currency = CurrencyUtils.getCurrencySubstring(" 123 456 ");
        Assertions.assertEquals(123456f, currency);
        currency = CurrencyUtils.getCurrencySubstring("f 123.456 c");
        Assertions.assertEquals(123.456f, currency);
        currency = CurrencyUtils.getCurrencySubstring("f 123,456 c");
        Assertions.assertEquals(123.456f, currency);
        currency = CurrencyUtils.getCurrencySubstring("f 12 3,45.6 c");
        Assertions.assertEquals(12345.6f, currency);
        currency = CurrencyUtils.getCurrencySubstring("f 12,3,45.6 c");
        Assertions.assertEquals(12345.6f, currency);
        currency = CurrencyUtils.getCurrencySubstring("f 12.3.45,6 c");
        Assertions.assertEquals(12345.6f, currency);
    }

}
