package com.devel.pricetracker.repository;


import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import com.devel.pricetracker.application.models.repository.ItemPriceRepository;
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
public class TestItemPriceRepository {

    @Autowired
    public TestItemPriceRepository(ItemPriceRepository itemPriceRepository) {
        this.itemPriceRepository = itemPriceRepository;
    }

    @Test
    public void testFindById() {
        Optional<ItemPriceEntity> itemPriceEntityOptional = itemPriceRepository.findById(1L);
        Assertions.assertTrue(itemPriceEntityOptional.isPresent());
        ItemPriceEntity itemPriceEntity = itemPriceEntityOptional.get();
        Assertions.assertEquals(1L, itemPriceEntity.getId());
    }

    private final ItemPriceRepository itemPriceRepository;
}
