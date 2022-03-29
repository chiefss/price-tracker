package com.devel.pricetracker.application.utils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class CurrencyUtils {

    public static String formatCurrency(Float currency) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(' ');
        DecimalFormat formatter = new DecimalFormat("###,###.##", symbols);
        return formatter.format(currency);
    }

    public static Float getCurrencySubstring(String string) throws IOException {
        String replaceAll = string.replaceAll("[^0-9,.]", "");
        if (replaceAll.indexOf(".") > replaceAll.indexOf(",")) {
            replaceAll = replaceAll.replace(",", "");
        } else {
            replaceAll = replaceAll.replace(".", "").replace(",", ".");
        }
        if (replaceAll.length() > 0) {
            return Float.valueOf(replaceAll);
        }
        throw new IOException("Substring does not contain currency");
    }

}
