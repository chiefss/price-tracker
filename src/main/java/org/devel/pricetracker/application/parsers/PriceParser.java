package org.devel.pricetracker.application.parsers;

import javassist.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.devel.pricetracker.application.configuration.AppParserConnectionHeadersConfig;
import org.devel.pricetracker.application.dto.PriceParserResultDto;
import org.devel.pricetracker.application.entities.Item;
import org.devel.pricetracker.application.entities.ItemPrice;
import org.devel.pricetracker.application.services.ItemPriceService;
import org.devel.pricetracker.application.services.ItemService;
import org.devel.pricetracker.application.utils.Defines;
import org.devel.pricetracker.application.utils.CurrencyUtils;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;


@Log4j2
@Component
public class PriceParser {

    @Autowired
    private AppParserConnectionHeadersConfig appParserConnectionHeadersConfig;

    public static final int MAX_THREADS = 8;

    public static final String SELECTOR_SPLIT_DELIMITER = "\\|";

    private final ItemService itemService;

    private final ItemPriceService itemPriceService;

    public PriceParser(ItemService itemService, ItemPriceService itemPriceService) {
        this.itemService = itemService;
        this.itemPriceService = itemPriceService;
    }

    public List<PriceParserResultDto> parseAll() {
        List<Item> itemEntities = itemService.findAll(true);
        List<PriceParserResultDto> priceParserResultDtos = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);
        List<Callable<Optional<PriceParserResultDto>>> tasks = new ArrayList<>();
        for (Item item : itemEntities) {
            Callable<Optional<PriceParserResultDto>> parserTask = createParserTask(item);
            tasks.add(parserTask);
        }
        try {
            List<Future<Optional<PriceParserResultDto>>> futures = pool.invokeAll(tasks);
            for (Future<Optional<PriceParserResultDto>> future : futures) {
                future.get().ifPresent(priceParserResultDtos::add);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error(String.format("Cannot parse all items: \"%s\"", e.getMessage()));
        } finally {
            pool.shutdown();
        }
        return priceParserResultDtos;
    }

    public Optional<ItemPrice> parse(Item item) throws IOException, NotFoundException {
        sleepThread();
        URL itemUrl = new URL(item.getUrl());
        Document itemDocument = loadContent(itemUrl);
        try {
            Double priceValue = findItemPriceBySelectors(itemDocument, item.getSelector());
            ItemPrice itemPrice = buildParsedItemPriceEntity(item, priceValue);
            if (isActualPrice(itemPrice)) {
                return Optional.of(itemPrice);
            }
        } catch (NotFoundException e) {
            String breakSelector = item.getBreakSelector();
            if (breakSelector != null && breakSelector.length() > 0 && findItemPriceBreak(itemDocument, breakSelector)) {
                log.info(String.format("Item with id \"%d\" found break selector \"%s\"", item.getId(), breakSelector));
            } else {
                throw e;
            }
        }
        return Optional.empty();
    }

    private void sleepThread() {
        try {
            int sleepTime = ThreadLocalRandom.current().nextInt(Defines.PARSER_THREAD_SLEEP_MIN_SECOND, Defines.PARSER_THREAD_SLEEP_MAX_SECOND + 1) * 1000;
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            log.error(String.format("Cannot sleep parser thread: %s", e.getMessage()));
        }
    }

    private ItemPrice buildParsedItemPriceEntity(Item item, Double priceValue) {
        ItemPrice itemPrice = new ItemPrice();
        itemPrice.setItem(item);
        itemPrice.setPrice(priceValue);
        return itemPrice;
    }

    private boolean isActualPrice(ItemPrice itemPrice) {
        List<ItemPrice> itemPriceList = itemPriceService.findLast(itemPrice.getItem());
        if (itemPriceList.isEmpty()) {
            return true;
        }
        Double currentPrice = itemPrice.getPrice();
        Double prevPrice = itemPriceList.get(0).getPrice();
        return !currentPrice.equals(prevPrice);
    }

    private Double findItemPriceBySelectors(Document document, String selectors) throws NotFoundException {
        for (String selector : selectors.split(SELECTOR_SPLIT_DELIMITER)) {
            Optional<Double> priceValueOptional = findItemPriceBySelector(document, selector);
            if (priceValueOptional.isPresent()) {
                return priceValueOptional.get();
            }
        }
        throw new NotFoundException(String.format("Item price element not found by selectors \"%s\"", selectors));
    }

    private Optional<Double> findItemPriceBySelector(Document document, String selector) {
        try {
            Element element = document.selectFirst(selector);
            if (element == null) {
                throw new NotFoundException("Item price element not found");
            }
            String html = element.html();
            double priceValue = CurrencyUtils.getCurrencySubstring(html);
            log.debug(String.format("Item price html \"%s\" and value \"%f\"", html, priceValue));
            return Optional.of(priceValue);
        } catch (NotFoundException | IOException e) {
            log.debug(String.format("Item price element not found by selector \"%s\"", selector));
        }
        return Optional.empty();
    }

    private boolean findItemPriceBreak(Element item, String selector) {
        Element element = item.selectFirst(selector);
        return element != null;
    }

    private Document loadContent(URL url) throws IOException {
        Connection connection = getConnection(url);
        Connection.Response response = connection.execute();
        int statusCode = response.statusCode();
        if (statusCode == 200) {
            return response.parse();
        }
        throw new HttpStatusException("Cannot load page", statusCode, url.toString());
    }

    private Connection getConnection(URL url) {
        return Jsoup
                .connect(url.toString())
                .header("Host", url.getHost())
                .header("User-Agent", appParserConnectionHeadersConfig.getUserAgent())
                .header("Accept", appParserConnectionHeadersConfig.getAccept())
                .header("Accept-Language", appParserConnectionHeadersConfig.getAcceptLanguage())
                .header("Accept-Encoding", appParserConnectionHeadersConfig.getAcceptEncoding())
                .header("DNT", appParserConnectionHeadersConfig.getDnt())
                .header("Connection", appParserConnectionHeadersConfig.getConnection())
                .header("Upgrade-Insecure-Requests", appParserConnectionHeadersConfig.getUpgradeInsecureRequests())
                .header("Pragma", appParserConnectionHeadersConfig.getPragma())
                .header("Cache-Control", appParserConnectionHeadersConfig.getCacheControl());
    }

    private Callable<Optional<PriceParserResultDto>> createParserTask(Item item) {
        return () -> {
            try {
                Optional<ItemPrice> itemPriceEntityOptional = parse(item);
                if (itemPriceEntityOptional.isPresent()) {
                    return Optional.of(new PriceParserResultDto(item, itemPriceEntityOptional.get(), true, null));
                }
            } catch (NotFoundException | IOException e) {
                return Optional.of(new PriceParserResultDto(item, null, false, e.getMessage()));
            }
            return Optional.empty();
        };
    }
}
