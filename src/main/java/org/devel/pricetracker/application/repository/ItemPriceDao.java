package org.devel.pricetracker.application.repository;

import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.devel.pricetracker.application.entities.ItemPrice;
import org.devel.pricetracker.application.entities.QItemPrice;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ItemPriceDao extends AbstractDao<ItemPrice, Long> {

    private static final QItemPrice itemPrice = QItemPrice.itemPrice;

    public ItemPriceDao(EntityManager entityManager) {
        super(ItemPrice.class, entityManager);
    }


    public List<ItemPrice> findAllByItemId(Long itemId, Integer limit) {
        JPAQuery<ItemPrice> sql = getQuery()
                .selectFrom(itemPrice)
                .where(itemPrice.item.id.eq(itemId))
                .orderBy(itemPrice.createdAt.desc());
        if (limit != null) {
            sql.limit(limit);
        }
        return sql.fetch();
    }


    public void deleteAllByItemId(Long itemId) {
        getQuery()
                .delete(itemPrice)
                .where(itemPrice.item.id.eq(itemId))
                .execute();
    }
}
