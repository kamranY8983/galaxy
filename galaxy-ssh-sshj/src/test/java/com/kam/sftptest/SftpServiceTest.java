package com.kam.sftptest;

import com.github.stefanbirkner.fakesftpserver.lambda.FakeSftpServer;
import com.kam.galaxy.service.SftpService;
import static org.assertj.core.api.Assertions.assertThat;

import net.schmizz.sshj.sftp.RemoteResourceInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * @author Kamran Y. Khan
 * @since 31-Dec-2022
 */
public class SftpServiceTest {

    @TempDir
    Path path;

    @ParameterizedTest
    @ValueSource(strings = {"/home/test", "/test"})
    public void creteDirectoryTest(String path) throws Exception {
        runOnFakeServer(
                sftpService -> {
                        sftpService.createDirectory(path);
                        assertThat(sftpService.isDirectoryExist(path)).isTrue();
                        sftpService.closeSession();
                    });
    }

    @Test
    public void uploadFileTest() throws Exception {
        Path tempFilePath = Path.of(path.toString() + File.separator + "TempFile.txt");
        Files.createFile(tempFilePath);

        runOnFakeServer(
                sftpService -> {
                        sftpService.createDirectory("/home/test");
                        sftpService.uploadFile(tempFilePath.toString(), "/home/test");
                        assertThat(sftpService.isDirectoryExist("/home/test")).isTrue();
                        assertThat(sftpService.isFileExist("/home/test/" + tempFilePath.getFileName())).isTrue();
                        sftpService.closeSession();
                    });
    }

    @Test
    public void downloadFileTest() throws Exception {
        Path tempFilePath = Path.of(path.toString() + File.separator + "TempFile.txt");
        Files.createFile(tempFilePath);

        runOnFakeServer(
                sftpService -> {
                    sftpService.downloadFile("/home/test/TestFile.txt", path.toString());
                    assertThat(Files.exists(Path.of(path.toString() + File.separator + "TestFile.txt"))).isTrue();
                    sftpService.closeSession();
                },
                fakeSftpServer -> {
                    try{
                        fakeSftpServer.putFile("/home/test/TestFile.txt", "Test Content".getBytes(StandardCharsets.UTF_8));
                    }
                    catch (IOException ex){
                        throw new RuntimeException("Can't put a file on server.", ex);
                    }
                });
    }

    @Test
    public void getListOfFilesInRemotePathTest() throws Exception {
        Path tempFilePath = Path.of(path.toString() + File.separator + "TempFile.txt");
        Files.createFile(tempFilePath);

        runOnFakeServer(
                sftpService -> {

                    List<String> fileNames = sftpService.getListOfFilesInRemotePath("/home/test").stream()
                            .map(RemoteResourceInfo::getName)
                            .collect(Collectors.toList());
                    assertThat(fileNames.size()).isEqualTo(4);
                    assertThat(fileNames).contains("TestFile.txt","TestFile.zip","TestFile.json","TestFile.xml");
                    sftpService.closeSession();
                },
                fakeSftpServer -> {
                    try{
                        byte[] testContent = "Test Content".getBytes(StandardCharsets.UTF_8);
                        fakeSftpServer.putFile("/home/test/TestFile.txt", testContent);
                        fakeSftpServer.putFile("/home/test/TestFile.zip", testContent);
                        fakeSftpServer.putFile("/home/test/TestFile.json", testContent);
                        fakeSftpServer.putFile("/home/test/TestFile.xml", testContent);
                    }
                    catch (IOException ex){
                        throw new RuntimeException("Can't put a file on server.", ex);
                    }
                });
    }

    public void runOnFakeServer(Consumer<SftpService> sftpServiceConsumer, Consumer<FakeSftpServer>... preSetup) throws Exception{
            FakeSftpServer.withSftpServer(
                    fakeSftpServer -> {
                        if(preSetup != null){
                            Arrays.stream(preSetup).forEach(
                                    fakeSftpServerConsumer ->
                                            fakeSftpServerConsumer.accept(fakeSftpServer)
                            );
                        }
                        fakeSftpServer.addUser("ADMIN", "password");
                        fakeSftpServer.setPort(2244);
                        SftpService sftpService = new SftpService("localhost", 2244, "ADMIN", "password");
                        sftpServiceConsumer.accept(sftpService);
                    });
    }
}
