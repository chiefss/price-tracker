package org.devel.pricetracker.application.controllers.api;

import javassist.NotFoundException;
import org.devel.pricetracker.application.dto.ItemPriceDto;
import org.devel.pricetracker.application.entities.Item;
import org.devel.pricetracker.application.factory.ItemPriceDtoFactory;
import org.devel.pricetracker.application.services.ItemPriceService;
import org.devel.pricetracker.application.services.ItemService;
import org.devel.pricetracker.application.utils.CurrencyUtils;
import org.devel.pricetracker.application.utils.Defines;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = Defines.API_PREFIX)
public class IndexApiController {

    private final ItemService itemService;

    private final ItemPriceService itemPriceService;

    @Autowired
    public IndexApiController(ItemService itemService, ItemPriceService itemPriceService) {
        this.itemService = itemService;
        this.itemPriceService = itemPriceService;
    }

    @GetMapping("prices/{id}")
    public List<ItemPriceDto> pricesAction(@PathVariable Long id) throws NotFoundException {
        Item item = itemService.find(id);
        List<ItemPriceDto> itemPriceDtos = itemPriceService.findAll(item).stream().map(ItemPriceDtoFactory::create).toList();
        itemPriceDtos.forEach(itemPriceDto -> itemPriceDto.setFormattedPrice(CurrencyUtils.formatCurrency(itemPriceDto.getPrice())));
        return itemPriceDtos;
    }
}
