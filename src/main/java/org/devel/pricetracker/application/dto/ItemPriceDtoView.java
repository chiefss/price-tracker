package org.devel.pricetracker.application.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class ItemPriceDtoView {

    private Long id;
    private Double price;
    private String formattedPrice;
    private LocalDateTime createdAt;

    public ItemPriceDtoView(Long id, Double price, LocalDateTime createdAt) {
        this.id = id;
        this.price = price;
        this.createdAt = createdAt;
    }
}
