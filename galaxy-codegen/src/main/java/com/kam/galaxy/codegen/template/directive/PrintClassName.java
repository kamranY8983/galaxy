package com.kam.galaxy.codegen.template.directive;

import com.kam.galaxy.codegen.template.directive.CustomDirective.LineDirective;

/**
 * @author Kamran Y. Khan
 * @since 22-Dec-2022
 */
public class PrintClassName extends LineDirective<Class<?>> {
    @Override
    public String render(Class value) {
        if(value == null)
            return null;

        return value.getName().startsWith("java.lang") ? value.getSimpleName() : value.getCanonicalName();
    }
}
