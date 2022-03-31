package com.devel.pricetracker.application.services;

import com.devel.pricetracker.application.dto.ItemDto;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.repository.ItemPriceRepository;
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
    public ItemServiceImpl(ItemRepository itemRepository, ItemPriceRepository itemPriceRepository) {
        this.itemRepository = itemRepository;
        this.itemPriceRepository = itemPriceRepository;
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
    public ItemEntity create(ItemDto itemDto) {
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setId(null);
        itemEntity.setName(itemDto.getName());
        itemEntity.setUrl(itemDto.getUrl());
        itemEntity.setSelector(itemDto.getSelector());
        itemEntity.setBreakSelector(itemDto.getBreakSelector());
        itemEntity.setDateFrom(LocalDateTime.now());
        return itemRepository.save(itemEntity);
    }

    @Override
    public ItemEntity update(ItemDto itemDto) throws NotFoundException {
        Long itemId = itemDto.getId();
        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(itemId);
        if (itemEntityOptional.isEmpty()) {
            logger.error(String.format("An error occurred during update Item with id: %d", itemId));
            throw new NotFoundException(String.format("An error occurred during update Item with id: %d", itemId));
        }
        ItemEntity itemEntityFromRepository = itemEntityOptional.get();
        itemEntityFromRepository.setName(itemDto.getName());
        itemEntityFromRepository.setUrl(itemDto.getUrl());
        itemEntityFromRepository.setSelector(itemDto.getSelector());
        itemEntityFromRepository.setBreakSelector(itemDto.getBreakSelector());

        itemRepository.save(itemEntityFromRepository);
        return itemEntityFromRepository;
    }

    @Override
    @Transactional
    public void delete(Long itemId) throws NotFoundException {
        ItemEntity itemEntity = find(itemId);
        itemPriceRepository.deleteAllByItemId(itemEntity.getId());
        itemRepository.delete(itemEntity);
    }

    @Override
    public void activate(Long itemId) throws NotFoundException {
        ItemEntity itemEntity = find(itemId);
        itemEntity.setDateTo(null);
        itemRepository.save(itemEntity);
    }

    @Override
    public void deactivate(Long itemId) throws NotFoundException {
        ItemEntity itemEntity = find(itemId);
        itemEntity.setDateTo(LocalDateTime.now());
        itemRepository.save(itemEntity);
    }

    private final ItemRepository itemRepository;

    private final ItemPriceRepository itemPriceRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}
