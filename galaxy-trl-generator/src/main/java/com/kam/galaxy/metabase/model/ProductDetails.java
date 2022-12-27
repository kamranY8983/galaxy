package com.kam.galaxy.metabase.model;

import com.kam.galaxy.metabase.payload.ListToString;
import com.squareup.moshi.Json;

import java.util.List;

/**
 * @author Kamran Y. Khan
 * @since 25-Dec-2022
 */
public record ProductDetails (
        @Json(name = "disp_nm") String displayName,
        String code,
        @ListToString List<String> sector){}
