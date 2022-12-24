package com.kam.galaxy.codegen.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Bucket is a top level container with data for the template engine.
 *
 * <p>It contains one or multiple {@link Bucket}s , have name,
 * and can have metadata in the format of String key -> Object value</p>
 * Also model can be easily copied using
 *
 * @author Kamran Y. Khan
 * @since 20-Dec-2022
 */
public class Model extends HasMetaData<Model> {

    private final Map<String, Bucket> buckets = new LinkedHashMap<>();

    private final String name;

    public Model(String name) {
        this.name = checkNotNull(name, "Model name can't be null");
    }

    public Model(Model model, String newName) {
        this(newName);
        addMetadata(model.metadata);
        model.getBuckets().forEach(bucket -> addBucket(new Bucket(bucket)).addMetadata(bucket.metadata));
    }

    public static Model create(String name){
        return new Model(name);
    }

    public Model copy(String newName){
        return new Model(this, newName);
    }

    public String getName() {
        return name;
    }

    public int size() {
        return buckets.size();
    }

    public Bucket getBucket(String bucketName) {
        checkArgument(hasBucket(bucketName), "No bucket with name '%s' exist in model '%s'!".formatted(bucketName, name));
        return buckets.get(bucketName);
    }

    public List<Bucket> getBuckets() {
        return List.copyOf(buckets.values());
    }

    Bucket addBucket(Bucket bucket) {
        checkArgument(!hasBucket(bucket.getName()), "Bucket with name '%s' already exist in model '%s'!".formatted(bucket.getName(), name));
        buckets.put(bucket.getName(), bucket);
        return bucket;
    }

    public Bucket addBucket(String bucketName) {
        return addBucket(new Bucket(bucketName));
    }

    public boolean hasBucket(String bucketName) {
        return buckets.containsKey(bucketName);
    }

    public Bucket getOrAddBucket(String bucketName) {
        return hasBucket(bucketName) ? getBucket(bucketName) : addBucket(bucketName);
    }

    public boolean isEmpty() {
        return buckets.isEmpty();
    }

    public Model trim() {
        buckets.values().forEach(Bucket::trim);
        buckets.values().removeIf(Bucket::isEmpty);
        return this;
    }


}
