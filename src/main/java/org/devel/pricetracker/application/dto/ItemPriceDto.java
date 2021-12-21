package org.devel.pricetracker.application.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class ItemPriceDto {

    private Long id;
    private Long itemId;
    private Double price;
    private String formattedPrice;
    private LocalDateTime createdAt;

    public ItemPriceDto(Long id, Long itemId, Double price, LocalDateTime createdAt) {
        this.id = id;
        this.itemId = itemId;
        this.price = price;
        this.createdAt = createdAt;
    }
}
