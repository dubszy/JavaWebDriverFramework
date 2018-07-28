package com.mwaltman.podriverframework.common.session;

import com.google.common.collect.ImmutableMap;
import com.mwaltman.podriverframework.common.exception.StoreException;
import com.mwaltman.podriverframework.common.util.RegexUtil;
import org.apache.commons.exec.util.MapUtils;
import org.testng.log4testng.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Store {

    private final Logger log = Logger.getLogger(Store.class);

    private ImmutableMap<String, Object> internalStore;

    /**
     * Get an item from the internal store map.
     *
     * @param key The key to look for
     *
     * @return An object from the map paired with the supplied key
     */
    private Object get(String key) {
        if (key == null || key.isEmpty()) {
            throw new StoreException("Key cannot be null or empty");
        }
        Object item;
        try {
            item = internalStore.get(key);
        } catch (NullPointerException e) {
            throw new StoreException("Invalid key: " + key);
        }
        return item;
    }

    /**
     * Get an item (that is not a List or Map) from the store.
     *
     * @param key The key to look for
     * @param <T> The type to cast the Object returned to
     *
     * @return An item from the store that is paired with the supplied key (of type T)
     */
    @SuppressWarnings("unchecked")
    public <T> T getItem(String key) {
        return (T) get(key);
    }

    /**
     * Get a List from the store.
     *
     * @param key The key to look for
     * @param <T> The template type for objects inside the list
     *
     * @return A List from the store that is paired with the supplied key (containing objects of template type T)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        return new ArrayList<>((List<T>) get(key));
    }

    /**
     * Get a Map from the store.
     *
     * @param key The key to look for
     * @param <K> The template type for keys inside the map
     * @param <V> The template type for values inside the map
     *
     * @return A Map from the store that is paired with the supplied key (containing key-value pairs of template types K and V, respectively)
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getMap(String key) {
        return MapUtils.copy((Map<K, V>) get(key));
    }

    /**
     * Insert a value into the store. Since the internal store is immutable, this creates a copy of the existing
     * internal store and reassigns the {@code internalStore} field to the newly created copy.
     *
     * @param key The key to put
     * @param item The item (value) to put
     */
    public void put(String key, Object item) {
        Map<String, Object> map;
        if (internalStore == null) {
            map = new HashMap<>();
        } else {
            map = new HashMap<>(internalStore);
        }
        map.put(key, item);
        internalStore = ImmutableMap.copyOf(map);
    }

    /**
     * Get an item from the store via string interpolation. Interpolation templates must take the format
     * ${stringToInterpolate}
     *
     * @param s The template interpolation string
     *
     * @return The item from the store that matches the interpolated string
     *
     * @throws StoreException If a key matching the interpolated string was not found in the store
     */
    public String interpolate(final String s) {
        String result = s;
        Pattern pattern;
        Matcher matcher;

        do {
            pattern = Pattern.compile(RegexUtil.INTERPOLATE_SINGLE.getPattern());
            matcher = pattern.matcher(result);

            if (matcher.find()) {
                String found = matcher.group();
                try {
                    result = result.replace(found, getItem(purgeCharactersFromString(found, "${}")));
                } catch (StoreException e) {
                    throw new StoreException("Attempted to interpolate '" + found + "' but a match was not found in the store");
                }
            } else {
                break;
            }
        } while (true);
        return result;
    }

    /**
     * Remove supplied characters from a supplied string.
     *
     * @param source The source string to remove characters from
     * @param chars The list of characters (as a string) to remove from the source string
     *
     * @return The source string with the supplied characters removed
     */
    private String purgeCharactersFromString(String source, String chars) {
        String src = source;
        for (Character c : chars.toCharArray()) {
            src = src.replace(c.toString(), "");
        }
        return src;
    }
}
