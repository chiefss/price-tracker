package com.devel.pricetracker.application.controllers.web;

import com.devel.pricetracker.application.dto.*;
import com.devel.pricetracker.application.factory.ItemDtoViewFactory;
import com.devel.pricetracker.application.factory.ItemPriceDtoFactory;
import com.devel.pricetracker.application.factory.ItemPriceDtoViewFactory;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import com.devel.pricetracker.application.parsers.PriceParser;
import com.devel.pricetracker.application.services.ItemPriceService;
import com.devel.pricetracker.application.services.ItemService;
import com.devel.pricetracker.application.utils.Constants;
import com.devel.pricetracker.application.utils.CurrencyUtils;
import javassist.NotFoundException;
import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String detailAction(Model model, @PathVariable Long id) throws NotFoundException {
        ItemEntity itemEntity = itemService.find(id);
        ItemDtoView itemDtoView = getItemDto(itemEntity);
        List<ItemPriceDtoView> itemPriceDtoViews = itemPriceService.findAll(itemEntity).stream().limit(Constants.DETAIL_PRICES_LIMIT)
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
                               @RequestParam(name = "parse_now", required = false) boolean parseNow) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setUrl(url);
        itemDto.setSelector(selector);
        itemDto.setBreakSelector(breakSelector);
        ItemEntity createdItemEntity = itemService.create(itemDto);
        if (createdItemEntity != null && parseNow) {
            try {
                createItemPrice(createdItemEntity);
            } catch (NotFoundException | IOException e) {
                logger.error(String.format("An error occurred during parse price %s: %s", createdItemEntity.getUrl(), e.getMessage()));
            }
        }
        return "redirect:/";
    }

    @PostMapping("/update")
    public String updateAction(@RequestParam Long id, @RequestParam String name, @RequestParam String url,
                               @RequestParam String selector, @RequestParam(value = "break_selector") String breakSelector,
                               @RequestParam(name = "parse_now", required = false) boolean parseNow) throws NotFoundException {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(id);
        itemDto.setName(name);
        itemDto.setUrl(url);
        itemDto.setSelector(selector);
        itemDto.setBreakSelector(breakSelector);
        ItemEntity updatedItemEntity = itemService.update(itemDto);
        if (parseNow) {
            try {
                createItemPrice(updatedItemEntity);
            } catch(HttpStatusException e) {
                logger.error(e.getMessage());
            } catch (NotFoundException | IOException e) {
                logger.error(String.format("An error occurred during parse price %s: %s", updatedItemEntity.getUrl(), e.getMessage()));
            }
        }
        return String.format("redirect:/view/%d", itemDto.getId());
    }

    @PostMapping("/delete/{id}")
    public String deleteAction(@PathVariable Long id) throws NotFoundException {
        itemService.delete(id);
        return "redirect:/";
    }

    @PostMapping("/activate/{id}")
    public String activateAction(@PathVariable Long id) throws NotFoundException {
        itemService.activate(id);
        return "redirect:/";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateAction(@PathVariable Long id) throws NotFoundException {
        itemService.deactivate(id);
        return "redirect:/";
    }

    @GetMapping("/prices/clean/{id}")
    public String cleanAction(@PathVariable Long id) throws NotFoundException {
        ItemEntity itemEntity = itemService.find(id);
        cleanPriceDuplicates(itemEntity);
        return String.format("redirect:/view/%d", itemEntity.getId());
    }

    @GetMapping("/prices/cleanall")
    public String cleanAllAction() {
        List<ItemEntity> itemEntities = itemService.findAll(false);
        for (ItemEntity itemEntity : itemEntities) {
            cleanPriceDuplicates(itemEntity);
        }
        return "redirect:/";
    }

    @GetMapping("/parseAll")
    public String parseAllAction() {
        List<PriceParserResultDto> priceParserResultDtos = priceParser.parseAll();
        for (PriceParserResultDto priceParserResultDto : priceParserResultDtos) {
            if (priceParserResultDto.isSuccess()) {
                ItemPriceEntity itemPriceEntity = priceParserResultDto.getItemPrice();
                ItemPriceDto itemPriceDto = ItemPriceDtoFactory.create(itemPriceEntity.getItem().getId(), itemPriceEntity.getPrice());
                try {
                    itemPriceService.create(itemPriceDto);
                } catch (NotFoundException e) {
                    logger.error(String.format("An error occurred during parse all and save item price for item id \"%d\"", itemPriceEntity.getItem().getId()));
                }
            }
        }
        return "redirect:/";
    }

    @GetMapping("/favicon.ico")
    @ResponseBody
    public void returnNoFavicon() {

    }

    private void createItemPrice(ItemEntity createdItemEntity) throws IOException, NotFoundException {
        Optional<ItemPriceEntity> itemPriceEntityOptional = priceParser.parse(createdItemEntity);
        if (itemPriceEntityOptional.isPresent()) {
            ItemPriceEntity itemPriceEntity = itemPriceEntityOptional.get();
            ItemPriceDto itemPriceDto = ItemPriceDtoFactory.create(itemPriceEntity.getItem().getId(), itemPriceEntity.getPrice());
            itemPriceService.create(itemPriceDto);
        }
    }

    private void cleanPriceDuplicates(ItemEntity itemEntity) {
        List<ItemPriceEntity> itemPriceEntities = itemPriceService.findAll(itemEntity);
        Float prevValue = null;
        for (ItemPriceEntity itemPriceEntity : itemPriceEntities) {
            Float currentValue = itemPriceEntity.getPrice();
            if (currentValue.equals(prevValue)) {
                itemPriceService.delete(itemPriceEntity.getId());
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

    private final ItemService itemService;

    private final ItemPriceService itemPriceService;

    private final PriceParser priceParser;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}
