package com.github.terma.jenkins.githubprcoveragestatus;

import java.util.List;
import java.util.Objects;

public class CoverageMetaData {
    private String gitUrl;
    private String gitBranch;
    private List<ReportMetaData> reportMetaDataList;

    public CoverageMetaData(String gitUrl, String gitBranch, List<ReportMetaData> reportMetaDataList) {
        this.gitUrl = gitUrl;
        this.gitBranch = gitBranch;
        this.reportMetaDataList = reportMetaDataList;
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
        return gitUrl.equals(that.gitUrl) && gitBranch.equals(that.gitBranch) && Objects.equals(reportMetaDataList, that.reportMetaDataList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gitUrl, gitBranch, reportMetaDataList);
    }
}
