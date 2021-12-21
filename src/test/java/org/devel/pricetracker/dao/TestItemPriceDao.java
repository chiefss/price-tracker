package org.devel.pricetracker.dao;


import org.devel.pricetracker.AbstractFunctionalSpringBootTest;
import org.devel.pricetracker.application.entities.ItemPrice;
import org.devel.pricetracker.application.repository.ItemPriceDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

class TestItemPriceDao extends AbstractFunctionalSpringBootTest {

    private final ItemPriceDao itemPriceDao;

    @Autowired
    TestItemPriceDao(ItemPriceDao itemPriceDao) {
        this.itemPriceDao = itemPriceDao;
    }

    @Test
    void testFindById() {
        Optional<ItemPrice> itemPriceEntityOptional = itemPriceDao.findById(1L);

        Assertions.assertTrue(itemPriceEntityOptional.isPresent());
        ItemPrice itemPrice = itemPriceEntityOptional.get();
        Assertions.assertEquals(1L, itemPrice.getId());
    }

    @Test
    void findAllByItemId_WithoutLimit() {
        List<ItemPrice> itemPrices = itemPriceDao.findAllByItemId(1L, null);

        Assertions.assertFalse(itemPrices.isEmpty());
    }

    @Test
    void findAllByItemId_WithLimit() {
        List<ItemPrice> itemPrices = itemPriceDao.findAllByItemId(1L, 2);

        Assertions.assertEquals(2, itemPrices.size());
    }

    @Test
    void testDelete() {
        Long itemId = 3L;

        itemPriceDao.deleteAllByItemId(itemId);

        List<ItemPrice> items = itemPriceDao.findAll();
        Assertions.assertTrue(items.stream().noneMatch(itemPrice -> itemPrice.getItem().getId().equals(itemId)));
    }
}
