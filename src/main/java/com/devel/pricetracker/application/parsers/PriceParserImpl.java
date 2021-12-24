package com.devel.pricetracker.application.parsers;

import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import com.devel.pricetracker.application.services.ItemPriceService;
import com.devel.pricetracker.application.services.ItemService;
import com.devel.pricetracker.application.services.MailService;
import com.devel.pricetracker.application.utils.Constants;
import com.devel.pricetracker.application.utils.CurrencyUtils;
import javassist.NotFoundException;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


@Component
public class PriceParserImpl implements PriceParser {

    public PriceParserImpl(ItemService itemService, ItemPriceService itemPriceService, MailService mailService) {
        this.itemService = itemService;
        this.itemPriceService = itemPriceService;
        this.mailService = mailService;
    }

    public void parseAll() {
        List<ItemEntity> itemEntities = itemService.findAll();
        List<String> reducedPriceMessages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        for (ItemEntity itemEntity : itemEntities) {
            try {
                boolean parsed = parse(itemEntity);
                if (parsed) {
                    List<ItemPriceEntity> itemPriceEntities = itemPriceService.findLast(itemEntity);
                    if (isPriceReduced(itemPriceEntities)) {
                        Float itemPriceEntityCurrent = itemPriceEntities.get(0).getPrice();
                        Float itemPriceEntityPrev = itemPriceEntities.get(1).getPrice();
                        reducedPriceMessages.add(String.format("\"%s\" with id \"%d\" change price to %s (-%s) url \"%s\"",
                            itemEntity.getName(), itemEntity.getId(),
                            CurrencyUtils.formatCurrency(itemPriceEntityCurrent),
                            CurrencyUtils.formatCurrency(itemPriceEntityPrev - itemPriceEntityCurrent),
                            itemEntity.getUrl()));
                    }
                }
            } catch (NotFoundException | IOException e) {
                errorMessages.add(String.format("\"%s\" with id \"%d\", error: \"%s\"", itemEntity.getName(), itemEntity.getId(), e.getMessage()));
            }
        }
        notify(reducedPriceMessages, errorMessages);
    }

    private void notify(List<String> reducedPriceMessages, List<String> errorMessages) {
        StringJoiner subject = new StringJoiner(", ");
        StringJoiner body = new StringJoiner("\n\n");
        subject.add(String.format("[Price tracker] Price reporting. Reduced %d", reducedPriceMessages.size()));
        if (reducedPriceMessages.size() > 0) {
            body.add(String.format("Reduced:\n\n%s", String.join("\n\n", reducedPriceMessages)));
        }
        if (errorMessages.size() > 0) {
            subject.add(String.format("errors %d", errorMessages.size()));
            body.add(String.format("Errors:\n\n%s", String.join("\n", errorMessages)));
        }
        mailService.sendAdmin(subject.toString(), body.toString());
    }

    public boolean parse(ItemEntity itemEntity) throws IOException, NotFoundException {
        String url = itemEntity.getUrl();
        try {
            int sleepTime = ThreadLocalRandom.current().nextInt(Constants.PARSER_THREAD_SLEEP_MIN_SECOND, Constants.PARSER_THREAD_SLEEP_MAX_SECOND + 1) * 1000;
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            logger.error(String.format("An error occurred during sleep parser thread: %s", e.getMessage()));
        }

        URL itemUrl = new URL(url);
        Document itemDocument = loadContent(itemUrl);
        try {
            String priceValue = findItemPrice(itemDocument, itemEntity);
            ItemPriceEntity itemPriceEntity = new ItemPriceEntity();
            itemPriceEntity.setItem(itemEntity);
            itemPriceEntity.setDateFrom(LocalDateTime.now());
            itemPriceEntity.setPrice(Float.valueOf(priceValue));
            itemPriceService.create(itemPriceEntity);
        } catch (NotFoundException e) {
            String breakSelector = itemEntity.getBreakSelector();
            if (breakSelector != null && findItemPriceBreak(itemDocument, breakSelector)) {
                logger.info(String.format("Item with id \"%s\" found break selector \"%s\"", itemEntity.getId(), breakSelector));
                return false;
            } else {
                throw e;
            }
        }
        return true;
    }

    protected Document loadContent(URL url) throws IOException {
        Connection connection = Jsoup
                .connect(url.toString())
                .header("Host", url.getHost())
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
                .header("Accept-Encoding", "gzip, deflate")
                .header("DNT", "1")
                .header("Connection", "keep-alive")
                .header("Cookie", "modtids=; modpids=; modtids=; modpids=; __cfduid=1; deskver=1; c-1=; c-1=; c-1=; c-1=")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Pragma", "no-cache")
                .header("Cache-Control", "no-cache");
        Connection.Response response = connection.execute();
        int statusCode = response.statusCode();
        if (statusCode == 200) {
            return response.parse();
        }
        throw new HttpStatusException("An error occurred during load page", statusCode, url.toString());
    }

    private String findItemPrice(Document itemDocument, ItemEntity itemEntity) throws NotFoundException {
        String itemEntitySelector = itemEntity.getSelector();
        for (String selector : itemEntitySelector.split("\\|")) {
            try {
                return findItemPrice(itemDocument, selector);
            } catch (NotFoundException e) {
                logger.debug(String.format("Item price element not found by selector \"%s\"", selector));
            }
        }
        throw new NotFoundException(String.format("Item price element not found by selectors \"%s\"", itemEntitySelector));
    }

    private String findItemPrice(Element item, String selector) throws NotFoundException {
        Element element = item.selectFirst(selector);
        if (element == null) {
            throw new NotFoundException("Item price element not found");
        }
        String html = element.html();
        return html.replaceAll("[^0-9]", "");
    }

    private boolean findItemPriceBreak(Element item, String selector) {
        Element element = item.selectFirst(selector);
        if (element == null) {
            return false;
        }
        return true;
    }

    private boolean isPriceReduced(List<ItemPriceEntity> itemPriceEntities) {
        if (itemPriceEntities.size() > 1) {
            ItemPriceEntity itemPriceEntityCurrent = itemPriceEntities.get(0);
            ItemPriceEntity itemPriceEntityPrev = itemPriceEntities.get(1);
            if (itemPriceEntityCurrent.getPrice() < itemPriceEntityPrev.getPrice()) {
                return true;
            }
        }
        return false;
    }

    private final ItemService itemService;

    private final ItemPriceService itemPriceService;

    private final MailService mailService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

}
