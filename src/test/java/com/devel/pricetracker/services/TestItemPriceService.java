package com.devel.pricetracker.services;


import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import com.devel.pricetracker.application.models.repository.ItemPriceRepository;
import com.devel.pricetracker.application.services.ItemPriceService;
import javassist.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("tests")
@Sql("/db.tests.data/init.sql")
public class TestItemPriceService {

    @Autowired
    public TestItemPriceService(ItemPriceService itemPriceService, ItemPriceRepository itemPriceRepository) {
        this.itemPriceService = itemPriceService;
        this.itemPriceRepository = itemPriceRepository;
    }

    @Test
    public void testFindAll() {
        ItemEntity itemEnity = new ItemEntity();
        itemEnity.setId(1L);
        List<ItemPriceEntity> itemPriceEntities = itemPriceService.findAll(itemEnity);
        Assertions.assertEquals(2, itemPriceEntities.size());
    }

    @Test
    public void testFindLast() {
        ItemEntity itemEnity = new ItemEntity();
        itemEnity.setId(2L);
        List<ItemPriceEntity> itemPriceEntities = itemPriceService.findLast(itemEnity);
        Assertions.assertEquals(2, itemPriceEntities.size());
    }

    @Test
    public void testCreate() {
        Long expectedId = 18L;
        ItemEntity itemEnity = new ItemEntity();
        itemEnity.setId(1L);
        ItemPriceEntity itemPriceEntity = new ItemPriceEntity(1000L, itemEnity, (float) 222.3, LocalDateTime.now());
        ItemPriceEntity newItemPriceEntity = itemPriceService.create(itemPriceEntity);
        Assertions.assertEquals(expectedId, newItemPriceEntity.getId());
        Optional<ItemPriceEntity> itemPriceEntityOptional = itemPriceRepository.findById(expectedId);
        Assertions.assertTrue(itemPriceEntityOptional.isPresent());
    }

    @Test
    public void testDelete() {
        Long expectedId = 3L;
        long countAll = itemPriceRepository.count();
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setId(expectedId);
        Long result = itemPriceService.deleteAll(itemEntity);
        Assertions.assertEquals(1L, result);
        Optional<ItemPriceEntity> itemPriceEntity = itemPriceRepository.findById(6L);
        Assertions.assertTrue(itemPriceEntity.isEmpty());
        long newCountAll = itemPriceRepository.count();
        Assertions.assertEquals(newCountAll, countAll - 3);
    }

    private final ItemPriceService itemPriceService;

    private final ItemPriceRepository itemPriceRepository;
}
