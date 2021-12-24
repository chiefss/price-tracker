package com.devel.pricetracker.application.controllers.api;

import com.devel.pricetracker.application.dto.ItemPriceDto;
import com.devel.pricetracker.application.factory.ItemPriceDtoFactory;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.services.ItemPriceService;
import com.devel.pricetracker.application.services.ItemService;
import com.devel.pricetracker.application.utils.CurrencyUtils;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class IndexApiController {

    @Autowired
    public IndexApiController(ItemService itemService, ItemPriceService itemPriceService) {
        this.itemService = itemService;
        this.itemPriceService = itemPriceService;
    }

    @GetMapping("/api/prices/{id}")
    public List<ItemPriceDto> pricesAction(@PathVariable Long id) throws NotFoundException {
        ItemEntity itemEntity;
        try {
            itemEntity = itemService.find(id);
        } catch (NotFoundException e) {
            logger.warn(String.format("An error occurred during find item prices for item with id \"%d\"", id));
            throw new NotFoundException("An error occurred during find item prices for item with id \"%d\"", e);
        }
        List<ItemPriceDto> itemPriceDtos = itemPriceService.findAll(itemEntity).stream().map(itemPriceEntity -> ItemPriceDtoFactory.create(itemPriceEntity)).collect(Collectors.toList());
        itemPriceDtos.forEach(itemPriceDto -> itemPriceDto.setFormatedPrice(CurrencyUtils.formatCurrency(itemPriceDto.getPrice())));
        return itemPriceDtos;
    }

    private final ItemService itemService;

    private final ItemPriceService itemPriceService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}
