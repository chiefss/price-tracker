package org.devel.pricetracker.dao;


import org.devel.pricetracker.AbstractFunctionalSpringBootTest;
import org.devel.pricetracker.application.entities.Item;
import org.devel.pricetracker.application.repository.ItemDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

class TestItemDao extends AbstractFunctionalSpringBootTest {

    private final ItemDao itemDao;

    @Autowired
    TestItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    @Test
    void testFindById() {
        Optional<Item> itemEntityOptional = itemDao.findById(1L);
        Assertions.assertTrue(itemEntityOptional.isPresent());
        Item item = itemEntityOptional.get();
        Assertions.assertEquals(1L, item.getId());
    }

    @Test
    void testCreate() {
        Item item = new Item();
        item.setName("name");
        item.setUrl("localhost");
        item.setSelector("");
        item.setBreakSelector("");

        Item createdItem = itemDao.save(item);

        Assertions.assertNotNull(createdItem);
    }
}
