package com.kam.galaxy.codegen.output;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Kamran Y. Khan
 * @since 20-Dec-2022
 */
public final class OutputFormatConfig {

    private String fileDir = "";
    private String fileName;
    private String fileExt;

    public String fileDir(){
        return fileDir;
    }

    public OutputFormatConfig fileDir(String fileDir){
        this.fileDir = checkNotNull(fileDir, "fileDir value can't be null");
        return this;
    }
    public String fileName(){
        return fileName;
    }

    public OutputFormatConfig fileName(String fileName){
        this.fileName = checkNotNull(fileName, "fileName value can't be null");
        return this;
    }
    public String fileExt(){
        return fileExt;
    }

    public OutputFormatConfig fileExt(String fileExt){
        this.fileExt = checkNotNull(fileExt, "fileDir value can't be null");
        return this;
    }

    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this.getClass())
                .add("fileDir", fileDir)
                .add("fileName", fileName)
                .add("fileExt", fileExt)
                .toString();
    }


}
