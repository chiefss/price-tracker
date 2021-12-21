package org.devel.pricetracker.application.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.persistence.EntityManager;
import org.devel.pricetracker.application.entities.Item;
import org.devel.pricetracker.application.entities.QItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ItemDao extends AbstractDao<Item, Long> {

    private static final QItem item = QItem.item;

    public ItemDao(EntityManager entityManager) {
        super(Item.class, entityManager);
    }

    public List<Item> findAllByActivated(boolean activatedOnly) {
        BooleanExpression where = null;
        if (activatedOnly) {
            where = item.deletedAt.isNull();
        }
        return getQuery()
                .selectFrom(item)
                .where(where)
                .orderBy(item.createdAt.asc(), item.name.asc())
                .fetch();
    }
}
