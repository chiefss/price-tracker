package org.devel.pricetracker.services;


import org.devel.pricetracker.AbstractFunctionalTest;
import org.devel.pricetracker.application.dto.ItemDto;
import org.devel.pricetracker.application.entities.Item;
import org.devel.pricetracker.application.repository.ItemDao;
import org.devel.pricetracker.application.repository.ItemPriceDao;
import org.devel.pricetracker.application.services.ItemService;
import javassist.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class TestItemService extends AbstractFunctionalTest {

    private final ItemService itemService;

    private final ItemDao itemDao;

    private final ItemPriceDao itemPriceDao;

    public TestItemService() {
        this.itemDao = mock(ItemDao.class);
        this.itemPriceDao = mock(ItemPriceDao.class);
        this.itemService = new ItemService(itemDao, itemPriceDao);
    }

    @Test
    void testFindAll() {
        when(itemDao.findAllByActivated(false))
                .thenReturn(List.of());

        itemService.findAll(false);

        verify(itemDao, times(1)).findAllByActivated(false);
    }

    @Test
    void testFindAllActivatedOnly() {
        when(itemDao.findAllByActivated(true))
                .thenReturn(List.of());

        itemService.findAll(true);

        verify(itemDao, times(1)).findAllByActivated(true);
    }

    @Test
    void testFind() throws NotFoundException {
        when(itemDao.findById(1L))
                .thenReturn(Optional.of(new Item()));

        itemService.find(1L);

        verify(itemDao, times(1)).findById(1L);
    }

    @Test
    void testFindNotFound() {
        when(itemDao.findById(1000L))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.find(1000L));

        verify(itemDao, times(1)).findById(1000L);
    }

    @Test
    void testCreate() {
        ItemDto itemDto = new ItemDto(null, "name 6", "url 6", "selector 6", "break selector 6");

        itemService.create(itemDto);

        ArgumentCaptor<Item> requestCaptorItem = ArgumentCaptor.forClass(Item.class);
        verify(itemDao, times(1)).save(requestCaptorItem.capture());
        Item item = requestCaptorItem.getValue();
        Assertions.assertEquals("name 6", item.getName());
        Assertions.assertEquals("url 6", item.getUrl());
        Assertions.assertEquals("selector 6", item.getSelector());
        Assertions.assertEquals("break selector 6", item.getBreakSelector());
    }

    @Test
    void testUpdate() throws NotFoundException {
        Long itemId = 2L;
        ItemDto itemDto = new ItemDto(itemId, "name 2-2", "url 2-2", "selector 2-2", "break selector 2-2");
        Item item = new Item("name 2", "url 2", "selector 2", "break selector 2");
        item.setId(itemId);
        when(itemDao.findById(any()))
                .thenReturn(Optional.of(item));

        itemService.update(itemDto);

        ArgumentCaptor<Item> requestCaptorItem = ArgumentCaptor.forClass(Item.class);
        verify(itemDao, times(1)).save(requestCaptorItem.capture());
        Item updatedItem = requestCaptorItem.getValue();
        Assertions.assertEquals("name 2-2", updatedItem.getName());
        Assertions.assertEquals("url 2-2", updatedItem.getUrl());
        Assertions.assertEquals("selector 2-2", updatedItem.getSelector());
        Assertions.assertEquals("break selector 2-2", updatedItem.getBreakSelector());
    }

    @Test
    void testUpdateNotFound() {
        when(itemDao.findById(any()))
                .thenReturn(Optional.empty());
        Long expectedId = 1000L;
        ItemDto itemDto = new ItemDto(expectedId, "name 2-2", "url 2-2", "selector 2-2", "break selector 2-2");

        Assertions.assertThrows(NotFoundException.class, () -> itemService.update(itemDto));
    }

    @Test
    void testDelete() throws NotFoundException {
        Long itemId = 3L;
        Item item = new Item();
        item.setId(itemId);
        when(itemDao.findById(any()))
                .thenReturn(Optional.of(item));

        itemService.delete(itemId);

        verify(itemPriceDao, times(1)).deleteAllByItemId(itemId);
        verify(itemDao, times(1)).deleteById(itemId);
    }

    @Test
    void testDeleteNotFound() {
        Long itemId = 1000L;

        Assertions.assertThrows(NotFoundException.class, () -> itemService.delete(itemId));
    }
}
