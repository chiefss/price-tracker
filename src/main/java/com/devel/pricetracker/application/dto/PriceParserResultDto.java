package com.devel.pricetracker.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class PriceParserResultDto {

    public void addReduceMessage(String reduceMessage) {
        reduceMessages.add(reduceMessage);
    }

    public boolean hasReduceMessages() {
        return reduceMessages.size() > 0;
    }

    public void addErrorMessage(String errorMessage) {
        errorMessages.add(errorMessage);
    }

    public boolean hasErrorMessages() {
        return errorMessages.size() > 0;
    }

    private List<String> reduceMessages = new ArrayList<>();
    private List<String> errorMessages = new ArrayList<>();
}
