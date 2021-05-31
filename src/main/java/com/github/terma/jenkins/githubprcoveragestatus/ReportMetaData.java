package com.github.terma.jenkins.githubprcoveragestatus;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportMetaData {
    private String key;
    private String includes;
    private String excludes;
    private static final Logger LOGGER = Logger.getLogger(ReportMetaData.class.getName());

    public ReportMetaData(String key) {
        this.key = key;
        this.includes = null;
        this.excludes = null;
    }

    public ReportMetaData(String key, String includes, String excludes) {
        this.key = key;
        if(includes!=null) this.includes = "\\b(" + includes + ")\\b";
        if(excludes!=null) this.excludes = "\\b(" + excludes + ")\\b";

        LOGGER.log(Level.INFO, "includes: " + includes);
        LOGGER.log(Level.INFO, "excludes: " + excludes);
    }

    public boolean validate(String path) {
//        if(includes==null && excludes==null) return false;
        if(includes==null && excludes==null) return true;

        if(excludes==null) return Pattern.compile(includes).matcher(path).find();
        if(includes==null) return !Pattern.compile(excludes).matcher(path).find();

        LOGGER.log(Level.INFO, "path: " + path);
        LOGGER.log(Level.INFO, "isIncluded: " + Pattern.compile(includes).matcher(path).find());
        LOGGER.log(Level.INFO, "isExcluded: " + !Pattern.compile(excludes).matcher(path).find());

        return Pattern.compile(includes).matcher(path).find() && !Pattern.compile(excludes).matcher(path).find();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIncludes() {
        return includes;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    @Override
    public String toString() {
        return "ReportMetaData{" +
                "key='" + key + '\'' +
                ", includes='" + includes + '\'' +
                ", excludes='" + excludes + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportMetaData that = (ReportMetaData) o;

        if (!key.equals(that.key)) return false;
        if (includes != null ? !includes.equals(that.includes) : that.includes != null) return false;
        return excludes != null ? excludes.equals(that.excludes) : that.excludes == null;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (includes != null ? includes.hashCode() : 0);
        result = 31 * result + (excludes != null ? excludes.hashCode() : 0);
        return result;
    }
}
