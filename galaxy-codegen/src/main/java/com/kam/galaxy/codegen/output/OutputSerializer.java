package com.kam.galaxy.codegen.output;

import java.util.Map;

/**
 * @author Kamran Y. Khan
 * @since 20-Dec-2022
 */
public interface OutputSerializer {

    /**
     * Perform actual content to file serialization using output format specific implementation.
     *
     * @param outputConfig
     * @param baseDir
     * @param content
     * @param context
     */
    void serialize(
            OutputFormatConfig outputConfig,
            String baseDir,
            String content,
            Map<String, Object> context);
}
