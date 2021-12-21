package com.devel.pricetracker.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ItemDtoView {

    public ItemDtoView(Long id, String name, String url, String selector, String breakSelector, List<ItemPriceDtoView> prices, LocalDateTime dateFrom, LocalDateTime dateTo) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.selector = selector;
        this.breakSelector = breakSelector;
        this.prices = prices;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public boolean isDeltaPlus() {
        return delta > 0;
    }

    public boolean isDeltaMinus() {
        return delta < 0;
    }

    private Long id;
    private String name;
    private String url;
    private String host;
    private String selector;
    private String breakSelector;
    private List<ItemPriceDtoView> prices = new ArrayList();
    private String formatedLastPrice;
    private Float delta;
    private String formatedDelta;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;

}
