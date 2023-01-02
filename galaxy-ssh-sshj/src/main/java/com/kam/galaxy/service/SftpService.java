package com.kam.galaxy.service;

import com.kam.galaxy.common.exception.GalaxyException;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.*;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Kamran Y. Khan
 * @since 31-Dec-2022
 */
public class SftpService {

    private  static final Logger log = LoggerFactory.getLogger(SftpService.class);

    private  static final String HOSTNAME = "http://localhost/";
    private  static final int PORT = 2244;
    private  static final String USERNAME = "ADMIN";
    private  static final String PASSWORD = "password";

    private final SSHClient sshClient;

    public SftpService(){
        this(HOSTNAME, PORT, USERNAME, PASSWORD);
    }

    public SftpService(String hostname, int port, String userName, String password){
        SSHClient sshClient = new SSHClient();
        try{
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(hostname, port);
            sshClient.authPassword(userName, password);
        } catch (IOException ex){
            throw new GalaxyException("Can't connect to host", ex);
        }
        this.sshClient = sshClient;
    }

    public SftpService(SSHClient sshClient){
        this.sshClient = sshClient;
    }


    public void printTree(String path) throws Exception {
        try(SFTPClient sftpClient = getSFTPClient()){
            printTree(path,sftpClient);
        } catch (IOException ex){
            throw new GalaxyException("Cannot print tree for path '%s'".formatted(path), ex);
        }
    }
    private void printTree(String path, SFTPClient sftpClient) throws IOException {

        for (RemoteResourceInfo remoteResourceInfo : sftpClient.ls(path)) {
            FileAttributes remoteResourceInfoAttributes = remoteResourceInfo.getAttributes();
            FileMode remoteResourceInfoAttributesMode = remoteResourceInfoAttributes.getMode();

            System.out.println();
            System.out.printf("[printTree][%d]\n", System.currentTimeMillis());
            System.out.printf("[printTree][%d] Get name : %s \n",
                    System.currentTimeMillis(), remoteResourceInfo.getName());
            System.out.printf("[printTree][%d] Get path : %s \n",
                    System.currentTimeMillis(), remoteResourceInfo.getPath());
            System.out.printf("[printTree][%d] Get parent : %s \n",
                    System.currentTimeMillis(), remoteResourceInfo.getParent());
            System.out.printf("[printTree][%d] Get size : %d \n",
                    System.currentTimeMillis(), remoteResourceInfoAttributes.getSize());
            System.out.printf("[printTree][%d] Get user : %d \n",
                    System.currentTimeMillis(), remoteResourceInfoAttributes.getUID());
            System.out.printf("[printTree][%d] Get group : %d \n",
                    System.currentTimeMillis(), remoteResourceInfoAttributes.getGID());
            System.out.printf("[printTree][%d] Get last accessed time : %d \n",
                    System.currentTimeMillis(), (remoteResourceInfoAttributes.getAtime() * 1000));
            System.out.printf("[printTree][%d] Get last modified time : %d \n",
                    System.currentTimeMillis(), (remoteResourceInfoAttributes.getMtime() * 1000));
            System.out.printf("[printTree][%d] Get type : %s \n",
                    System.currentTimeMillis(), remoteResourceInfoAttributesMode.getType());
            System.out.printf("[printTree][%d] Is file : %s \n",
                    System.currentTimeMillis(), remoteResourceInfo.isRegularFile());
            System.out.printf("[printTree][%d] Is directory : %s \n",
                    System.currentTimeMillis(), remoteResourceInfo.isDirectory());
            System.out.printf("[printTree][%d] File to string : %s \n",
                    System.currentTimeMillis(), remoteResourceInfo);
            System.out.printf("[printTree][%d] File attributes to string : %s \n",
                    System.currentTimeMillis(), remoteResourceInfoAttributes);
            System.out.printf("[printTree][%d] File attributes mode to string : %s \n",
                    System.currentTimeMillis(), remoteResourceInfoAttributesMode);
            System.out.println();

            if (remoteResourceInfo.isDirectory()) {
                printTree(remoteResourceInfo.getPath(), sftpClient);
            }
        }
    }

    /**
     * Creates directories on the host for specified path if not already exist
     * @param path - path of the remote directory to be created
     */
    public void createDirectory(String path) {
        try(SFTPClient sftpClient = getSFTPClient()){
            if(!isDirectoryExist(path)){
                sftpClient.mkdirs(path);
                log.info("Directories '{}' created Successfully!!!", path);
            }
            else {
                log.info("Directory already exist!!!");
            }
        } catch (IOException ex){
            throw new GalaxyException("Cannot create directory at path '%s'".formatted(path), ex);
        }
    }

