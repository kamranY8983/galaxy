package com.kam.galaxy.codegen.model;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Element is a lowest level element with data for the template engine.
 *
 * <p>Elements are stored inside the {@link Record}, have name,
 * and can have metadata in the format of String key -> Object value</p>
 *
 * @author Kamran Y. Khan
 * @since 20-Dec-2022
 *
 */
public class Element extends HasMetaData<Element> {

    private final String name;

    public Element(String name) {
        this.name = checkNotNull(name, "Element name can't be null");
    }

    public Element(Element element) {
        this(element.getName());
        addMetadata(element.metadata);
    }

    public String getName() {
        return name;
    }
}
