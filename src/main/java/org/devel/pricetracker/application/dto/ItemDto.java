package org.devel.pricetracker.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ItemDto {

    private Long id;
    private String name;
    private String url;
    private String selector;
    private String breakSelector;
}
