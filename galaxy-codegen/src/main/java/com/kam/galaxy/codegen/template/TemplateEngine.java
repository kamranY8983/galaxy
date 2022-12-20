package com.kam.galaxy.codegen.template;

import com.kam.galaxy.common.exception.GalaxyException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.context.Context;

import java.io.StringWriter;
import java.util.Map;

/**
 * @author Kamran Y. Khan
 * @since 19-Dec-2022
 */
public enum TemplateEngine {
    INSTANCE;

    private TemplateEngine(){
        //initialize template engine
        Velocity.init();

        //TODO: load custom directive implementation
    }

    public String generate(Template template, Map<String, Object> context){
        //create and configure velocity context
        final VelocityContext velocityContext = new VelocityContext(context);
        //render referenced output without proper value as null instead of as-is
        final EventCartridge eventCartridge = new EventCartridge();

        //fix for putting "null" when the VTL expression evaluates to null
        eventCartridge.addReferenceInsertionEventHandler(
                (Context cntxt, String reference, Object value) -> value == null ? "null" : value);

        velocityContext.attachEventCartridge(eventCartridge);

        //generate content using template and context
        final var contentWriter = new StringWriter();
        final boolean renderingSucceed = Velocity.evaluate(velocityContext, contentWriter, template.name(), template.body());
        if(!renderingSucceed){
            throw new GalaxyException("Template engine failed process, check logs for details!");
        }
        return contentWriter.toString();
    }
}
