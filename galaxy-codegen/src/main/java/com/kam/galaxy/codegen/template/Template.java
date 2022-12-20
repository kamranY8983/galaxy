package com.kam.galaxy.codegen.template;

import com.google.common.io.Resources;
import com.kam.galaxy.common.exception.GalaxyException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Kamran Y. Khan
 * @since 19-Dec-2022
 *
 * Wrapper class for template to be used by Generator Engine
 */
public record Template(String name, String body) {

    /**
     * Load template content from String
     *
     * @param templateName name of the template for tracing purposes
     * @param templateContent content of the template
     * @return instance of {@link Template} class for given String template
     */
    public static Template fromString(String templateName, String templateContent){
        return new Template(templateName, templateContent);
    }

    /**
     * Load template content from String
     *
     * @param templateContent content of the template
     * @return instance of {@link Template} class for given String template
     */
    public static Template fromString(String templateContent){
        return fromString(templateContent, templateContent);
    }

    /**
     * Loand template content from String
     *
     * @param templateName name of the template in the resources
     * @return instance of {@link Template} class for given String template
     */
    public static Template fromResources(String templateName){
        try{
            final URL url = Resources.getResource(templateName);
            return fromString(templateName, Resources.toString(url, StandardCharsets.UTF_8));
        } catch (IOException ex){
            throw new GalaxyException(
                    "Failed to load template from resource '%s'".formatted(templateName), ex);
        }
    }

    @Override
    public String toString(){
        return name;
    }


}
