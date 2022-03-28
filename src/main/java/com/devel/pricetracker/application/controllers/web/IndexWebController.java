package com.devel.pricetracker.application.controllers.web;

import com.devel.pricetracker.application.dto.ItemDtoView;
import com.devel.pricetracker.application.dto.ItemPriceDtoView;
import com.devel.pricetracker.application.factory.ItemDtoViewFactory;
import com.devel.pricetracker.application.factory.ItemPriceDtoViewFactory;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import com.devel.pricetracker.application.parsers.PriceParser;
import com.devel.pricetracker.application.services.ItemPriceService;
import com.devel.pricetracker.application.services.ItemService;
import com.devel.pricetracker.application.utils.CurrencyUtils;
import javassist.NotFoundException;
import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class IndexWebController {

    @Autowired
    public IndexWebController(ItemService itemService, ItemPriceService itemPriceService, PriceParser priceParser) {
        this.itemService = itemService;
        this.itemPriceService = itemPriceService;
        this.priceParser = priceParser;
    }

    @GetMapping("/")
    public String indexAction(Model model) {
        List<ItemDtoView> itemDtoViews = getItemDtos();
        model.addAttribute("items", itemDtoViews);
        return "index";
    }

    @GetMapping("/view/{id}")
    public String detailAction(Model model, @PathVariable Long id) {
        ItemEntity itemEntity;
        ItemDtoView itemDtoView;
        try {
            itemEntity = itemService.find(id);
        } catch (NotFoundException e) {
            logger.warn(String.format("An error occurred during find item with id \"%d\"", id));
            return "redirect:/";
        }
        itemDtoView = getItemDto(itemEntity);
        List<ItemPriceDtoView> itemPriceDtoViews = itemPriceService.findAll(itemEntity).stream().limit(DETAIL_PRICES_LIMIT)
                .map(itemPriceEntity -> ItemPriceDtoViewFactory.create(itemPriceEntity)).collect(Collectors.toList());
        itemPriceDtoViews.forEach(itemPriceDto -> itemPriceDto.setFormatedPrice(CurrencyUtils.formatCurrency(itemPriceDto.getPrice())));
        List<ItemDtoView> itemDtoViews = getItemDtos();
        model.addAttribute("item", itemDtoView);
        model.addAttribute("prices", itemPriceDtoViews);
        model.addAttribute("items", itemDtoViews);
        return "index";
    }

    @PostMapping("/create")
    public String createAction(@RequestParam String name, @RequestParam String url,
                               @RequestParam String selector, @RequestParam(value = "break_selector") String breakSelector,
                               @RequestParam(name = "parse_now") Optional<Boolean> parseNow) {
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setName(name);
        itemEntity.setUrl(url);
        itemEntity.setSelector(selector);
        itemEntity.setBreakSelector(breakSelector);
        itemEntity.setDateFrom(LocalDateTime.now());
        ItemEntity createdItemEntity = itemService.create(itemEntity);
        try {
            if (createdItemEntity != null && parseNow.isPresent() && parseNow.get()) {
                priceParser.parse(createdItemEntity);
            }
        } catch (NotFoundException | IOException e) {
            logger.error(String.format("An error occurred during parse price %s: %s", createdItemEntity.getUrl(), e.getMessage()));
        }
        return "redirect:/";
    }

    @PostMapping("/update")
    public String updateAction(@RequestParam Long id, @RequestParam String name, @RequestParam String url,
                               @RequestParam String selector, @RequestParam(value = "break_selector") String breakSelector,
                               @RequestParam(name = "parse_now") Optional<Boolean> parseNow) {
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setId(id);
        itemEntity.setName(name);
        itemEntity.setUrl(url);
        itemEntity.setSelector(selector);
        itemEntity.setBreakSelector(breakSelector);
        try {
            ItemEntity updatedItemEntity = itemService.update(itemEntity);
            try {
                if (parseNow.isPresent() && parseNow.get()) {
                    priceParser.parse(updatedItemEntity);
                }
            } catch(HttpStatusException e) {
                logger.error(e.getMessage());
            } catch (NotFoundException | IOException e) {
                logger.error(String.format("An error occurred during parse price %s: %s", updatedItemEntity.getUrl(), e.getMessage()));
            }
        } catch (NotFoundException e) {
            logger.error(e.getMessage());
        }
        return String.format("redirect:/view/%d", itemEntity.getId());
    }

    @PostMapping("/delete/{id}")
    public String deleteAction(@PathVariable Long id) {
        try {
            itemService.delete(id);
        } catch (NotFoundException e) {
            logger.warn(String.format("An error occurred during delete item with id \"%d\"", id));
        }
        return "redirect:/";
    }

    @PostMapping("/activate/{id}")
    public String activateAction(@PathVariable Long id) {
        try {
            itemService.activate(id);
        } catch (NotFoundException e) {
            logger.warn(String.format("An error occurred during activate item with id \"%d\"", id));
        }
        return "redirect:/";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateAction(@PathVariable Long id) {
        try {
            itemService.deactivate(id);
        } catch (NotFoundException e) {
            logger.warn(String.format("An error occurred during deactivate item with id \"%d\"", id));
        }
        return "redirect:/";
    }

    @GetMapping("/prices/clean/{id}")
    public String cleanAction(@PathVariable Long id) throws NotFoundException {
        ItemEntity itemEntity = itemService.find(id);
        cleanPriceDuplicates(itemEntity);
        return String.format("redirect:/view/%d", itemEntity.getId());
    }

    @GetMapping("/prices/cleanall")
    public String cleanAllAction() throws NotFoundException {
        List<ItemEntity> itemEntities = itemService.findAll(false);
        for (ItemEntity itemEntity : itemEntities) {
            cleanPriceDuplicates(itemEntity);
        }
        return "redirect:/";
    }

    @GetMapping("/parseAll")
    public String parseAllAction() {
        priceParser.parseAll();
        return "redirect:/";
    }

    private void cleanPriceDuplicates(ItemEntity itemEntity) {
        List<ItemPriceEntity> itemPriceEntities = itemPriceService.findAll(itemEntity);
        Float prevValue = null;
        for (ItemPriceEntity itemPriceEntity : itemPriceEntities) {
            Float currentValue = itemPriceEntity.getPrice();
            if (currentValue.equals(prevValue)) {
                itemPriceService.delete(itemPriceEntity);
            }
            prevValue = currentValue;
        }
    }

    private List<ItemDtoView> getItemDtos() {
        List<ItemEntity> itemEntities = itemService.findAll(false);
        List<ItemDtoView> itemDtoViews = new ArrayList<>();
        for (ItemEntity itemEntity : itemEntities) {
            ItemDtoView itemDtoView = getItemDto(itemEntity);
            itemDtoViews.add(itemDtoView);
        }
        return itemDtoViews;
    }

    private ItemDtoView getItemDto(ItemEntity itemEntity) {
        ItemDtoView itemDtoView = ItemDtoViewFactory.create(itemEntity);
        List<ItemPriceEntity> itemPriceEntities = itemPriceService.findLast(itemEntity);

        String formatedLastPrice = CurrencyUtils.formatCurrency(getLastPrice(itemPriceEntities));
        itemDtoView.setFormatedLastPrice(formatedLastPrice);

        Float delta = getDelta(itemPriceEntities);
        itemDtoView.setDelta(delta);
        itemDtoView.setFormatedDelta(CurrencyUtils.formatCurrency(Math.abs(delta)));

        try {
            URL url = new URL(itemEntity.getUrl());
            itemDtoView.setHost(url.getHost());
        } catch (MalformedURLException e) {
            logger.debug(String.format("An error occurred during parse host by item entity with url \"%s\"", itemEntity.getUrl()));
        }

        return itemDtoView;
    }

    private Float getDelta(List<ItemPriceEntity> itemPriceEntities) {
        Float lastPricesValue = 0f;
        int itemPriceCount = itemPriceEntities.size();
        if (itemPriceCount == 2) {
            Float todayPrice = itemPriceEntities.get(0).getPrice();
            Float yesterdayPrice = itemPriceEntities.get(1).getPrice();
            if (!todayPrice.equals(yesterdayPrice)) {
                lastPricesValue = todayPrice - yesterdayPrice;
            }
        }
        return lastPricesValue;
    }

    private Float getLastPrice(List<ItemPriceEntity> itemPriceEntities) {
        if (itemPriceEntities.size() == 0) {
            return 0f;
        }
        return itemPriceEntities.get(0).getPrice();
    }

    private final int DETAIL_PRICES_LIMIT = 10;

    private final ItemService itemService;

    private final ItemPriceService itemPriceService;

    private final PriceParser priceParser;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}
