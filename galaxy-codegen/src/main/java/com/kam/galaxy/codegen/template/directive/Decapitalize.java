package com.kam.galaxy.codegen.template.directive;

import com.kam.galaxy.codegen.template.directive.CustomDirective.LineDirective;

/**
 * @author Kamran Y. Khan
 * @since 22-Dec-2022
 */
public class Decapitalize extends LineDirective<String> {
    @Override
    public String render(String value) {
        if(value == null || value.isEmpty())
            return value;

        char[] c = value.toCharArray();
        c[0] = Character.toLowerCase(c[0]);

        return new String(c);
    }
}
