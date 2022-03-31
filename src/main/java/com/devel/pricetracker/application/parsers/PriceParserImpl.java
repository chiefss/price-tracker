package com.devel.pricetracker.application.parsers;

import com.devel.pricetracker.application.dto.ItemPriceDto;
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
import java.util.*;
import java.util.concurrent.*;


@Component
public class PriceParserImpl implements PriceParser {

    public static final String SELECTOR_SPLIT_DELIMITER = "\\|";

    public PriceParserImpl(ItemService itemService, ItemPriceService itemPriceService) {
        this.itemService = itemService;
        this.itemPriceService = itemPriceService;
    }

    public List<PriceParserResultDto> parseAll() {
        List<ItemEntity> itemEntities = itemService.findAll(true);
        List<PriceParserResultDto> priceParserResultDtos = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(Constants.PARSER_MAX_THREADS);
        List<Callable<Optional<PriceParserResultDto>>> tasks = new ArrayList<>();
        for (ItemEntity itemEntity : itemEntities) {
            Callable callable = new Callable() {
                @Override
                public Optional<PriceParserResultDto> call()  {
                    try {
                        Optional<ItemPriceEntity> itemPriceEntityOptional = parse(itemEntity);
                        if (itemPriceEntityOptional.isPresent()) {
                            ItemPriceEntity itemPriceEntity = itemPriceEntityOptional.get();
                            ItemPriceDto itemPriceDto = new ItemPriceDto();
                            itemPriceDto.setItemId(itemPriceEntity.getItem().getId());
                            itemPriceDto.setPrice(itemPriceEntity.getPrice());
                            itemPriceService.create(itemPriceDto);
                            return Optional.of(new PriceParserResultDto(itemEntity, true, null));
                        }
                    } catch (NotFoundException | IOException e) {
                        return Optional.of(new PriceParserResultDto(itemEntity, false, e.getMessage()));
                    }
                    return Optional.empty();
                }
            };
            tasks.add(callable);
        }
        try {
            List<Future<Optional<PriceParserResultDto>>> futures = pool.invokeAll(tasks);
            for (Future<Optional<PriceParserResultDto>> future : futures) {
                Optional<PriceParserResultDto> priceParserResultDtoOptional = future.get();
                if (priceParserResultDtoOptional.isPresent()) {
                    priceParserResultDtos.add(priceParserResultDtoOptional.get());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error(String.format("An error occurred during parse all items: \"%s\"", e.getMessage()));
        } finally {
            pool.shutdown();
        }
        return priceParserResultDtos;
    }

    public Optional<ItemPriceEntity> parse(ItemEntity itemEntity) throws IOException, NotFoundException {
        try {
            int sleepTime = ThreadLocalRandom.current().nextInt(Constants.PARSER_THREAD_SLEEP_MIN_SECOND, Constants.PARSER_THREAD_SLEEP_MAX_SECOND + 1) * 1000;
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            logger.error(String.format("An error occurred during sleep parser thread: %s", e.getMessage()));
        }
        URL itemUrl = new URL(itemEntity.getUrl());
        Document itemDocument = loadContent(itemUrl);
        try {
            Float priceValue = findItemPrice(itemDocument, itemEntity.getSelector());
            ItemPriceEntity itemPriceEntity = buildParsedItemPriceEntity(itemEntity, priceValue);
            if (isActualPrice(itemPriceEntity)) {
                return Optional.of(itemPriceEntity);
            }
        } catch (NotFoundException e) {
            String breakSelector = itemEntity.getBreakSelector();
            if (breakSelector != null && breakSelector.length() > 0 && findItemPriceBreak(itemDocument, breakSelector)) {
                logger.info(String.format("Item with id \"%d\" found break selector \"%s\"", itemEntity.getId(), breakSelector));
            } else {
                throw e;
            }
        }
        return Optional.empty();
    }

    private ItemPriceEntity buildParsedItemPriceEntity(ItemEntity itemEntity, Float priceValue) {
        ItemPriceEntity itemPriceEntity = new ItemPriceEntity();
        itemPriceEntity.setItem(itemEntity);
        itemPriceEntity.setPrice(priceValue);
        return itemPriceEntity;
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

    private Float findItemPrice(Document document, String selectors) throws NotFoundException {
        for (String selector : selectors.split(SELECTOR_SPLIT_DELIMITER)) {
            try {
                Element element = document.selectFirst(selector);
                if (element == null) {
                    throw new NotFoundException("Item price element not found");
                }
                String html = element.html();
                Float priceValue = CurrencyUtils.getCurrencySubstring(html);
                logger.debug(String.format("Item price html \"%s\" and value \"%f\"", html, priceValue));
                return priceValue;
            } catch (NotFoundException | IOException e) {
                logger.debug(String.format("Item price element not found by selector \"%s\"", selector));
            }
        }
        throw new NotFoundException(String.format("Item price element not found by selectors \"%s\"", selectors));
    }

    private boolean findItemPriceBreak(Element item, String selector) {
        Element element = item.selectFirst(selector);
        return element != null;
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
