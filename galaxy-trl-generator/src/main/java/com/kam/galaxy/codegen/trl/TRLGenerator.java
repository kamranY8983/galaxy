package com.kam.galaxy.codegen.trl;

import com.kam.galaxy.codegen.generator.GeneratorEngine;
import com.kam.galaxy.codegen.generator.GeneratorJobBuilder;
import com.kam.galaxy.codegen.generator.GeneratorScope;
import com.kam.galaxy.codegen.model.Bucket;
import com.kam.galaxy.codegen.model.Model;
import com.kam.galaxy.codegen.output.OutputCategory;
import com.kam.galaxy.codegen.output.OutputFormat;
import com.kam.galaxy.codegen.template.Template;
import com.kam.galaxy.common.exception.GalaxyException;
import com.kam.galaxy.metabase.MetabaseService;
import com.kam.galaxy.metabase.model.ProductDetails;
import com.kam.galaxy.metabase.model.TRLAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Option;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Kamran Y. Khan
 * @since 26-Dec-2022
 */
public class TRLGenerator extends GeneratorEngine {

    private static final Logger log = LoggerFactory.getLogger(TRLGenerator.class);
    private static final MetabaseService METABASE_SERVICE = new MetabaseService();
    private static final Model productsModel = Model.create("Products");
    private static final Model productEntitiesModel = Model.create("Products Entities");
    private static final Map<String, Model> trlModels = new HashMap<>();
    private static final Consumer<Map<String, Object>> BASE_PACKAGE_CONTEXT = cntx -> cntx.put("basePackage", "com.kam.galaxy.genesys");

    @Option(
            names = {"-trl", "--trl-version"},
            description = "Mandatory parameter to define one or more TRL version to perform code-generation for.",
            required = true)
    String[] trlVersions;
    @Option(
            names = {"-p", "--product"},
            description = "Optional parameter to define one or more Products",
            required = false)
    private List<String> products;

    private static void populateBaseModels(List<String> products) {
        log.info("Started populating products and productsEntities models...");
        final Bucket productsBucket = productsModel.getOrAddBucket("Products");
        METABASE_SERVICE.getProductDetails()
                .stream()
                .filter(product -> products == null || products.contains(product.displayName()))
                .forEach(
                        product ->
                                product.sector()
                                        .forEach(
                                                sector -> {
                                                    productsBucket
                                                            .getOrAddRecord(sector)
                                                            .addElement(product.code())
                                                            .addMetadata("displayName", product.displayName());
                                                    final Bucket entityBucket =
                                                            productEntitiesModel.getOrAddBucket(product.displayName())
                                                                    .addMetadata("sector", sector)
                                                                    .addMetadata("code", product.code());
                                                    populateProduct(sector, product, entityBucket);
                                                }));
        log.info("Completed populating products and ProductEntities models");
    }

    private static void populateProduct(String sector, ProductDetails product, Bucket bucket) {
        METABASE_SERVICE
                .getProductEntityDetails(product.displayName())
                .forEach(
                        details ->
                                details.entity()
                                        .forEach(
                                                entity ->
                                                        bucket.getOrAddRecord(entity.displayName())
                                                                .addMetadata("sector", sector)
                                                                .addMetadata("code", product.code())));
    }

    //lazy getter for the products model
    private static Model productsModel(List<String> products) {
        log.info("Products to be generated: {}", products == null ? "ALL" : products );
        if(productsModel.isEmpty()){
            populateBaseModels(products);
        }
        return productsModel;
    }

    //lazy getter for the products model
    private static Model productEntitiesModel() {
        if(productEntitiesModel.isEmpty()){
            throw new GalaxyException("Products model must be populated before accessing Product Entites Model!");
        }
        return productEntitiesModel;
    }

