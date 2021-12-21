package org.devel.pricetracker.application.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyUtils {

    public static String formatCurrency(double currency) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(' ');
        DecimalFormat formatter = new DecimalFormat("###,###.##", symbols);
        return formatter.format(currency);
    }

    public static double getCurrencySubstring(String string) throws IOException {
        String replaceAll = string.replaceAll("[^0-9,.]", "");
        if (replaceAll.indexOf(".") > replaceAll.indexOf(",")) {
            replaceAll = replaceAll.replace(",", "");
        } else {
            replaceAll = replaceAll.replace(".", "").replace(",", ".");
        }
        if (replaceAll.length() > 0) {
            return Double.parseDouble(replaceAll);
        }
        throw new IOException("Substring does not contain currency");
    }

}
