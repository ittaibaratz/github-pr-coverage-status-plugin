package com.github.terma.jenkins.githubprcoveragestatus;

import java.util.List;

public class CoverageMetaData {
    private String gitUrl;
    private String gitBranch;
    private List<ReportMetaData> reportMetaDataList;

    public CoverageMetaData(List<ReportMetaData> reportMetaDataList) {
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
}
