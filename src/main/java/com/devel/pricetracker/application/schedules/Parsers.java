package com.devel.pricetracker.application.schedules;

import com.devel.pricetracker.application.parsers.PriceParser;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@EnableScheduling
public class Parsers {

    public Parsers(PriceParser priceParser) {
        this.priceParser = priceParser;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void parse() {
        parseWeb();
    }

    private void parseWeb() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                priceParser.parseAll();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private final PriceParser priceParser;

}
