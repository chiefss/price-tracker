package com.devel.pricetracker.application.dto;

import com.devel.pricetracker.application.models.entities.ItemEntity;
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
    private boolean success;
    private String message;
}
