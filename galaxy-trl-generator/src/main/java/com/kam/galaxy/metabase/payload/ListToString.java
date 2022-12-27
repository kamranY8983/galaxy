package com.kam.galaxy.metabase.payload;

import com.squareup.moshi.JsonQualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Kamran Y. Khan
 * @since 25-Dec-2022
 */
@Retention(RetentionPolicy.RUNTIME)
@JsonQualifier
public @interface ListToString {
}
