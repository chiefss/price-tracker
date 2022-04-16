package com.devel.pricetracker.application.models.repository;

import com.devel.pricetracker.application.models.entities.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends CrudRepository<ItemEntity, Long>, JpaSpecificationExecutor<ItemEntity>, JpaRepository<ItemEntity, Long> {
}
