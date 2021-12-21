package com.devel.pricetracker.application.services;

import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import com.devel.pricetracker.application.models.repository.ItemPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemPriceServiceImpl implements ItemPriceService {

    @Autowired
    public ItemPriceServiceImpl(ItemPriceRepository itemPriceRepository) {
        this.itemPriceRepository = itemPriceRepository;
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
    public ItemPriceEntity create(ItemPriceEntity itemPriceEntity) {
        itemPriceEntity.setId(null);
        ItemPriceEntity newEntity = itemPriceRepository.save(itemPriceEntity);
        return newEntity;
    }

    @Override
    @Transactional
    public Long deleteAll(ItemEntity itemEntity) {
        itemPriceRepository.deleteAllByItemId(itemEntity.getId());
        return 1L;
    }

    private final ItemPriceRepository itemPriceRepository;
}
