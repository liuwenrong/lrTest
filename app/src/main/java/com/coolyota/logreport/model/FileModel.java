package com.coolyota.logreport.model;

import java.io.File;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/30
 */
public class FileModel {
    private String fileName;
    private String fileTime;
    private long fileLongTime;
    private String fileSize;
    private boolean isFold;// 是否是文件夹
    private File file;
    private boolean isSelected = false;// 是否被勾选 默认不勾选

    public FileModel(){

    }

    public FileModel(String fileName, String fileTime, String fileSize, boolean isFold) {
        this.fileName = fileName;
        this.fileTime = fileTime;
        this.fileSize = fileSize;
        this.isFold = isFold;
    }

    public long getFileLongTime() {
        return fileLongTime;
    }

    public void setFileLongTime(long fileLongTime) {
        this.fileLongTime = fileLongTime;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileTime() {
        return fileTime;
    }

    public void setFileTime(String fileTime) {
        this.fileTime = fileTime;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isFold() {
        return isFold;
    }

    public void setFold(boolean fold) {
        isFold = fold;
    }
}
