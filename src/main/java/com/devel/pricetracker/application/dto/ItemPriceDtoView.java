package com.devel.pricetracker.application.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class ItemPriceDtoView {

    public ItemPriceDtoView(Long id, Float price, LocalDateTime dateFrom) {
        this.id = id;
        this.price = price;
        this.dateFrom = dateFrom;
    }

    private Long id;
    private Float price;
    private String formatedPrice;
    private LocalDateTime dateFrom;

}
