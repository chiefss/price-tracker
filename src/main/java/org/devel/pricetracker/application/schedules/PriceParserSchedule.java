package org.devel.pricetracker.application.schedules;

import javassist.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.devel.pricetracker.application.dto.EmailMessage;
import org.devel.pricetracker.application.dto.ItemPriceDto;
import org.devel.pricetracker.application.dto.PriceParserResultDto;
import org.devel.pricetracker.application.entities.Item;
import org.devel.pricetracker.application.entities.ItemPrice;
import org.devel.pricetracker.application.factory.ItemPriceDtoFactory;
import org.devel.pricetracker.application.parsers.PriceParser;
import org.devel.pricetracker.application.services.ItemPriceService;
import org.devel.pricetracker.application.services.MailService;
import org.devel.pricetracker.application.utils.Counter;
import org.devel.pricetracker.application.utils.CurrencyUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;


@Log4j2
@Component
@ConditionalOnProperty(name = "app.parser.cron.enabled", havingValue = "true")
public class PriceParserSchedule {

    public static final String SUBJECT_DELIMITER = ", ";
    public static final String SUCCESS_BODY_TITLE_DELIMITER = "\n\n";
    public static final String SUCCESS_BODY_DELIMITER = "\n\n";
    public static final String BODY_DELIMITER = "\n\n";
    public static final String FAIL_BODY_TITLE_DELIMITER = "\n\n";
    public static final String FAIL_BODY_DELIMITER = "\n\n";

    private final PriceParser priceParser;

    private final ItemPriceService itemPriceService;

    private final MailService mailService;

    public PriceParserSchedule(PriceParser priceParser, ItemPriceService itemPriceService, MailService mailService) {
        this.priceParser = priceParser;
        this.itemPriceService = itemPriceService;
        this.mailService = mailService;
    }

    @Scheduled(fixedRateString = "${app.parser.cron.interval}", initialDelayString = "${app.parser.cron.initial-timeout}")
    public void start() {
        List<PriceParserResultDto> priceParserResultDtos = priceParser.parseAll();
        saveItemPriceAll(priceParserResultDtos);
        notify(priceParserResultDtos);
    }

    private void saveItemPriceAll(List<PriceParserResultDto> priceParserResultDtos) {
        for (PriceParserResultDto priceParserResultDto : priceParserResultDtos) {
            if (!priceParserResultDto.isSuccess()) {
                continue;
            }
            try {
                saveItemPrice(priceParserResultDto);
            } catch (NotFoundException e) {
                log.error(String.format("Cannot parse all by cron and save item price for item with id \"%d\"", priceParserResultDto.getItem().getId()));
            }
        }
    }

    private void saveItemPrice(PriceParserResultDto priceParserResultDto) throws NotFoundException {
        ItemPrice itemPrice = priceParserResultDto.getItemPrice();
        ItemPriceDto itemPriceDto = ItemPriceDtoFactory.create(itemPrice.getItem().getId(), itemPrice.getPrice());
        itemPriceService.create(itemPriceDto);
    }

    private void notify(List<PriceParserResultDto> priceParserResultDtos) {
        StringJoiner successBody = new StringJoiner(SUCCESS_BODY_DELIMITER);
        StringJoiner failBody = new StringJoiner(FAIL_BODY_DELIMITER);
        Counter successCounter = new Counter();
        Counter failCounter = new Counter();
        for (PriceParserResultDto priceParserResultDto : priceParserResultDtos) {
            Item item = priceParserResultDto.getItem();
            if (priceParserResultDto.isSuccess()) {
                List<ItemPrice> itemPriceEntities = itemPriceService.findLast(item);
                if (!isPriceReduced(itemPriceEntities)) {
                    continue;
                }
                double itemPriceEntityCurrent = itemPriceEntities.get(0).getPrice();
                double itemPriceEntityPrev = itemPriceEntities.get(1).getPrice();
                successBody.add(String.format("\"%s\" with id \"%d\" change price to %s (-%s) url \"%s\"",
                        item.getName(), item.getId(),
                        CurrencyUtils.formatCurrency(itemPriceEntityCurrent),
                        CurrencyUtils.formatCurrency(itemPriceEntityPrev - itemPriceEntityCurrent),
                        item.getUrl()));
                successCounter.increase();
            } else {
                failBody.add(String.format("\"%s\" with id \"%d\", error: \"%s\"", item.getName(), item.getId(), priceParserResultDto.getMessage()));
                failCounter.increase();
            }
        }
        int successCount = successCounter.getCount();
        int failCount = failCounter.getCount();
        if (successCount > 0 || failCount > 0) {
            EmailMessage emailMessage = buildEmailMessage(successCount, successBody, failCount, failBody);
            mailService.sendAdmin(emailMessage.getSubject(), emailMessage.getBody());
        }
    }

    private EmailMessage buildEmailMessage(int successCount, StringJoiner successBody, int failCount, StringJoiner failBody) {
        StringJoiner subject = new StringJoiner(SUBJECT_DELIMITER);
        StringJoiner body = new StringJoiner(BODY_DELIMITER);
        subject.add(String.format("[Price tracker] Parser reporting. Reduced %d", successCount));
        if (successCount > 0) {
            body.add(buildSuccessBody(successBody));
        }
        if (failCount > 0) {
            subject.add(String.format("errors %d", failCount));
            body.add(buildFailBody(failBody));
        }
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setSubject(subject.toString());
        emailMessage.setBody(body.toString());
        return emailMessage;
    }

    private boolean isPriceReduced(List<ItemPrice> itemPriceEntities) {
        if (itemPriceEntities.isEmpty()) {
            return false;
        }
        double currentPrice = itemPriceEntities.get(0).getPrice();
        double prevPrice = itemPriceEntities.get(1).getPrice();
        return currentPrice < prevPrice;
    }

    private String buildSuccessBody(StringJoiner successBody) {
        return String.join(SUCCESS_BODY_TITLE_DELIMITER, "Price reduced:", successBody.toString());
    }

    private String buildFailBody(StringJoiner failBody) {
        return String.join(FAIL_BODY_TITLE_DELIMITER, "Errors:", failBody.toString());
    }
}
