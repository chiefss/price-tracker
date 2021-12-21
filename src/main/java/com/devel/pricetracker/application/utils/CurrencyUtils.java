package com.devel.pricetracker.application.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class CurrencyUtils {

    public static String formatCurrency(Float currency) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(' ');
        DecimalFormat formatter = new DecimalFormat("###,###.##", symbols);
        return formatter.format(currency);
    }

}
