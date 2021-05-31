package com.sloppie.mediawellbeing.models;

public class Photo {
    public static final int SUCCESSFUL_SCAN = 1;
    public static final int FAILED_SCAN = 0;
    public static final int PENDING_SCAN = -1;

    private int isExplicit = -1;
    private String fileName;
    private String id;
    private String absolutePath;
    private String status = "NOT_REVIEWED";

    public int getScanState() {
        return scanState;
    }

    public void setScanState(int scanState) {
        this.scanState = scanState;
    }

    private int scanState;

    public Photo(String absolutePath) {
        this.absolutePath = absolutePath;
        String[] folders = absolutePath.split("/");
        fileName = folders[folders.length - 1];
        id = absolutePath.replace('/', '_');
        scanState = PENDING_SCAN;
    }

    public int getIsExplicit() {
        return isExplicit;
    }

    public void setIsExplicit(int isExplicit) {
        this.isExplicit = isExplicit;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
