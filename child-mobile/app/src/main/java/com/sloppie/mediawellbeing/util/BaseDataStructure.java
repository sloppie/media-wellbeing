package com.sloppie.mediawellbeing.util;

/**
 * This is the data structure that will be used across complex data types in the Application.
 * This data structure will allow for the fetching of a key value that will be used to sort and/or
 * perform unique operations on the Data Structure itself.
 * The key gotten from {@link #getKey()} may be of type {@link java.lang.String} if the key is
 * used to only store uniqueness. It can also be of type {@link java.lang.Integer} if the key will
 * be used for sorting purposes.
 */
public interface BaseDataStructure<T> {
    T getKey();
}
