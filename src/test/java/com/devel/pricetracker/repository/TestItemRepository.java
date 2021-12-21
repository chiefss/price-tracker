package com.devel.pricetracker.repository;


import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.repository.ItemRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles("tests")
@Sql("/db.tests.data/init.sql")
public class TestItemRepository {

    @Autowired
    public TestItemRepository(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Test
    public void testFindById() {
        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(1L);
        Assertions.assertTrue(itemEntityOptional.isPresent());
        ItemEntity itemEntity = itemEntityOptional.get();
        Assertions.assertEquals(1L, itemEntity.getId());
    }

    private final ItemRepository itemRepository;
}
