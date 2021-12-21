package org.devel.pricetracker.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.devel.pricetracker.application.entities.Item;
import org.devel.pricetracker.application.entities.ItemPrice;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PriceParserResultDto {

    private Item item;
    private ItemPrice itemPrice;
    private boolean success;
    private String message;
}
