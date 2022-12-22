package com.kam.galaxy.codegen.generator;

import com.kam.galaxy.codegen.model.Model;
import com.kam.galaxy.codegen.output.OutputFormatConfig;
import com.kam.galaxy.codegen.template.TemplateEngine;
import com.kam.galaxy.common.exception.GalaxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Kamran Y. Khan
 * @since 18-Dec-2022
 */

@Command
public abstract class GeneratorEngine implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(GeneratorEngine.class);

    static class LoacationPerCategoryGroup{

        @Option(
                names = {"--base-dir"},
                paramLabel = "BASE_DIR",
                description = "Generated non code base directory path")
        String baseDir;

        @Option(
                names = {"--source-base-dir"},
                paramLabel = "SRC_BASE_DIR",
                description = "Generated sources base directory path")
        String sourcesBaseDir;

        @Option(
                names = {"--test-base-dir"},
                paramLabel = "TEST_BASE_DIR",
                description = "Generated tests base directory path")
        String testsBaseDir;
    }

    @CommandLine.ArgGroup(multiplicity = "1", exclusive = false)
    LoacationPerCategoryGroup loacationPerCategoryGroup;


    // to allow lazy evaluation of job configuration (i.e to be able to pick custom command line arguments
    // we store job configurations is very lazt structure until job execution is started)
    private final Stream.Builder<Supplier<Stream<Consumer<GeneratorJobBuilder>>>> jobConfigs = Stream.builder();

    /**
     *  Adds generator jobs to the generation engine (supplied as Consumer)
     * @param jobs
     * @return
     */
    public GeneratorEngine addJobs(Consumer<GeneratorJobBuilder>... jobs){
        return addJobs(Stream.of(jobs));
    }

    /**
     *  Adds generator jobs to the generation engine (supplied as a Stream)
     * @param jobs
     * @return
     */
    private GeneratorEngine addJobs(Stream<Consumer<GeneratorJobBuilder>> jobs) {
        return addJobs(() -> jobs);
    }

    /**
     *  Adds generator jobs to the generation engine (supplied as a supplier for stream)
     * @param jobs
     * @return
     */
    private GeneratorEngine addJobs(Supplier<Stream<Consumer<GeneratorJobBuilder>>> jobs) {
        jobConfigs.add(jobs);
        return this;
    }

    private void doGenerate(GeneratorJob job, Model model, Consumer<Map<String, Object>>... contextUpdaters){
        log.info("Executing {}", job);
        //prepare context
        final Map<String, Object> templateContext =
                new HashMap<>(
                        Map.of(
                            "generatedBy", this.getClass().getName(),
                            "geneatedAt", DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS)),
                            "model", model)
                );

        //apply external context updaters
        Stream.of(contextUpdaters)
                .forEach(contextUpdater -> contextUpdater.accept(templateContext));

        // generate content using template and context
        final String generatedContent =
                TemplateEngine.INSTANCE.generate(job.template(), templateContext);

        //obtain base directory for current job based on category
        final var jobBaseDir  =
                switch (job.category()) {
                    case GENERIC -> loacationPerCategoryGroup.baseDir;
                    case SOURCES -> loacationPerCategoryGroup.sourcesBaseDir;
                    case TESTS -> loacationPerCategoryGroup.testsBaseDir;
                };

        //obtain output format config
        OutputFormatConfig outputFormatConfig = null;
        if(job.outputConfig() != null){
            outputFormatConfig = new OutputFormatConfig();
            job.outputConfig().accept(outputFormatConfig);
        }

        //write generated content to file
        job.format().serialize(outputFormatConfig, jobBaseDir, generatedContent, templateContext);
    }

    @Override
    public void run() {
        if(jobConfigs != null){
            jobConfigs.build()
                    .flatMap(stream -> stream.get())
                    .map(
                            job -> {
                                final var jobConfigBuilder = GeneratorJobBuilder.builder();
                                job.accept(jobConfigBuilder);
                                return jobConfigBuilder.build();
                            }
                    )
                    .forEach(
                            job -> {
                                // execute model supplier just one for each job to obtain the model
                                final Model jobModel = job.model().get();

                                switch (job.scope()) {
                                    case MODEL -> doGenerate(job, jobModel, job.context());
                                    case BUCKET -> jobModel.getBuckets().stream()
                                            .filter(bucket -> !bucket.isEmpty())
                                            .forEach(
                                                    bucket ->
                                                            doGenerate(job,
                                                                        jobModel,
                                                                        context -> context.put("bucket", bucket),
                                                                        job.context()));
                                    case RECORD -> jobModel.getBuckets().stream()
                                            .filter(bucket -> !bucket.isEmpty())
                                            .forEach(
                                                    bucket ->
                                                            bucket.getRecords().stream()
                                                                            .filter(record -> !record.isEmpty())
                                                                                    .forEach(
                                                                                            record ->
                                                                                                    doGenerate(job,
                                                                                                                jobModel,
                                                                                                                context -> {
                                                                                                                    context.put("bucket", bucket);
                                                                                                                    context.put("record", record);
                                                                                                                },
                                                                                                                job.context())
                                                                                    )
                                                            );
                                    default -> throw new GalaxyException("Unsupported generator scope '%s'!".formatted((job.scope())));
                                }
                            }
                    );
        }
    }

    public int generate(String... args){
        return new CommandLine(this).execute(args);
    }

}
