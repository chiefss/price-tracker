package com.devel.pricetracker.application.services;

import com.devel.pricetracker.application.dto.ItemDto;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.repository.ItemPriceRepository;
import com.devel.pricetracker.application.models.repository.ItemRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, ItemPriceRepository itemPriceRepository) {
        this.itemRepository = itemRepository;
        this.itemPriceRepository = itemPriceRepository;
    }

    @Override
    public List<ItemEntity> findAll(boolean activatedOnly) {
        return itemRepository.findAll(new Specification<ItemEntity>() {
            @Override
            public Predicate toPredicate(Root<ItemEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (activatedOnly) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.isNull(root.get("dateTo"))));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, Sort.by("dateTo").ascending().and(Sort.by("name").ascending()));
    }

    @Override
    public ItemEntity find(Long itemId) throws NotFoundException {
        return itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException(String.format("An error occurred during find Item with id: %d", itemId)));
    }

    @Override
    public ItemEntity create(ItemDto itemDto) {
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setName(itemDto.getName());
        itemEntity.setUrl(itemDto.getUrl());
        itemEntity.setSelector(itemDto.getSelector());
        itemEntity.setBreakSelector(itemDto.getBreakSelector());
        itemEntity.setDateFrom(LocalDateTime.now());
        return itemRepository.save(itemEntity);
    }

    @Override
    public ItemEntity update(ItemDto itemDto) throws NotFoundException {
        ItemEntity itemEntityFromRepository = find(itemDto.getId());
        itemEntityFromRepository.setName(itemDto.getName());
        itemEntityFromRepository.setUrl(itemDto.getUrl());
        itemEntityFromRepository.setSelector(itemDto.getSelector());
        itemEntityFromRepository.setBreakSelector(itemDto.getBreakSelector());
        return itemRepository.save(itemEntityFromRepository);
    }

    @Override
    @Transactional
    public void delete(Long itemId) throws NotFoundException {
        ItemEntity itemEntity = find(itemId);
        itemPriceRepository.deleteAllByItemId(itemEntity.getId());
        itemRepository.delete(itemEntity);
    }

    @Override
    public void activate(Long itemId) throws NotFoundException {
        ItemEntity itemEntity = find(itemId);
        itemEntity.setDateTo(null);
        itemRepository.save(itemEntity);
    }

    @Override
    public void deactivate(Long itemId) throws NotFoundException {
        ItemEntity itemEntity = find(itemId);
        itemEntity.setDateTo(LocalDateTime.now());
        itemRepository.save(itemEntity);
    }

    private final ItemRepository itemRepository;

    private final ItemPriceRepository itemPriceRepository;
}
