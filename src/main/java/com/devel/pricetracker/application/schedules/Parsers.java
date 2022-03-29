package com.devel.pricetracker.application.schedules;

import com.devel.pricetracker.application.dto.PriceParserResultDto;
import com.devel.pricetracker.application.parsers.PriceParser;
import com.devel.pricetracker.application.services.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;


@Component
@EnableScheduling
public class Parsers {

    public Parsers(PriceParser priceParser, MailService mailService) {
        this.priceParser = priceParser;
        this.mailService = mailService;
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3, initialDelay = 1000 * 60 * 60)
    public void parse() {
        if (appParserCronEnabled) {
            PriceParserResultDto priceParserResultDto = priceParser.parseAll();
            if (priceParserResultDto.hasReduceMessages() || priceParserResultDto.hasErrorMessages()) {
                notify(priceParserResultDto.getReduceMessages(), priceParserResultDto.getErrorMessages());
            }
        }
    }

    private void notify(List<String> reducedPriceMessages, List<String> errorMessages) {
        StringJoiner subject = new StringJoiner(", ");
        StringJoiner body = new StringJoiner("\n\n");
        subject.add(String.format("[Price tracker] Parser reporting. Reduced %d", reducedPriceMessages.size()));
        if (reducedPriceMessages.size() > 0) {
            body.add(String.format("Price reduced:\n\n%s", String.join("\n\n", reducedPriceMessages)));
        }
        if (errorMessages.size() > 0) {
            subject.add(String.format("errors %d", errorMessages.size()));
            body.add(String.format("Errors:\n\n%s", String.join("\n\n", errorMessages)));
        }
        mailService.sendAdmin(subject.toString(), body.toString());
    }

    private final PriceParser priceParser;

    private final MailService mailService;

    @Value("${app.parser.cron.enabled}")
    private boolean appParserCronEnabled;

}
