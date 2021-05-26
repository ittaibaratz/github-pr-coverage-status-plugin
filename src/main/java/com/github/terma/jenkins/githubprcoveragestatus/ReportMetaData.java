package com.github.terma.jenkins.githubprcoveragestatus;

import java.util.List;

public class ReportMetaData {
    private String key;
    private List<String> includePaths;
    private List<String> excludePaths;

    public ReportMetaData(String key, List<String> includePaths, List<String> excludePaths) {
        this.key = key;
        this.includePaths = includePaths;
        this.excludePaths = excludePaths;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getIncludePaths() {
        return includePaths;
    }

    public void setIncludePaths(List<String> includePaths) {
        this.includePaths = includePaths;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }

    @Override
    public String toString() {
        return "ReportMetaData{" +
                "key='" + key + '\'' +
                ", includePaths=" + includePaths +
                ", excludePaths=" + excludePaths +
                '}';
    }
}
