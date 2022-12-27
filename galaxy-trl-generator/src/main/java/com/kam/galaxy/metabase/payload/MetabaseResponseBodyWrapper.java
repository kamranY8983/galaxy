package com.kam.galaxy.metabase.payload;

import java.util.List;

/**
 * @author Kamran Y. Khan
 * @since 25-Dec-2022
 */
public record MetabaseResponseBodyWrapper<T>(String message, boolean success, List<T> result) {
}