    /**
     * Uploads regular file to host at specified path only if the specified path exists.
     * @param localPath - absolute Path of the local regular file to be uploaded
     * @param remotePath - path on the host where file has to be uploaded
     */
    public void uploadFile(String localPath, String remotePath) {
        if(!Files.isRegularFile(Path.of(localPath))){
            throw new GalaxyException("File specified at local path '%s' either does not exist or is not a regular file".formatted(localPath));
        }
        if(!isDirectoryExist(remotePath)){
            throw new GalaxyException("Specified Remote directory '%s' does not exist at host '%s'".formatted(remotePath, sshClient.getRemoteHostname()));
        }
        try(SFTPClient sftpClient = getSFTPClient()){
            sftpClient.put(localPath, remotePath);
            log.info("File '{}' uploaded Successfully at path '{}'", localPath, remotePath);
        } catch (IOException ex){
            throw new GalaxyException("Cannot upload file '%s' at path '%s'".formatted(localPath, remotePath), ex);
        }
    }

    /**
     * Uploads regular file to host at specified path. If the path at remote host does not exist it creates one.
     * @param localPath - absolute Path of the local regular file to be uploaded
     * @param remotePath - path on the host where file has to be uploaded
     */
    public void uploadFileCreatingDirectories(String localPath, String remotePath) {
        if(isDirectoryExist(remotePath)){
            log.info("Creating directories on host for Path '{}'", remotePath);
            this.createDirectory(remotePath);
        }
        uploadFile(localPath, remotePath);
    }

    /**
     * Downloads file from the host to specified local path
     * @param remotePath - path of file to be downloaded
     * @param localPath - path where the file has to be downloaded
     */
    public void downloadFile(String remotePath, String localPath) {
        if(!isFileExist(remotePath)){
            throw new GalaxyException("Specified Remote file '%s' does not exist at host '%s'".formatted(remotePath, sshClient.getRemoteHostname()));
        }
        try(SFTPClient sftpClient = getSFTPClient()){
            Path lp = Path.of(localPath);
            if(!Files.isDirectory(lp)){
                log.info("Specified local path '{}' does not exist. Creating!", localPath);
                Files.createDirectories(lp);
            }
            sftpClient.get(remotePath,localPath);
            log.info("File '{}' downloaded successfully at '{}'", remotePath, localPath);
        } catch (IOException ex){
            throw new GalaxyException("Unable to download file '%s'".formatted(remotePath), ex);
        }

    }

    /**
     * Gets the list of files at specified path
     * @param remotePath - path of the directory
     * @return - list of files in remote directory. Null if the specified path does not exist
     */
    public List<RemoteResourceInfo> getListOfFilesInRemotePath(String remotePath) {
        try(SFTPClient sftpClient = getSFTPClient()){
            if(this.isDirectoryExist(remotePath)){
                 return sftpClient.ls(remotePath);
            }
            else {
                log.info("Specified Remote Path '{}' does not exist at host '{}'", remotePath, sshClient.getRemoteHostname());
                return null;
            }
        } catch (IOException ex){
            throw new GalaxyException("Cannot get list of files from remote path '%s'".formatted(remotePath), ex);
        }
    }

    private FileAttributes getFileAttr(String remotePath) {
        try(SFTPClient sftpClient = getSFTPClient()){
            return sftpClient.stat(remotePath);
        } catch (IOException ex){
            log.debug("Cannot get File attributes for remote path '{}' : {}", remotePath, ex.getLocalizedMessage());
            //Need this to avoid 'File does not exist' exception at runtime
            return null;
        }
    }

    /**
     * Checks if the specified path exists on host and is a directory
     * @param remotePath - path of the directory
     * @return true if path exists, false if path does not exist or is a regular file (not a directory)
     */
    public boolean isDirectoryExist(String remotePath) {
        FileAttributes fileAttributes = getFileAttr(remotePath);
        if(fileAttributes != null){
            return fileAttributes.getType().equals(FileMode.Type.DIRECTORY);
        }
        return false;
    }

    /**
     * Checks if the specified path represents regular file on host
     * @param remotePath - path of file
     * @return true if file exists, false if file does not exist or is a not regular file
     */
    public boolean isFileExist(String remotePath) {
        FileAttributes fileAttributes = getFileAttr(remotePath);
        if(fileAttributes != null){
            return fileAttributes.getType().equals(FileMode.Type.REGULAR);
        }
        return false;
    }

    private SFTPClient getSFTPClient(){
        try{
            return sshClient.newSFTPClient();
        } catch (IOException ex){
            throw new GalaxyException("Cannot create SFTP client to host '%s'".formatted(sshClient.getRemoteHostname()), ex);
        }
    }

    public void closeSession(){
        if(sshClient != null && sshClient.isConnected()){
            try{
                sshClient.close();
            } catch (IOException ex){
                throw new GalaxyException("Session cannot be closed");
            }
        }
        else {
            log.info("Disconnected attempted to close");
        }
    }

}
