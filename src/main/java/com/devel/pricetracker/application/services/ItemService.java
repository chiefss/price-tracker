package com.devel.pricetracker.application.services;

import com.devel.pricetracker.application.models.entities.ItemEntity;
import javassist.NotFoundException;

import java.util.List;

public interface ItemService {
    public List<ItemEntity> findAll();
    public ItemEntity find(Long itemId) throws NotFoundException;
    public ItemEntity create(ItemEntity itemEntity);
    public ItemEntity update(ItemEntity itemEntity) throws NotFoundException;
    public Long delete(Long itemId) throws NotFoundException;
}
