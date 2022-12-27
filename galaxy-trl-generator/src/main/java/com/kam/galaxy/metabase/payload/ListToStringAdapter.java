package com.kam.galaxy.metabase.payload;

import com.google.common.base.Splitter;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.List;

/**
 * @author Kamran Y. Khan
 * @since 25-Dec-2022
 */
public class ListToStringAdapter {

    @FromJson
    @ListToString
    List<String> fromJson(String sectors){
        return sectors.contains(",")
                ? Splitter.on(',').trimResults().splitToList(sectors)
                : List.of(sectors);
    }

    @ToJson
    public String toJson(@ListToString List<String> sectors){
        throw new UnsupportedOperationException("Serialization to JSON is not supported");
    }
}
