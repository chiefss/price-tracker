package com.devel.pricetracker.services;


import com.devel.pricetracker.application.dto.ItemDto;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.repository.ItemRepository;
import com.devel.pricetracker.application.services.ItemService;
import javassist.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("tests")
@Sql("/db.tests.data/init.sql")
public class TestItemService {

    @Autowired
    public TestItemService(ItemService itemService, ItemRepository itemRepository) {
        this.itemService = itemService;
        this.itemRepository = itemRepository;
    }

    @Test
    public void testFindAll() {
        List<ItemEntity> itemEntities = itemService.findAll(false);
        Assertions.assertEquals(5, itemEntities.size());
    }

    @Test
    public void testFindAllActivatedOnly() {
        List<ItemEntity> itemEntities = itemService.findAll(true);
        Assertions.assertEquals(4, itemEntities.size());
    }

    @Test
    public void testFind() {
        ItemEntity itemEntity = null;
        try {
            itemEntity = itemService.find(1L);
        } catch (NotFoundException e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(1L, itemEntity.getId());
    }

    @Test
    public void testFindNotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> itemService.find(1000L));
    }

    @Test
    public void testCreate() {
        ItemDto itemDto = new ItemDto(null, "name 6", "url 6", "selector 6", "break selector 6");
        ItemEntity createdItemEntity = itemService.create(itemDto);
        Assertions.assertTrue(createdItemEntity.getId() > 0);
        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(createdItemEntity.getId());
        Assertions.assertTrue(itemEntityOptional.isPresent());
    }

    @Test
    public void testUpdate() throws NotFoundException {
        Long expectedId = 2L;
        ItemDto itemDto = new ItemDto(expectedId, "name 2-2", "url 2-2", "selector 2-2", "break selector 2-2");
        ItemEntity updatedItemEntity = itemService.update(itemDto);
        Assertions.assertEquals(expectedId, updatedItemEntity.getId());
        ItemEntity itemEntityUpdated = itemRepository.findById(expectedId).get();
        Assertions.assertEquals("name 2-2", itemEntityUpdated.getName());
        Assertions.assertEquals("url 2-2", itemEntityUpdated.getUrl());
        Assertions.assertEquals("selector 2-2", itemEntityUpdated.getSelector());
        Assertions.assertEquals("break selector 2-2", itemEntityUpdated.getBreakSelector());
    }

    @Test
    public void testUpdateNotFound() {
        Long expectedId = 1000L;
        ItemDto itemDto = new ItemDto(expectedId, "name 2-2", "url 2-2", "selector 2-2", "break selector 2-2");
        Assertions.assertThrows(NotFoundException.class, () -> itemService.update(itemDto));
    }

    @Test
    public void testDelete() throws NotFoundException {
        Long expectedId = 3L;
        itemService.delete(expectedId);
        Optional<ItemEntity> itemEntity = itemRepository.findById(expectedId);
        Assertions.assertTrue(itemEntity.isEmpty());
    }

    @Test
    public void testDeleteNotFound() {
        Long expectedId = 1000L;
        Assertions.assertThrows(NotFoundException.class, () -> itemService.delete(expectedId));
    }

    private final ItemService itemService;

    private final ItemRepository itemRepository;
}
