package com.kam.galaxy.codegen.utils;

import com.thoughtworks.qdox.parser.ParseException;

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

        }
        return ex;
    }
}
