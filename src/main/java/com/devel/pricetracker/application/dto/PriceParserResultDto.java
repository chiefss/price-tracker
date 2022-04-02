package com.devel.pricetracker.application.dto;

import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PriceParserResultDto {

    private ItemEntity item;
    private ItemPriceEntity itemPrice;
    private boolean success;
    private String message;
}
