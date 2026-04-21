package com.habbashx.axiomhttp.parser;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Utility class for converting between HTTP header representations.
 *
 * <p>The framework uses a {@code Map<String, List<String>>} internally (same format as
 * {@code java.net.http.HttpHeaders#map()}) but {@code HttpRequest.Builder#headers(String...)}
 * expects a flat alternating {@code [name, value, name, value, ...]} array.
 * This class bridges the two representations.
 *
 * <p>All methods are static; this class is not instantiable.
 *
 * <h3>Key normalisation</h3>
 * Header names are lower-cased when parsing to ensure case-insensitive lookup,
 * matching the behaviour of {@code java.net.http.HttpHeaders}.
 */
public class HeaderParser {

    /** Utility class — no instances. */
    private HeaderParser() {}

    /**
     * Parses a flat alternating {@code [name, value, name, value, ...]} array into a
     * {@code Map<String, List<String>>}.
     *
     * <p>Header names are lower-cased. If two entries share the same (normalised) name,
     * their values are accumulated in the same list.
     *
     * <p>If the array has an odd length the last element is silently ignored.
     *
     * @param headers a flat alternating name/value array, as accepted by
     *                {@code HttpRequest.Builder#headers(String...)}; {@code null} returns an empty map
     * @return a mutable map from lower-cased header name to its list of values; never {@code null}
     */
    public static @NotNull Map<String, List<String>> parseHeaders(String[] headers) {

        Map<String, List<String>> map = new HashMap<>();

        if (headers == null) return map;

        for (int i = 0; i < headers.length - 1; i += 2) {
            String name  = headers[i].toLowerCase();
            String value = headers[i + 1];

            map.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        }

        return map;
    }

    /**
     * Converts a {@code Map<String, List<String>>} back into a flat alternating
     * {@code [name, value, name, value, ...]} array suitable for
     * {@code HttpRequest.Builder#headers(String...)}.
     *
     * <p>If a header has multiple values each value produces its own {@code name, value} pair.
     *
     * @param map the header map to convert; must not be {@code null}
     * @return a flat string array of alternating name/value pairs; never {@code null}
     */
    public static String @NotNull [] toHeaderArray(@NotNull Map<String, List<String>> map) {

        List<String> result = new ArrayList<>();

        for (var entry : map.entrySet()) {
            String name             = entry.getKey();
            List<String> values     = entry.getValue();

            for (String value : values) {
                result.add(name);
                result.add(value);
            }
        }

        return result.toArray(new String[0]);
    }
}
