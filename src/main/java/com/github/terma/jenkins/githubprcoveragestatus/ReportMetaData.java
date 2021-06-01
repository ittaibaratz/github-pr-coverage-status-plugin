package com.github.terma.jenkins.githubprcoveragestatus;

import java.io.Serializable;
import java.util.regex.Pattern;

public class ReportMetaData implements Serializable {
    private String label;
    private String includes;
    private String excludes;

    public ReportMetaData() {
    }

    public ReportMetaData(String label) {
        this.label = label;
        this.includes = null;
        this.excludes = null;
    }

    public ReportMetaData(String label, String includes, String excludes) {
        this.label = label;
        if(includes!=null) this.includes = "\\b(" + includes + ")\\b";
        if(excludes!=null) this.excludes = "\\b(" + excludes + ")\\b";
    }

    public boolean validate(String path) {
//        if(includes==null && excludes==null) return false;
        if(includes==null && excludes==null) return true;

        if(excludes==null) return Pattern.compile(includes).matcher(path).find();
        if(includes==null) return !Pattern.compile(excludes).matcher(path).find();

        return Pattern.compile(includes).matcher(path).find() && !Pattern.compile(excludes).matcher(path).find();
    }

    public String getLabel() {
        return label;
    }

    public String getIncludes() {
        return includes;
    }

    public String getExcludes() {
        return excludes;
    }


    public void setLabel(String label) {
        this.label = label;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    @Override
    public String toString() {
        return "ReportMetaData{" +
                "label='" + label + '\'' +
                ", includes='" + includes + '\'' +
                ", excludes='" + excludes + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportMetaData that = (ReportMetaData) o;

        if (!label.equals(that.label)) return false;
        if (includes != null ? !includes.equals(that.includes) : that.includes != null) return false;
        return excludes != null ? excludes.equals(that.excludes) : that.excludes == null;
    }

    @Override
    public int hashCode() {
        int result = label.hashCode();
        result = 31 * result + (includes != null ? includes.hashCode() : 0);
        result = 31 * result + (excludes != null ? excludes.hashCode() : 0);
        return result;
    }
}
