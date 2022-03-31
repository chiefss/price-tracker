package com.devel.pricetracker.application.services;

import com.devel.pricetracker.application.dto.ItemPriceDto;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import com.devel.pricetracker.application.models.repository.ItemPriceRepository;
import com.devel.pricetracker.application.models.repository.ItemRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemPriceServiceImpl implements ItemPriceService {

    @Autowired
    public ItemPriceServiceImpl(ItemPriceRepository itemPriceRepository, ItemRepository itemRepository) {
        this.itemPriceRepository = itemPriceRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public List<ItemPriceEntity> findAll(ItemEntity itemEntity) {
        Iterable<ItemPriceEntity> itemPriceEntityIterable = itemPriceRepository.findAllByItemOrderByDateFromDesc(itemEntity);
        List<ItemPriceEntity> itemPriceEntities = new ArrayList<>();
        for (ItemPriceEntity itemPriceEntity : itemPriceEntityIterable) {
            itemPriceEntities.add(itemPriceEntity);
        }
        return itemPriceEntities;
    }

    public List<ItemPriceEntity> findLast(ItemEntity itemEntity) {
        Iterable<ItemPriceEntity> itemPriceEntityIterable = itemPriceRepository.findTop2ByItemOrderByDateFromDesc(itemEntity);
        List<ItemPriceEntity> itemPriceEntities = new ArrayList<>();
        for (ItemPriceEntity itemPriceEntity : itemPriceEntityIterable) {
            itemPriceEntities.add(itemPriceEntity);
        }
        return itemPriceEntities;
    }

    @Override
    public ItemPriceEntity create(ItemPriceDto itemPriceDto) throws NotFoundException {
        Long itemId = itemPriceDto.getItemId();
        ItemEntity itemEntity = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException(String.format("An error occurred during create Item price and find Item with id: %d", itemId)));
        ItemPriceEntity itemPriceEntity = new ItemPriceEntity();
        itemPriceEntity.setId(null);
        itemPriceEntity.setItem(itemEntity);
        itemPriceEntity.setPrice(itemPriceDto.getPrice());
        itemPriceEntity.setDateFrom(LocalDateTime.now());
        ItemPriceEntity newEntity = itemPriceRepository.save(itemPriceEntity);
        return newEntity;
    }

    public void delete(Long id) {
        itemPriceRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAll(ItemEntity itemEntity) {
        itemPriceRepository.deleteAllByItemId(itemEntity.getId());
    }

    private final ItemPriceRepository itemPriceRepository;

    private final ItemRepository itemRepository;
}
