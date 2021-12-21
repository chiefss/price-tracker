package org.devel.pricetracker.application.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Defines {

    public static final String API_PREFIX = "/api/";
    public static final int PARSER_MAX_THREADS = 6;

    public static final int PARSER_THREAD_SLEEP_MIN_SECOND = 1;
    public static final int PARSER_THREAD_SLEEP_MAX_SECOND = 3;

    public static final String API_ERROR_TEMPLATE_NAME = "empty";
    public static final String WEB_ERROR_TEMPLATE_NAME = "error";

    public static final int DETAIL_PRICES_LIMIT = 10;
}
