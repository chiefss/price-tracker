package com.devel.pricetracker.application.services;

import com.devel.pricetracker.application.dto.ItemDto;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import javassist.NotFoundException;

import java.util.List;

public interface ItemService {
    public List<ItemEntity> findAll(boolean activatedOnly);
    public ItemEntity find(Long itemId) throws NotFoundException;
    public ItemEntity create(ItemDto itemDto);
    public ItemEntity update(ItemDto itemDto) throws NotFoundException;
    public void delete(Long itemId) throws NotFoundException;
    public void activate(Long itemId) throws NotFoundException;
    public void deactivate(Long itemId) throws NotFoundException;
}
