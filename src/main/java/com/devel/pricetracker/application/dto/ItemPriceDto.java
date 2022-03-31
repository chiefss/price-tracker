package com.devel.pricetracker.application.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class ItemPriceDto {

    public ItemPriceDto(Long id, Long itemId, Float price, LocalDateTime dateFrom) {
        this.id = id;
        this.itemId = itemId;
        this.price = price;
        this.dateFrom = dateFrom;
    }

    private Long id;
    private Long itemId;
    private Float price;
    private String formatedPrice;
    private LocalDateTime dateFrom;

}
