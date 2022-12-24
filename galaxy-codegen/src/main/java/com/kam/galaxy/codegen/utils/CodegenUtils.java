package com.kam.galaxy.codegen.utils;

import com.thoughtworks.qdox.parser.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Kamran Y. Khan
 * @since 20-Dec-2022
 */
public final class CodegenUtils {

    private CodegenUtils(){

    }

    public static ParseException enhancedParseException(String source, ParseException ex){
        final int line = ex.getLine() - 1;
        final int column = ex.getColumn() - 1;
        final var sourceInfoBuilder = new StringBuilder("\n");
        if(line >= 0){
            //if column  index with error is available, we will create an arrow pointing to this index
            //in error message and add previous and next lines for better context
            if(column >= 0){
                final String[] splittedSource = source.split("\n");
                if(line > 0) {
                    sourceInfoBuilder.append("•").append(splittedSource[line -1]).append("\n");
                }
                sourceInfoBuilder
                        .append("•")
                        .append(splittedSource[line])
                        .append("\n")
                        .append(" ".repeat(column + 1))
                        .append("^\n")
                        .append(" ".repeat(column + 1))
                        .append("|\n")
                        .append("-".repeat(column + 2));
                if (line < splittedSource.length - 1) {
                    sourceInfoBuilder.append("\n").append("•").append(splittedSource[line + 1]);
                }
            } else {
                sourceInfoBuilder.append(source.split("\n")[ex.getLine()]);
            }
        }
        try{
            final Path tempFile = Files.createTempFile("jedi-codegen-", ".java");
            Files.write(tempFile, source.getBytes());
            sourceInfoBuilder

                    .append("\nComplete generated source is available at ")
                    .append(tempFile.toFile());
        } catch (IOException ioEx){
            //can't do anything here
        }
//        System.out.println(sourceInfoBuilder);
        ex.setSourceInfo(sourceInfoBuilder.toString());
        return ex;
    }
}
