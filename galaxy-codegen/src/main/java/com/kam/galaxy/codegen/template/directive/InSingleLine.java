package com.kam.galaxy.codegen.template.directive;

import com.kam.galaxy.codegen.template.directive.CustomDirective.LineDirective;

/**
 * @author Kamran Y. Khan
 * @since 22-Dec-2022
 */
public class InSingleLine extends LineDirective<String> {
    @Override
    public String render(String value) {
        if(value == null || value.isEmpty())
            return value;

        return value.replaceAll("\\r\\n|\\r|\\n", "");
    }
}
