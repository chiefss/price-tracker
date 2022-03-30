package com.devel.pricetracker.application.parsers;


import com.devel.pricetracker.application.dto.PriceParserResultDto;
import com.devel.pricetracker.application.models.entities.ItemEntity;
import com.devel.pricetracker.application.models.entities.ItemPriceEntity;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface PriceParser {

    public List<PriceParserResultDto> parseAll();
    public Optional<ItemPriceEntity> parse(ItemEntity itemEntity) throws IOException, NotFoundException;
}
