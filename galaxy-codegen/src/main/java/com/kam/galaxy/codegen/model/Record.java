package com.kam.galaxy.codegen.model;

import com.google.common.base.Predicate;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Record is a low level container with data for the template engine.
 *
 * <p>Records are stored inside the {@link Bucket}, have name, contain one or more {@link Element}s
 * and can have metadata in the format of String key -> Object value</p>
 *
 * @author Kamran Y. Khan
 * @since 20-Dec-2022
 *
 */
public class Record extends HasMetaData<Record> {

    private final List<Element> elements = new LinkedList<>();

    private final String name;

    public Record(String name) {
        this.name = checkNotNull(name, "Record name can't be null");
    }

    public Record(Record record) {
        this(record.getName());
        addMetadata(record.metadata);
        record.getElements().forEach(
                element -> addElement(new Element(element)).addMetadata(element.metadata));
    }

    public String getName() {
        return name;
    }

    public int size() {
        return elements.size();
    }

    public List<Element> getElements() {
        return List.copyOf(elements);
    }

    Element addElement(Element element) {
//        checkArgument(element.getName());  Not needed because a Record can have duplicate elements
        elements.add(element);
        return element;
    }

    public Element addElement(String elementName) {
        return addElement(new Element(elementName));
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public Record trim(Predicate<Element> trimCriteria) {
        elements.removeIf(trimCriteria);
        return this;
    }


}
