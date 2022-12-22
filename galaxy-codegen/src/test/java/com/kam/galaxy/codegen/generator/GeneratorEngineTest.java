package com.kam.galaxy.codegen.generator;

import com.kam.galaxy.codegen.model.Model;
import com.kam.galaxy.codegen.output.OutputCategory;
import com.kam.galaxy.codegen.output.OutputFormat;
import com.kam.galaxy.codegen.template.Template;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Kamran Y. Khan
 * @since 19-Dec-2022
 */
public class GeneratorEngineTest {

    @TempDir
    static Path path;

    static Stream<Arguments> positiveArgumentsProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"--base-dir", "C:/"}),
                Arguments.of((Object) new String[]{"--source-base-dir", "C:/source"}),
                Arguments.of((Object) new String[]{"--test-base-dir", "C:/tests"}),
                Arguments.of((Object) new String[]{"--base-dir", "C:/", "--source-base-dir", "C:/source", "--test-base-dir", "C:/tests"})
        );
    }

    static Stream<Arguments> negativeArgumentsProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"--base-dir"}),
                Arguments.of((Object) new String[]{"--source-base-dir"}),
                Arguments.of((Object) new String[]{"--test-base-dir"})
        );
    }

    @ParameterizedTest
    @MethodSource("positiveArgumentsProvider")
    public void commandlineArgumentPositiveTest(String[] args) {
        GeneratorEngine generatorEngine = new GeneratorEngine(){};
        int statusCode = generatorEngine.generate(args);
        Assertions.assertThat(statusCode).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("negativeArgumentsProvider")
    public void commandlineArgumentNegativeTest(String[] args) {
        GeneratorEngine generatorEngine = new GeneratorEngine(){};
        int statusCode = generatorEngine.generate(args);
        Assertions.assertThat(statusCode).isNotEqualTo(0);
    }

    @Test
    public void javaCodeGenerationForModelTest() throws Exception {
        GeneratorEngine generatorEngine = new GeneratorEngine(){};
        int statusCode = generatorEngine
                .addJobs(
                        jobConfig ->
                                jobConfig.template(Template.fromResources("templates/JavaTemplateForModel.vm"))
                                        .model(() -> Model.create("TestModel"))
                                        .context(cntxt -> {
                                            cntxt.put("variableName","testVariable");
                                            cntxt.put("methodName","testMethod");
                                                        })
                                        .scope(GeneratorScope.MODEL)
                                        .category(OutputCategory.SOURCES)
                                        .format(OutputFormat.JAVA)
                )
                .generate("--source-base-dir", path.toString());

        Assertions.assertThat(statusCode).isEqualTo(0);

        int compilationStatus = ToolProvider.getSystemJavaCompiler().run(null,null, null, path + File.separator +"TestModel.java");
        Assertions.assertThat(compilationStatus).as("Generated class file cannot be compiled").isEqualTo(0);
        Assertions.assertThat(new File(path.toString())).isDirectoryContaining("glob:**TestModel.java");

        Class testClass = new URLClassLoader(new URL[]{path.toUri().toURL()}).loadClass("TestModel");
        Assertions.assertThat(testClass).hasDeclaredFields("testVariable");
        Assertions.assertThat(testClass).hasDeclaredMethods("testMethod");
    }

    @Test
    public void javaCodeGenerationForBucketTest() throws Exception {
        GeneratorEngine generatorEngine = new GeneratorEngine(){};
        int statusCode = generatorEngine
                .addJobs(
                        jobConfig ->
                                jobConfig.template(Template.fromResources("templates/JavaTemplateForBucket.vm"))
                                        .model(() -> {
                                            Model model = Model.create("Test Model");
                                            model.getOrAddBucket("TestBucket1").addRecord("bucket1Field1");
                                            model.getOrAddBucket("TestBucket1").addRecord("bucket1Field2");
                                            model.getOrAddBucket("TestBucket2").addRecord("bucket2Field1");
                                            model.getOrAddBucket("TestBucket2").addRecord("bucket2Field2");
                                            return model;
                                        })
                                        .context(cntx -> Map.of())
                                        .scope(GeneratorScope.BUCKET)
                                        .category(OutputCategory.SOURCES)
                                        .format(OutputFormat.JAVA)
                )
                .generate("--source-base-dir", path.toString());

        Assertions.assertThat(statusCode).isEqualTo(0);
        Assertions.assertThat(new File(path.toString())).isDirectoryContaining("glob:**TestBucket1.java");
        Assertions.assertThat(new File(path.toString())).isDirectoryContaining("glob:**TestBucket1.java");

        ToolProvider.getSystemJavaCompiler().run(null,null, null, path + File.separator +"TestBucket1.java");
        ToolProvider.getSystemJavaCompiler().run(null,null, null, path + File.separator +"TestBucket2.java");

        URLClassLoader classLoader = new URLClassLoader(new URL[]{path.toUri().toURL()});
        Class testClass1 = classLoader.loadClass("TestBucket1");
        Class testClass2 = classLoader.loadClass("TestBucket2");

        Assertions.assertThat(testClass1).hasDeclaredFields("bucket1Field1", "bucket1Field2");
        Assertions.assertThat(testClass2).hasDeclaredFields("bucket2Field1", "bucket2Field2");
    }

    @Test
    public void javaCodeGenerationForRecordTest() throws Exception {
        GeneratorEngine generatorEngine = new GeneratorEngine(){};
        int statusCode =
                generatorEngine
                .addJobs(
                        jobConfig ->
                                jobConfig.template(Template.fromResources("templates/JavaTemplateForRecord.vm"))
                                        .model(() -> {
                                            Model model = Model.create("Test Model");
                                            model.getOrAddBucket("TestBucket").getOrAddRecord("TestRecord1").addElement("record1Field1");
                                            model.getOrAddBucket("TestBucket").getOrAddRecord("TestRecord1").addElement("record1Field2");
                                            model.getOrAddBucket("TestBucket").getOrAddRecord("TestRecord2").addElement("record2Field1");
                                            model.getOrAddBucket("TestBucket").getOrAddRecord("TestRecord2").addElement("record2Field2");
                                            return model;
                                        })
                                        .context(cntx -> Map.of())
                                        .scope(GeneratorScope.RECORD)
                                        .category(OutputCategory.SOURCES)
                                        .format(OutputFormat.JAVA)
                )
                .generate("--source-base-dir", path.toString());

        Assertions.assertThat(statusCode).isEqualTo(0);
        Assertions.assertThat(new File(path.toString())).isDirectoryContaining("glob:**TestRecord1.java");
        Assertions.assertThat(new File(path.toString())).isDirectoryContaining("glob:**TestRecord2.java");

        ToolProvider.getSystemJavaCompiler().run(null,null, null, path + File.separator +"TestRecord1.java");
        ToolProvider.getSystemJavaCompiler().run(null,null, null, path + File.separator +"TestRecord2.java");

        URLClassLoader classLoader = new URLClassLoader(new URL[]{path.toUri().toURL()});
        Class testClass1 = classLoader.loadClass("TestRecord1");
        Class testClass2 = classLoader.loadClass("TestRecord2");

        Assertions.assertThat(testClass1).hasDeclaredFields("record1Field1", "record1Field2");
        Assertions.assertThat(testClass2).hasDeclaredFields("record2Field1", "record2Field2");
    }


}
