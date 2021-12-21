package com.devel.pricetracker.application.factory;


import com.devel.pricetracker.application.dto.ItemPriceDtoView;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;

public class ItemPriceDtoViewFactory {
    public static ItemPriceDtoView create(ItemPriceEntity itemPriceEntity) {
        return new ItemPriceDtoView(
                itemPriceEntity.getId(),
                itemPriceEntity.getPrice(),
                itemPriceEntity.getDateFrom());
    }
}
