package com.devel.pricetracker.application.parsers;


import com.devel.pricetracker.application.models.entities.ItemEntity;
import javassist.NotFoundException;

import java.io.IOException;

public interface PriceParser {

    public void parseAll();
    public boolean parse(ItemEntity itemEntity) throws IOException, NotFoundException;
}
