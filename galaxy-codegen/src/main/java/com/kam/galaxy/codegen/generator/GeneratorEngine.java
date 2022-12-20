package com.kam.galaxy.codegen.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author Kamran Y. Khan
 * @since 18-Dec-2022
 */

@Command
public class GeneratorEngine implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(GeneratorEngine.class);

    @Override
    public void run() {
        System.out.println("Ran Successfully");
    }

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
        String srcBaseDir;

        @Option(
                names = {"--test-base-dir"},
                paramLabel = "TEST_BASE_DIR",
                description = "Generated tests base directory path")
        String testBaseDir;
    }

    @CommandLine.ArgGroup(multiplicity = "1", exclusive = false)
    LoacationPerCategoryGroup loacationPerCategoryGroup;

    public int generate(String... args){
        return new CommandLine(this).execute(args);
    }

    public static void main(String[] args){
        GeneratorEngine generatorEngine = new GeneratorEngine();
        generatorEngine.generate("--base-dir", "xyz");
    }
}
