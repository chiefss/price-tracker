package org.devel.pricetracker.application.controllers.web;

import javassist.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.devel.pricetracker.application.dto.*;
import org.devel.pricetracker.application.entities.Item;
import org.devel.pricetracker.application.entities.ItemPrice;
import org.devel.pricetracker.application.factory.ItemDtoViewFactory;
import org.devel.pricetracker.application.factory.ItemPriceDtoFactory;
import org.devel.pricetracker.application.factory.ItemPriceDtoViewFactory;
import org.devel.pricetracker.application.parsers.PriceParser;
import org.devel.pricetracker.application.services.ItemPriceService;
import org.devel.pricetracker.application.services.ItemService;
import org.devel.pricetracker.application.utils.Defines;
import org.devel.pricetracker.application.utils.CurrencyUtils;
import org.jsoup.HttpStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@Controller
public class IndexWebController {

    private final ItemService itemService;

    private final ItemPriceService itemPriceService;

    private final PriceParser priceParser;

    @Autowired
    public IndexWebController(ItemService itemService, ItemPriceService itemPriceService, PriceParser priceParser) {
        this.itemService = itemService;
        this.itemPriceService = itemPriceService;
        this.priceParser = priceParser;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<ItemDtoView> itemDtoViews = getItemDtos();
        model.addAttribute("items", itemDtoViews);
        return "index";
    }

    @GetMapping("/view/{id}")
    public String viewById(Model model, @PathVariable Long id) throws NotFoundException {
        Item item = itemService.find(id);
        ItemDtoView itemDtoView = getItemDto(item);
        List<ItemPriceDtoView> itemPriceDtoViews = itemPriceService.findAll(item)
                .stream()
                .limit(Defines.DETAIL_PRICES_LIMIT)
                .map(ItemPriceDtoViewFactory::create)
                .toList();
        itemPriceDtoViews.forEach(itemPriceDto -> itemPriceDto.setFormattedPrice(CurrencyUtils.formatCurrency(itemPriceDto.getPrice())));
        List<ItemDtoView> itemDtoViews = getItemDtos();
        model.addAttribute("item", itemDtoView);
        model.addAttribute("prices", itemPriceDtoViews);
        model.addAttribute("items", itemDtoViews);
        return "index";
    }

