package com.kam.galaxy.codegen.model;

import com.google.common.base.Predicate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Bucket is a mid level container with data for the template engine.
 *
 * <p>Buckets are stored inside the {@link Model}, have name, contain one or more {@link Record}s
 * and can have metadata in the format of String key -> Object value</p>
 *
 * @author Kamran Y. Khan
 * @since 20-Dec-2022
 */
public class Bucket extends HasMetaData<Bucket> {

    private final Map<String, Record> records = new LinkedHashMap<>();

    private final String name;

    public Bucket(String name) {
        this.name = checkNotNull(name, "Bucket name can't be null");
    }

    public Bucket(Bucket bucket) {
        this(bucket.getName());
        addMetadata(bucket.metadata);
        bucket.getRecords().forEach(record -> addRecord(new Record(record)).addMetadata(record.metadata));
    }

    public String getName() {
        return name;
    }

    public int size() {
        return records.size();
    }

    public Record getRecord(String recordName) {
        checkArgument(hasRecord(recordName), "No record with name '%s' exist in bucket '%s'!".formatted(recordName, name));
        return records.get(recordName);
    }

    public List<Record> getRecords() {
        return List.copyOf(records.values());
    }

    Record addRecord(Record record) {
        checkArgument(!hasRecord(record.getName()), "Record with name '%s' already exist in bucket '%s'!".formatted(record.getName(), name));
        records.put(record.getName(), record);
        return record;
    }

    public Record addRecord(String recordName) {
        return addRecord(new Record(recordName));
    }

    public boolean hasRecord(String recordName) {
        return records.containsKey(recordName);
    }

    public Record getOrAddRecord(String recordName) {
        return hasRecord(recordName) ? getRecord(recordName) : addRecord(recordName);
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }

    public Bucket trim() {
        records.values().removeIf(Record::isEmpty);
        return this;
    }


}
