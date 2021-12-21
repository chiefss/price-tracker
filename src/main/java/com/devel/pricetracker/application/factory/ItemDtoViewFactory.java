package com.devel.pricetracker.application.factory;


import com.devel.pricetracker.application.dto.ItemDtoView;
import com.devel.pricetracker.application.dto.ItemPriceDtoView;
import com.devel.pricetracker.application.models.entities.ItemEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ItemDtoViewFactory {
    public static ItemDtoView create(ItemEntity itemEntity) {
        return new ItemDtoView(
                itemEntity.getId(),
                itemEntity.getName(),
                itemEntity.getUrl(),
                itemEntity.getSelector(),
                itemEntity.getBreakSelector(),
                null,
                itemEntity.getDateFrom(),
                itemEntity.getDateTo());
    }
}
