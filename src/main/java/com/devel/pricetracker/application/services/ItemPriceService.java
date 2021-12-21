package com.devel.pricetracker.application.services;

import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import javassist.NotFoundException;

import java.util.List;

public interface ItemPriceService {

    public List<ItemPriceEntity> findAll(ItemEntity itemEntity);
    public List<ItemPriceEntity> findLast(ItemEntity itemEntity);
    public ItemPriceEntity create(ItemPriceEntity itemPriceEntity);
    public Long deleteAll(ItemEntity itemEntity);
}