    @PostMapping("/create")
    public String createItem(@RequestParam String name, @RequestParam String url,
                             @RequestParam String selector, @RequestParam(value = "break_selector") String breakSelector,
                             @RequestParam(name = "parse_now", required = false) boolean parseNow) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setUrl(url);
        itemDto.setSelector(selector);
        itemDto.setBreakSelector(breakSelector);
        Item createdItem = itemService.create(itemDto);
        if (createdItem != null && parseNow) {
            try {
                createItemPrice(createdItem);
            } catch (NotFoundException | IOException e) {
                log.error(String.format("Cannot parse price %s: %s", createdItem.getUrl(), e.getMessage()));
            }
        }
        return redirectTo(String.format("/view/%d", createdItem.getId()));
    }

    @PostMapping("/update")
    public String updateItem(@RequestParam Long id, @RequestParam String name, @RequestParam String url,
                             @RequestParam String selector, @RequestParam(value = "break_selector") String breakSelector,
                             @RequestParam(name = "parse_now", required = false) boolean parseNow) throws NotFoundException {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(id);
        itemDto.setName(name);
        itemDto.setUrl(url);
        itemDto.setSelector(selector);
        itemDto.setBreakSelector(breakSelector);
        Item updatedItem = itemService.update(itemDto);
        if (parseNow) {
            try {
                createItemPrice(updatedItem);
            } catch(HttpStatusException e) {
                log.error(e.getMessage());
            } catch (NotFoundException | IOException e) {
                log.error(String.format("Cannot parse price %s: %s", updatedItem.getUrl(), e.getMessage()));
            }
        }
        return redirectTo(String.format("/view/%d", itemDto.getId()));
    }

    @PostMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id) throws NotFoundException {
        itemService.delete(id);
        return redirectToRoot();
    }

    @PostMapping("/activate/{id}")
    public String activateItem(@PathVariable Long id) throws NotFoundException {
        itemService.activate(id);
        return redirectToRoot();
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateItem(@PathVariable Long id) throws NotFoundException {
        itemService.deactivate(id);
        return redirectToRoot();
    }

    @GetMapping("/prices/clean/{id}")
    public String cleanPricesByItemId(@PathVariable Long id) throws NotFoundException {
        Item item = itemService.find(id);
        cleanPriceDuplicates(item);
        return String.format("redirect:/view/%d", item.getId());
    }

    @GetMapping("/prices/cleanall")
    public String cleanPricesAll() {
        List<Item> itemEntities = itemService.findAll(false);
        for (Item item : itemEntities) {
            cleanPriceDuplicates(item);
        }
        return redirectToRoot();
    }

    @GetMapping("/parseAll")
    public String parseAll() {
        List<PriceParserResultDto> priceParserResultDtos = priceParser.parseAll();
        for (PriceParserResultDto priceParserResultDto : priceParserResultDtos) {
            if (priceParserResultDto.isSuccess()) {
                ItemPrice itemPrice = priceParserResultDto.getItemPrice();
                ItemPriceDto itemPriceDto = ItemPriceDtoFactory.create(itemPrice.getItem().getId(), itemPrice.getPrice());
                try {
                    itemPriceService.create(itemPriceDto);
                } catch (NotFoundException e) {
                    log.error(String.format("Cannot parse all and save item price for item id \"%d\"", itemPrice.getItem().getId()));
                }
            }
        }
        return redirectToRoot();
    }

    @GetMapping("/favicon.ico")
    @ResponseBody
    public void returnNoFavicon() {

    }

    private void createItemPrice(Item createdItem) throws IOException, NotFoundException {
        Optional<ItemPrice> itemPriceEntityOptional = priceParser.parse(createdItem);
        if (itemPriceEntityOptional.isPresent()) {
            ItemPrice itemPrice = itemPriceEntityOptional.get();
            ItemPriceDto itemPriceDto = ItemPriceDtoFactory.create(itemPrice.getItem().getId(), itemPrice.getPrice());
            itemPriceService.create(itemPriceDto);
        }
    }

    private void cleanPriceDuplicates(Item item) {
        List<ItemPrice> itemPriceEntities = itemPriceService.findAll(item);
        Double prevValue = null;
        for (ItemPrice itemPrice : itemPriceEntities) {
            Double currentValue = itemPrice.getPrice();
            if (currentValue.equals(prevValue)) {
                itemPriceService.delete(itemPrice.getId());
            }
            prevValue = currentValue;
        }
    }

    private List<ItemDtoView> getItemDtos() {
        List<Item> itemEntities = itemService.findAll(false);
        List<ItemDtoView> itemDtoViews = new ArrayList<>();
        for (Item item : itemEntities) {
            ItemDtoView itemDtoView = getItemDto(item);
            itemDtoViews.add(itemDtoView);
        }
        return itemDtoViews;
    }

    private ItemDtoView getItemDto(Item item) {
        ItemDtoView itemDtoView = ItemDtoViewFactory.create(item);
        List<ItemPrice> itemPriceEntities = itemPriceService.findLast(item);

        String formatedLastPrice = CurrencyUtils.formatCurrency(getLastPrice(itemPriceEntities));
        itemDtoView.setFormattedLastPrice(formatedLastPrice);

        Double delta = getDelta(itemPriceEntities);
        itemDtoView.setDelta(delta);
        itemDtoView.setFormattedDelta(CurrencyUtils.formatCurrency(Math.abs(delta)));

        try {
            URL url = new URL(item.getUrl());
            itemDtoView.setHost(url.getHost());
        } catch (MalformedURLException e) {
            log.debug(String.format("Cannot parse host by item entity with url \"%s\"", item.getUrl()));
        }

        return itemDtoView;
    }

    private Double getDelta(List<ItemPrice> itemPriceEntities) {
        double lastPricesValue = 0.0;
        int itemPriceCount = itemPriceEntities.size();
        if (itemPriceCount == 2) {
            Double todayPrice = itemPriceEntities.get(0).getPrice();
            Double yesterdayPrice = itemPriceEntities.get(1).getPrice();
            if (!todayPrice.equals(yesterdayPrice)) {
                lastPricesValue = todayPrice - yesterdayPrice;
            }
        }
        return lastPricesValue;
    }

    private Double getLastPrice(List<ItemPrice> itemPriceEntities) {
        if (itemPriceEntities.isEmpty()) {
            return 0.0;
        }
        return itemPriceEntities.get(0).getPrice();
    }

    private String redirectToRoot() {
        return redirectTo("/");
    }

    private String redirectTo(String url) {
        return "redirect:" + url;
    }
}
