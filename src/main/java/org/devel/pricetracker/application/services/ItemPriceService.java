package org.devel.pricetracker.application.services;

import javassist.NotFoundException;
import org.devel.pricetracker.application.dto.ItemPriceDto;
import org.devel.pricetracker.application.entities.Item;
import org.devel.pricetracker.application.entities.ItemPrice;
import org.devel.pricetracker.application.repository.ItemDao;
import org.devel.pricetracker.application.repository.ItemPriceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemPriceService {

    private final ItemPriceDao itemPriceDao;

    private final ItemDao itemDao;

    @Autowired
    public ItemPriceService(ItemPriceDao itemPriceDao, ItemDao itemDao) {
        this.itemPriceDao = itemPriceDao;
        this.itemDao = itemDao;
    }

    @Transactional(readOnly = true)
    public List<ItemPrice> findAll(Item item) {
        return itemPriceDao.findAllByItemId(item.getId(), null);
    }

    @Transactional(readOnly = true)
    public List<ItemPrice> findLast(Item item) {
        return itemPriceDao.findAllByItemId(item.getId(), 2);
    }

    @Transactional
    public ItemPrice create(ItemPriceDto itemPriceDto) throws NotFoundException {
        Long itemId = itemPriceDto.getItemId();
        Item item = itemDao.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Cannot create Item price and find Item with id: %d", itemId)));
        ItemPrice itemPrice = new ItemPrice();
        itemPrice.setItem(item);
        itemPrice.setPrice(itemPriceDto.getPrice());
        itemPrice.setCreatedAt(LocalDateTime.now());
        return itemPriceDao.save(itemPrice);
    }

    @Transactional
    public void delete(Long id) {
        itemPriceDao.deleteById(id);
    }
}
