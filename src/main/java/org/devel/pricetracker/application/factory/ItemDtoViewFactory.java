package org.devel.pricetracker.application.factory;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.devel.pricetracker.application.dto.ItemDtoView;
import org.devel.pricetracker.application.entities.Item;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemDtoViewFactory {

    public static ItemDtoView create(Item item) {
        return new ItemDtoView(
                item.getId(),
                item.getName(),
                item.getUrl(),
                item.getSelector(),
                item.getBreakSelector(),
                null,
                item.getCreatedAt(),
                item.getDeletedAt());
    }
}
