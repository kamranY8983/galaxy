package com.kam.galaxy.codegen.template.directive;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Kamran Y. Khan
 * @since 22-Dec-2022
 */
public abstract class CustomDirective<T> extends Directive {

    public abstract String render(T value);

    @Override
    public String getName() {
        final String className = this.getClass().getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        writer.write(render((T) node.jjtGetChild(0).value(internalContextAdapter)));
        return false;
    }

    public abstract static class LineDirective<T> extends CustomDirective<T>{
        @Override
        public int getType() {
            return LINE;
        }
    }

    public abstract static class BlockDirective<T> extends CustomDirective<T>{
        @Override
        public int getType() {
            return BLOCK;
        }
    }
}
