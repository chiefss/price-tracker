package org.devel.pricetracker.application.factory;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.devel.pricetracker.application.dto.ItemPriceDtoView;
import org.devel.pricetracker.application.entities.ItemPrice;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemPriceDtoViewFactory {

    public static ItemPriceDtoView create(ItemPrice itemPrice) {
        return new ItemPriceDtoView(
                itemPrice.getId(),
                itemPrice.getPrice(),
                itemPrice.getCreatedAt());
    }
}
