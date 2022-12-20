package com.kam.galaxy.codegen.output;

import com.google.common.io.Resources;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Kamran Y. Khan
 * @since 20-Dec-2022
 */
public class OutputFormatTest {

    @TempDir
    private Path tempPath;

    @Test
    public void javaOutputFormatTest() throws Exception {
        String content = Resources.toString(Resources.getResource("content/JavaSampleContent.txt"), StandardCharsets.UTF_8);
        OutputFormat.JAVA.serialize(null, tempPath.toString(), content, null);

        File tempDir = new File(tempPath.toString());
        Assertions.assertThat(tempDir).isDirectoryContaining("glob:**com")
                .isDirectoryRecursivelyContaining("glob:**test")
                .isDirectoryRecursivelyContaining("glob:**TestClass.java");

        String contentFromFile = Files.readString(Path.of(tempDir.getPath() + File.separator + "com"
                                                                                + File.separator + "test"
                                                                                + File.separator +"TestClass.java"));
        Assertions.assertThat(contentFromFile).isEqualTo(content);
    }

    @Test
    public void textOutputFormatTest() throws Exception {
        String content = "Test Content";

        OutputFormatConfig outputFormatConfig = new OutputFormatConfig();
        outputFormatConfig.fileDir("test");
        outputFormatConfig.fileName("TestFile");
        outputFormatConfig.fileExt("txt");

        OutputFormat.TEXT.serialize(outputFormatConfig, tempPath.toString(), content, null);

        File tempDir = new File(tempPath.toString());
        Assertions.assertThat(tempDir).isDirectoryContaining("glob:**test")
                .isDirectoryRecursivelyContaining("glob:**TestFile.txt");

        String contentFromFile = Files.readString(Path.of(tempDir.getPath() + File.separator + "test"
                                                                                + File.separator +"TestFile.txt"));
        Assertions.assertThat(contentFromFile).isEqualTo(content);
    }

}
