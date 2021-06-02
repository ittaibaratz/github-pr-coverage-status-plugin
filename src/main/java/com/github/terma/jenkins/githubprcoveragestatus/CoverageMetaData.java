package com.github.terma.jenkins.githubprcoveragestatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CoverageMetaData implements Serializable {
    private String gitUrl;
    private String gitBranch;
    private List<ReportMetaData> reportMetaDataList;

    public CoverageMetaData(String gitUrl, String gitBranch, List<ReportMetaData> reportMetaDataList) {
        this.gitUrl = gitUrl;
        this.gitBranch = gitBranch;
        this.reportMetaDataList = reportMetaDataList!=null ? reportMetaDataList : new ArrayList<>();
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

    public List<ReportMetaData> getReportMetaDataList() {
        return reportMetaDataList;
    }

    public void setReportMetaDataList(List<ReportMetaData> reportMetaDataList) {
        this.reportMetaDataList = reportMetaDataList;
    }

    @Override
    public String toString() {
        return "CoverageMetaData{" +
                "gitUrl='" + gitUrl + '\'' +
                ", gitBranch='" + gitBranch + '\'' +
                ", reportMetaDataList=" + reportMetaDataList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoverageMetaData that = (CoverageMetaData) o;

        if (!gitUrl.equals(that.gitUrl)) return false;
        if (!gitBranch.equals(that.gitBranch)) return false;
        return reportMetaDataList != null ? reportMetaDataList.equals(that.reportMetaDataList) : that.reportMetaDataList == null;
    }

    @Override
    public int hashCode() {
        int result = gitUrl.hashCode();
        result = 31 * result + gitBranch.hashCode();
        result = 31 * result + (reportMetaDataList != null ? reportMetaDataList.hashCode() : 0);
        return result;
    }
}
