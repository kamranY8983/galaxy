package com.kam.galaxy.codegen.template.directive;

/**
 * @author Kamran Y. Khan
 * @since 27-Dec-2022
 */
public class AsAttributeHeader extends CustomDirective.LineDirective<String> {


    @Override
    public String render(String value) {
        if(value == null || value.isEmpty()){
            return value;
        }

        //remove extra spaces
        final String trimmedValue = value.trim().replaceAll("\\s+", " ");

        if(!Character.isJavaIdentifierStart(trimmedValue.charAt(0))) {
            return render("_" + trimmedValue);
        }

        final var sb = new StringBuilder();
        sb.append(trimmedValue.charAt(0));
        for(int i = 1; i < trimmedValue.length(); i++){
            final char character = trimmedValue.charAt(i);
            if(Character.isWhitespace(character)) {
                sb.append("_");
            } else if (Character.isJavaIdentifierPart(character)) {
                sb.append(trimmedValue.charAt(i));
            }
        }
        return sb.toString().toUpperCase();
    }
}
