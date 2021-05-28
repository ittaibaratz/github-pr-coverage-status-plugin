package com.github.terma.jenkins.githubprcoveragestatus;

import java.util.List;
import java.util.Objects;

public class ReportMetaData {
    private String key;
    private List<String> includes;
    private List<String> excludes;

    public ReportMetaData(String key, List<String> includes, List<String> excludes) {
        this.key = key;
        this.includes = includes;
        this.excludes = excludes;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    @Override
    public String toString() {
        return "ReportMetaData{" +
                "key='" + key + '\'' +
                ", includePaths=" + includes +
                ", excludePaths=" + excludes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportMetaData that = (ReportMetaData) o;
        return key.equals(that.key) && includes.equals(that.includes) && excludes.equals(that.excludes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, includes, excludes);
    }
}
