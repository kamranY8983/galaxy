package com.kam.galaxy.codegen.output;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.kam.galaxy.codegen.template.Template;
import com.kam.galaxy.codegen.template.TemplateEngine;
import com.kam.galaxy.common.exception.GalaxyException;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Kamran Y. Khan
 * @since 20-Dec-2022
 */
public enum OutputFormat implements OutputSerializer {

    /** Content serializer for plain text content */
    TEXT {
        @Override
        public void serialize(OutputFormatConfig outputConfig, String baseDir, String content, Map<String, Object> context) {
            var fileDir = checkNotNull(outputConfig.fileDir(), "fileDir value can't be null");
            var fileName = checkNotNull(outputConfig.fileName(), "fileName value can't be null");
            var fileExt = checkNotNull(outputConfig.fileExt(), "fileExt value can't be null");

            //if context is provided, use TemplateEngine to process fileDir/fileName/fileExt as
            // they can be set using template syntax
            if(context != null){
                if(!fileDir.isEmpty()){
                    fileDir = TemplateEngine.INSTANCE.generate(Template.fromString(fileDir), context);
                }
                if(fileName.isEmpty()){
                    fileName = TemplateEngine.INSTANCE.generate(Template.fromString(fileName), context);
                }
                if(fileExt.isEmpty()){
                    fileExt = TemplateEngine.INSTANCE.generate(Template.fromString(fileExt), context);
                }
            }

            // create output file for generated data
            final var outputFile =
                    new File(
                            new StringBuilder(baseDir)
                                    .append(File.separator)
                                    .append(fileDir)
                                    .append(File.separator)
                                    .append(fileName)
                                    .append(".")
                                    .append(fileExt)
                                    .toString());
            log.debug("Writing generated code to file '{}'", outputFile.getAbsolutePath());
            // write generated data to the file
            try{
                Files.createParentDirs(outputFile);
                Files.asCharSink(outputFile, StandardCharsets.UTF_8).write(content);
            } catch (IOException ex){
                throw new GalaxyException("Failed to serialize content to file: ", ex);
            }
        }
    },

    /** Content serializer for Java code */
    JAVA {

        @Override
        public void serialize(OutputFormatConfig outputConfig, String baseDir, String content, Map<String, Object> context) {

            //notify that OutputFormatCoinfig is not supported by JAVA format serializer
            if(outputConfig != null){
                log.warn(
                        "JAVA output format doesn't support format config, ignoring supplied '%s'".formatted(outputConfig));
            }

            //parse sources as a java code to determine package (to identify directory location)
            // and class name( to identify class name) and use them for serialization
            final var source = CharSource.wrap(content);
            final var codeParser = new JavaProjectBuilder();
            try{
                codeParser.addSource(source.openStream());
            } catch (IOException ex){
                throw new GalaxyException("Failed to generate file: ", ex);
            } catch (ParseException ex){
                //CodegenUtils
            }

            final var generatedClasses = codeParser.getClasses();
            if(generatedClasses.isEmpty()){
                throw new GalaxyException("Generated java file must contain atlease one class");
            }
            else {
                final var firstClass = generatedClasses.iterator().next();
                final var className = firstClass.getSimpleName();
                final var packages = firstClass.getPackageName().replace('.', File.separatorChar);

                // serialize content using TEXT serializer with custom format configuration
                TEXT.serialize(
                        new OutputFormatConfig()
                                .fileDir(packages)
                                .fileName(className)
                                .fileExt("java"),
                        baseDir,
                        content,
                        null);
            }
        }
    };

    private static final Logger log = LoggerFactory.getLogger(OutputFormat.class);
    }
