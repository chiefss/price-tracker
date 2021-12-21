package com.devel.pricetracker.application.models.repository;

import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemPriceRepository extends CrudRepository<ItemPriceEntity, Long>, JpaSpecificationExecutor<ItemPriceEntity> /*, JpaRepository<ItemPriceEntity, Long>*/ {

    public List<ItemPriceEntity> findAllByItemOrderByDateFromDesc(ItemEntity itemEntity);

    public List<ItemPriceEntity> findTop2ByItemOrderByDateFromDesc(ItemEntity itemEntity);

    public void deleteAllByItemId(Long itemId);
}
