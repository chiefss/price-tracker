package org.devel.pricetracker.application.factory;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.devel.pricetracker.application.dto.ItemPriceDto;
import org.devel.pricetracker.application.entities.ItemPrice;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemPriceDtoFactory {
    
    public static ItemPriceDto create(ItemPrice itemPrice) {
        return new ItemPriceDto(
                itemPrice.getId(),
                itemPrice.getItem().getId(),
                itemPrice.getPrice(),
                itemPrice.getCreatedAt());
    }

    public static ItemPriceDto create(Long itemId, Double price) {
        return new ItemPriceDto(null, itemId, price, null);
    }
}
