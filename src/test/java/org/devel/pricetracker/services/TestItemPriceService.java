package org.devel.pricetracker.services;


import javassist.NotFoundException;
import org.devel.pricetracker.AbstractFunctionalTest;
import org.devel.pricetracker.application.dto.ItemPriceDto;
import org.devel.pricetracker.application.entities.Item;
import org.devel.pricetracker.application.entities.ItemPrice;
import org.devel.pricetracker.application.repository.ItemDao;
import org.devel.pricetracker.application.repository.ItemPriceDao;
import org.devel.pricetracker.application.services.ItemPriceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class TestItemPriceService extends AbstractFunctionalTest {

    private final ItemPriceService itemPriceService;

    private final ItemDao itemDao;

    private final ItemPriceDao itemPriceDao;

    TestItemPriceService() {
        this.itemDao = mock(ItemDao.class);
        this.itemPriceDao = mock(ItemPriceDao.class);
        this.itemPriceService = new ItemPriceService(itemPriceDao, itemDao);
    }

    @Test
    void testFindAll() {
        Item itemEnity = new Item();
        itemEnity.setId(1L);
        when(itemPriceDao.findAllByItemId(itemEnity.getId(), null))
                .thenReturn(List.of());

        itemPriceService.findAll(itemEnity);

        verify(itemPriceDao, times(1)).findAllByItemId(itemEnity.getId(), null);
    }

    @Test
    void testFindLast() {
        Item itemEnity = new Item();
        itemEnity.setId(2L);
        when(itemPriceDao.findAllByItemId(itemEnity.getId(), 2))
                .thenReturn(List.of());

        itemPriceService.findLast(itemEnity);

        verify(itemPriceDao, times(1)).findAllByItemId(itemEnity.getId(), 2);
    }

    @Test
    void testCreate() throws NotFoundException {
        long itemId = 1L;
        ItemPriceDto itemPriceDto = new ItemPriceDto(1000L, itemId, 222.3, LocalDateTime.now());
        Item item = new Item();
        item.setId(itemId);
        when(itemDao.findById(itemId))
                .thenReturn(Optional.of(item));

        itemPriceService.create(itemPriceDto);

        ArgumentCaptor<ItemPrice> requestCaptorItem = ArgumentCaptor.forClass(ItemPrice.class);
        verify(itemPriceDao, times(1)).save(requestCaptorItem.capture());
        ItemPrice createdItem = requestCaptorItem.getValue();
        Assertions.assertEquals(itemId, createdItem.getItem().getId());
        Assertions.assertEquals(222.3, createdItem.getPrice());
        Assertions.assertNotNull(createdItem.getCreatedAt());
    }
}
