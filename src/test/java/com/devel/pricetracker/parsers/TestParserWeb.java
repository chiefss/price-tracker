package com.devel.pricetracker.parsers;


import com.devel.pricetracker.application.parsers.PriceParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
//@ActiveProfiles("tests")
public class TestParserWeb {

    @Autowired
    public TestParserWeb(PriceParser priceParser) {
        this.priceParser = priceParser;
    }

//    @Test
    public void testParse() {
        priceParser.parseAll();
    }

    private final PriceParser priceParser;
}
