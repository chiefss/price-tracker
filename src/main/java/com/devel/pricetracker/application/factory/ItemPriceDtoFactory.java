package com.devel.pricetracker.application.factory;


import com.devel.pricetracker.application.dto.ItemPriceDto;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;

public class ItemPriceDtoFactory {
    
    public static ItemPriceDto create(ItemPriceEntity itemPriceEntity) {
        return new ItemPriceDto(
                itemPriceEntity.getId(),
                itemPriceEntity.getItem().getId(),
                itemPriceEntity.getPrice(),
                itemPriceEntity.getDateFrom());
    }

    public static ItemPriceDto create(Long itemId, Float price) {
        return new ItemPriceDto(null, itemId, price, null);
    }
}
