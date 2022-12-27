package com.kam.galaxy.metabase.model;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * @author Kamran Y. Khan
 * @since 25-Dec-2022
 */
public record ProductEntityDetails(List<EntityDetails> entity){

    public record EntityDetails(@Json(name = "disp_nm") String displayName) {}
}