    private static Model getTRLModel(String version) {
        return trlModels.computeIfAbsent(
                version,
                trlVersion -> {
                    log.info("Started populating Products model fro TRL v{}...", version);
                    final Model productEntitiesForTRLModel =
                            productEntitiesModel().copy("Products TRL v" + version);
                    productEntitiesForTRLModel.addMetadata("trlVersion", trlVersion);

                    METABASE_SERVICE.getTRLAttributes(trlVersion).stream()
                            .filter(attr -> productEntitiesForTRLModel.hasBucket(attr.dataModelName()))
                            .sorted(Comparator.comparing(TRLAttributes::serialNumber))
                            .forEach(
                                    attr -> {
                                        productEntitiesForTRLModel
                                                .getBucket(attr.dataModelName())
                                                .getOrAddRecord(attr.dataEntityName())
                                                .addElement(attr.displayName())
                                                .addMetadata("retired", attr.isRetired())
                                                .addMetadata("attribute", attr);
                                    });

                    log.info("Compacting product entities model for  TRL v{} by eliminating retired entities", version);
                    for (var bucket : productEntitiesForTRLModel.getBuckets()){
                        for (var rec : bucket.getRecords()) {
                            final boolean retiredRecord = rec.getElements().stream()
                                    .allMatch(
                                            element -> (boolean)element.getMetadata("retired"));
                            //if all record elements are retired, remove all elements from it
                            if (retiredRecord) {
                                log.info("Entity '{}' for product '{}' is retired - Cleaning Up...", rec.getName(), bucket.getName());
                                rec.trim(element -> true);
                            }
                        }
                    }
                    log.info("Completed populating ProductEntities model for TRL v{}...", version);
                    return productEntitiesForTRLModel.trim();
                });

    }

    public static void main(String[] args) {
        final TRLGenerator trlGenerator = new TRLGenerator();

        final var productDetailsTemplate = Template.fromResources("templates/ProductDetails.java.vm");
        final Supplier<Stream<Consumer<GeneratorJobBuilder>>> productDetailsJobs =
                () ->
                        Stream.of(trlGenerator.trlVersions)
                                        .map(
                                                trlVersion -> {
                                                    return (Consumer<GeneratorJobBuilder>)
                                                            trlJob ->
                                                                trlJob
                                                                    .template(productDetailsTemplate)
                                                                    .model( () -> getTRLModel(trlVersion))
                                                                    .context(BASE_PACKAGE_CONTEXT)
                                                                    .scope(GeneratorScope.BUCKET)
                                                                    .category(OutputCategory.SOURCES)
                                                                    .format(OutputFormat.JAVA);

                                                });

        final var productEntityDetailsTemplate = Template.fromResources("templates/ProductEntityDetails.java.vm");
        final Supplier<Stream<Consumer<GeneratorJobBuilder>>> productEntityDetailsJobs =
                () ->
                        Stream.of(trlGenerator.trlVersions)
                                .map(
                                        trlVersion -> {
                                            return (Consumer<GeneratorJobBuilder>)
                                                    trlJob ->
                                                            trlJob
                                                                    .template(productEntityDetailsTemplate)
                                                                    .model( () -> getTRLModel(trlVersion))
                                                                    .context(BASE_PACKAGE_CONTEXT)
                                                                    .scope(GeneratorScope.RECORD)
                                                                    .category(OutputCategory.SOURCES)
                                                                    .format(OutputFormat.JAVA);

                                        });


        trlGenerator
                .addPreRequisite(() -> populateBaseModels(trlGenerator.products))
                .addJobs(
                        products ->
                                products.template(Template.fromResources("templates/Sector.java.vm"))
                                        .model(() -> productsModel)
                                        .context(BASE_PACKAGE_CONTEXT)
                                        .scope(GeneratorScope.MODEL)
                                        .category(OutputCategory.SOURCES)
                                        .format(OutputFormat.JAVA),
                        trlVersions ->
                                trlVersions
                                        .template(Template.fromResources("templates/TRLVersion.java.vm"))
                                        .model( () -> Model.create("trlVersions")
                                                .addMetadata("trlVersions", trlGenerator.trlVersions))
                                        .context(BASE_PACKAGE_CONTEXT)
                                        .scope(GeneratorScope.MODEL)
                                        .category(OutputCategory.SOURCES)
                                        .format(OutputFormat.JAVA)


                )
                .addJobs(productDetailsJobs)
                .addJobs(productEntityDetailsJobs)
                .generate(args);
    }
}
