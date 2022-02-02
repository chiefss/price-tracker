package com.devel.pricetracker.application.services;

import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.repository.ItemRepository;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, ItemPriceService itemPriceService) {
        this.itemRepository = itemRepository;
        this.itemPriceService = itemPriceService;
    }

    @Override
    public List<ItemEntity> findAll(boolean activatedOnly) {
        Iterable<ItemEntity> itemEntityIterable = itemRepository.findAll(Sort.by("name").ascending());
        List<ItemEntity> itemEntities = new ArrayList<>();
        for (ItemEntity itemEntity : itemEntityIterable) {
            itemEntities.add(itemEntity);
        }
        if (activatedOnly) {
            itemEntities = itemEntities.stream().filter(itemEntity -> itemEntity.getDateTo() == null).collect(Collectors.toList());
        }
        return itemEntities;
    }

    @Override
    public ItemEntity find(Long itemId) throws NotFoundException {
        return itemRepository.findById(itemId).orElseThrow(() -> {
            logger.warn(String.format("An error occurred during find Item with id: %d", itemId));
            return new NotFoundException(String.format("An error occurred during find Item with id: %d", itemId));
        });
    }

    @Override
    public ItemEntity create(ItemEntity itemEntity) {
        itemEntity.setId(null);
        return itemRepository.save(itemEntity);
    }

    @Override
    public ItemEntity update(ItemEntity itemEntity) throws NotFoundException {
        Long itemId = itemEntity.getId();
        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(itemId);
        if (itemEntityOptional.isEmpty()) {
            logger.error(String.format("An error occurred during update Item with id: %d", itemId));
            throw new NotFoundException(String.format("An error occurred during update Item with id: %d", itemId));
        }
        ItemEntity itemEntityFromRepository = itemEntityOptional.get();
        itemEntityFromRepository.setName(itemEntity.getName());
        itemEntityFromRepository.setUrl(itemEntity.getUrl());
        itemEntityFromRepository.setSelector(itemEntity.getSelector());
        itemEntityFromRepository.setBreakSelector(itemEntity.getBreakSelector());

        itemRepository.save(itemEntityFromRepository);
        return itemEntityFromRepository;
    }

    @Override
    @Transactional
    public Long delete(Long itemId) throws NotFoundException {
        try {
            ItemEntity itemEntity = find(itemId);
            itemPriceService.deleteAll(itemEntity);
            itemRepository.delete(itemEntity);
            return itemId;
        } catch (NotFoundException e) {
            logger.error(String.format("An error occurred during delete Item with id: %d", itemId));
            throw new NotFoundException(String.format("An error occurred during delete Item with id: %d", itemId), e);
        }
    }

    @Override
    public Long activate(Long itemId) throws NotFoundException {
        try {
            ItemEntity itemEntity = find(itemId);
            itemEntity.setDateTo(null);
            itemRepository.save(itemEntity);
            return itemId;
        } catch (NotFoundException e) {
            logger.error(String.format("An error occurred during activate Item with id: %d", itemId));
            throw new NotFoundException(String.format("An error occurred during activate Item with id: %d", itemId), e);
        }
    }

    @Override
    public Long deactivate(Long itemId) throws NotFoundException {
        try {
            ItemEntity itemEntity = find(itemId);
            itemEntity.setDateTo(LocalDateTime.now());
            itemRepository.save(itemEntity);
            return itemId;
        } catch (NotFoundException e) {
            logger.error(String.format("An error occurred during deactivate Item with id: %d", itemId));
            throw new NotFoundException(String.format("An error occurred during deactivate Item with id: %d", itemId), e);
        }
    }

    private final ItemRepository itemRepository;

    private final ItemPriceService itemPriceService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}
