package com.devel.pricetracker.application.factory;


import com.devel.pricetracker.application.dto.ItemPriceDto;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;

public class ItemPriceDtoFactory {
    public static ItemPriceDto create(ItemPriceEntity itemPriceEntity) {
        return new ItemPriceDto(
                itemPriceEntity.getId(),
                itemPriceEntity.getPrice(),
                itemPriceEntity.getDateFrom());
    }
}
