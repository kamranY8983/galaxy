package com.kam.galaxy.codegen.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kamran Y. Khan
 * @since 20-Dec-2022
 *
 * Abstract class with implementation of metadata holder in the format of String Key -> Object value
 *
 * @param <T> type of the element extending this class
 */
public abstract class HasMetaData<T> {

    protected Map<String, Object> metadata = new HashMap<>();

    public T addMetadata(Map<String, Object> metadata){
        this.metadata.putAll(metadata);
        return (T)this;
    }

    public T addMetadata(String key, Object value){
        this.metadata.put(key, value);
        return (T)this;
    }

    public Object getMetadata(String key){
        return this.metadata.get(key);
    }

}
