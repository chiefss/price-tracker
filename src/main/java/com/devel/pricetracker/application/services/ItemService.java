package com.devel.pricetracker.application.services;

import com.devel.pricetracker.application.dto.ItemDto;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import javassist.NotFoundException;

import java.util.List;

public interface ItemService {
    public List<ItemEntity> findAll(boolean activatedOnly);
    public ItemEntity find(Long itemId) throws NotFoundException;
    public ItemEntity create(ItemDto itemEntity);
    public ItemEntity update(ItemDto itemEntity) throws NotFoundException;
    public Long delete(Long itemId) throws NotFoundException;
    public Long activate(Long itemId) throws NotFoundException;
    public Long deactivate(Long itemId) throws NotFoundException;
}
