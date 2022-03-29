package com.devel.pricetracker.application.parsers;

import com.devel.pricetracker.application.dto.PriceParserResultDto;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import com.devel.pricetracker.application.services.ItemPriceService;
import com.devel.pricetracker.application.services.ItemService;
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

    public PriceParserImpl(ItemService itemService, ItemPriceService itemPriceService) {
        this.itemService = itemService;
        this.itemPriceService = itemPriceService;
    }

    public PriceParserResultDto parseAll() {
        List<ItemEntity> itemEntities = itemService.findAll(true);
        PriceParserResultDto priceParserResultDto = new PriceParserResultDto();
        for (ItemEntity itemEntity : itemEntities) {
            try {
                boolean parsed = parse(itemEntity);
                if (parsed) {
                    List<ItemPriceEntity> itemPriceEntities = itemPriceService.findLast(itemEntity);
                    if (isPriceReduced(itemPriceEntities)) {
                        Float itemPriceEntityCurrent = itemPriceEntities.get(0).getPrice();
                        Float itemPriceEntityPrev = itemPriceEntities.get(1).getPrice();
                        priceParserResultDto.addReduceMessage(String.format("\"%s\" with id \"%d\" change price to %s (-%s) url \"%s\"",
                            itemEntity.getName(), itemEntity.getId(),
                            CurrencyUtils.formatCurrency(itemPriceEntityCurrent),
                            CurrencyUtils.formatCurrency(itemPriceEntityPrev - itemPriceEntityCurrent),
                            itemEntity.getUrl()));
                    }
                }
            } catch (NotFoundException | IOException e) {
                priceParserResultDto.addErrorMessage(String.format("\"%s\" with id \"%d\", error: \"%s\"", itemEntity.getName(), itemEntity.getId(), e.getMessage()));
            }
        }
        return priceParserResultDto;
    }

    public boolean parse(ItemEntity itemEntity) throws IOException, NotFoundException {
        try {
            int sleepTime = ThreadLocalRandom.current().nextInt(Constants.PARSER_THREAD_SLEEP_MIN_SECOND, Constants.PARSER_THREAD_SLEEP_MAX_SECOND + 1) * 1000;
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            logger.error(String.format("An error occurred during sleep parser thread: %s", e.getMessage()));
        }
        URL itemUrl = new URL(itemEntity.getUrl());
        Document itemDocument = loadContent(itemUrl);
        try {
            String priceValue = findDocumentPrice(itemDocument, itemEntity.getSelector());
            ItemPriceEntity itemPriceEntity = new ItemPriceEntity();
            itemPriceEntity.setItem(itemEntity);
            itemPriceEntity.setDateFrom(LocalDateTime.now());
            itemPriceEntity.setPrice(Float.valueOf(priceValue));
            if (isActualPrice(itemPriceEntity)) {
                itemPriceService.create(itemPriceEntity);
            }
        } catch (NotFoundException e) {
            String breakSelector = itemEntity.getBreakSelector();
            if (breakSelector != null && breakSelector.length() > 0 && findItemPriceBreak(itemDocument, breakSelector)) {
                logger.info(String.format("Item with id \"%d\" found break selector \"%s\"", itemEntity.getId(), breakSelector));
                return false;
            } else {
                throw e;
            }
        }
        return true;
    }

    private boolean isActualPrice(ItemPriceEntity itemPriceEntity) {
        List<ItemPriceEntity> itemPriceEntityList = itemPriceService.findLast(itemPriceEntity.getItem());
        if (itemPriceEntityList.size() == 0) {
            return true;
        }
        Float currentPrice = itemPriceEntity.getPrice();
        Float prevPrice = itemPriceEntityList.get(0).getPrice();
        if (!currentPrice.equals(prevPrice)) {
            return true;
        }
        return false;
    }

    private String findDocumentPrice(Document document, String selectors) throws NotFoundException {
        for (String selector : selectors.split("\\|")) {
            try {
                Element element = document.selectFirst(selector);
                if (element == null) {
                    throw new NotFoundException("Item price element not found");
                }
                String html = element.html();
                String priceValue = html.replaceAll("[^0-9,.]", "").replace(",", ".");
                logger.debug(String.format("Item price html \"%s\" and value \"%s\"", html, priceValue));
                return priceValue;
            } catch (NotFoundException e) {
                logger.debug(String.format("Item price element not found by selector \"%s\"", selector));
            }
        }
        throw new NotFoundException(String.format("Item price element not found by selectors \"%s\"", selectors));
    }

    private boolean findItemPriceBreak(Element item, String selector) {
        Element element = item.selectFirst(selector);
        return element != null;
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

    private Document loadContent(URL url) throws IOException {
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

    private final ItemService itemService;

    private final ItemPriceService itemPriceService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

}
