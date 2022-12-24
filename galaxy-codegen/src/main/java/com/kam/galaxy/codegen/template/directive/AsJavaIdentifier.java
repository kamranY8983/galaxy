package com.kam.galaxy.codegen.template.directive;

import com.kam.galaxy.codegen.template.directive.CustomDirective.LineDirective;
/**
 * @author Kamran Y. Khan
 * @since 22-Dec-2022
 */
public class AsJavaIdentifier extends LineDirective<String> {
    @Override
    public String render(String value) {
        if(value == null || value.isEmpty())
            return value;

        if(!Character.isJavaIdentifierStart(value.charAt(0))){
            return render("_" + value);
        }

        final var sb = new StringBuilder();
        sb.append(value.charAt(0));
        for(int i = 1; i < value.length(); i++){
            if(Character.isJavaIdentifierPart(value.charAt(i))){
                sb.append(value.charAt(i));
            }
        }
        return sb.toString();
    }
}
