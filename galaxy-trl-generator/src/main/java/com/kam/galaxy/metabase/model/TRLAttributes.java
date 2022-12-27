package com.kam.galaxy.metabase.model;

import com.squareup.moshi.Json;

/**
 * @author Kamran Y. Khan
 * @since 26-Dec-2022
 */
public record TRLAttributes(

        @Json(name = "disp_nm")
        String displayName,
        @Json(name = "full_nm")
        String fullName,
        @Json(name = "sn")
        Integer serialNumber,
        @Json(name = "data_mdl_nm")
        String dataModelName,
        @Json(name = "data_enty_nm")
        String dataEntityName,
        String dataType,
        @Json(name = "datatype_len_precn")
        Integer dataTypeLengthPrecison,
        @Json(name = "datatype_scal")
        Integer dataTypeScale,
        @Json(name = "sta_cd")
        String statusCode
) {
        public boolean isRetired() {
                return statusCode.equals("Retired");
        }
}


