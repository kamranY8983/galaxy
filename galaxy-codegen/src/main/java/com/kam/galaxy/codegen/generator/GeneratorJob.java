package com.kam.galaxy.codegen.generator;

import com.kam.galaxy.codegen.model.Model;
import com.kam.galaxy.codegen.output.OutputCategory;
import com.kam.galaxy.codegen.output.OutputFormat;
import com.kam.galaxy.codegen.output.OutputFormatConfig;
import com.kam.galaxy.codegen.template.Template;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Kamran Y. Khan
 * @since 19-Dec-2022
 */
@RecordBuilder
public record GeneratorJob(
    // Template to be used by template engine */
    Template template,
    // Model to be used by template engine */
    Supplier<Model> model,
    // Additional context data to be included into the template engine context
    Consumer<Map<String, Object>> context,
    // Scope of generator job
    GeneratorScope scope,
    // Category of the generated data
    OutputCategory category,
    // Output format to be used for deserializing data
    OutputFormat format,
    // Configuration to the output format
    Consumer<OutputFormatConfig> outputConfig) {}
