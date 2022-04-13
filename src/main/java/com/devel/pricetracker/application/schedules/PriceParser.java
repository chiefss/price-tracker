package com.devel.pricetracker.application.schedules;

import com.devel.pricetracker.application.dto.ItemPriceDto;
import com.devel.pricetracker.application.dto.PriceParserResultDto;
import com.devel.pricetracker.application.factory.ItemPriceDtoFactory;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import com.devel.pricetracker.application.services.ItemPriceService;
import com.devel.pricetracker.application.services.MailService;
import com.devel.pricetracker.application.utils.CurrencyUtils;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;


@Component
@EnableScheduling
public class PriceParser {

    public static final String SUBJECT_DELIMITER = ", ";
    public static final String BODY_DELIMITER = "\n\n";
    public static final String SUCCESS_BODY_DELIMITER = "\n\n";
    public static final String SUCCESS_BODY_TITLE_DELIMITER = "\n\n";
    public static final String FAIL_BODY_DELIMITER = "\n\n";
    public static final String FAIL_BODY_TITLE_DELIMITER = "\n\n";

    public PriceParser(com.devel.pricetracker.application.parsers.PriceParser priceParser, ItemPriceService itemPriceService, MailService mailService) {
        this.priceParser = priceParser;
        this.itemPriceService = itemPriceService;
        this.mailService = mailService;
    }

    @Scheduled(fixedRateString = "${app.parser.cron.interval}", initialDelayString = "${app.parser.cron.initial-timeout}")
    public void parse() {
        if (appParserCronEnabled) {
            List<PriceParserResultDto> priceParserResultDtos = priceParser.parseAll();
            saveItemPriceAll(priceParserResultDtos);
            notify(priceParserResultDtos);
        }
    }

    private void saveItemPriceAll(List<PriceParserResultDto> priceParserResultDtos) {
        for (PriceParserResultDto priceParserResultDto : priceParserResultDtos) {
            if (priceParserResultDto.isSuccess()) {
                try {
                    saveItemPrice(priceParserResultDto);
                } catch (NotFoundException e) {
                    logger.error(String.format("An error occurred during parse all by cron and save item price for item with id \"%d\"", priceParserResultDto.getItem().getId()));
                }
            }
        }
    }

    private void saveItemPrice(PriceParserResultDto priceParserResultDto) throws NotFoundException {
        ItemPriceEntity itemPriceEntity = priceParserResultDto.getItemPrice();
        ItemPriceDto itemPriceDto = ItemPriceDtoFactory.create(itemPriceEntity.getItem().getId(), itemPriceEntity.getPrice());
        itemPriceService.create(itemPriceDto);
    }

    private void notify(List<PriceParserResultDto> priceParserResultDtos) {
        StringJoiner successBody = new StringJoiner(SUCCESS_BODY_DELIMITER);
        StringJoiner failBody = new StringJoiner(FAIL_BODY_DELIMITER);
        int successCount = 0;
        int failCount = 0;
        for (PriceParserResultDto priceParserResultDto : priceParserResultDtos) {
            ItemEntity itemEntity = priceParserResultDto.getItem();
            if (priceParserResultDto.isSuccess()) {
                List<ItemPriceEntity> itemPriceEntities = itemPriceService.findLast(itemEntity);
                if (isPriceReduced(itemPriceEntities)) {
                    Float itemPriceEntityCurrent = itemPriceEntities.get(0).getPrice();
                    Float itemPriceEntityPrev = itemPriceEntities.get(1).getPrice();
                    successBody.add(String.format("\"%s\" with id \"%d\" change price to %s (-%s) url \"%s\"",
                            itemEntity.getName(), itemEntity.getId(),
                            CurrencyUtils.formatCurrency(itemPriceEntityCurrent),
                            CurrencyUtils.formatCurrency(itemPriceEntityPrev - itemPriceEntityCurrent),
                            itemEntity.getUrl()));
                    successCount++;
                }
            } else {
                failBody.add(String.format("\"%s\" with id \"%d\", error: \"%s\"", itemEntity.getName(), itemEntity.getId(), priceParserResultDto.getMessage()));
                failCount++;
            }
        }
        if (successCount > 0 || failCount > 0) {
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
            mailService.sendAdmin(subject.toString(), body.toString());
        }
    }

    private boolean isPriceReduced(List<ItemPriceEntity> itemPriceEntities) {
        if (itemPriceEntities.size() > 1) {
            Float currentPrice = itemPriceEntities.get(0).getPrice();
            Float prevPrice = itemPriceEntities.get(1).getPrice();
            if (currentPrice < prevPrice) {
                return true;
            }
        }
        return false;
    }

    private String buildSuccessBody(StringJoiner successBody) {
        return String.join(SUCCESS_BODY_TITLE_DELIMITER, "Price reduced:", successBody.toString());
    }

    private String buildFailBody(StringJoiner failBody) {
        return String.join(FAIL_BODY_TITLE_DELIMITER, "Errors:", failBody.toString());
    }

    private final com.devel.pricetracker.application.parsers.PriceParser priceParser;

    private final ItemPriceService itemPriceService;

    private final MailService mailService;

    @Value("${app.parser.cron.enabled}")
    private boolean appParserCronEnabled;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}
