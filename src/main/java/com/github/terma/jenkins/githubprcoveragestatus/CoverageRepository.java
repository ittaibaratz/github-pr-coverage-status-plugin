package com.github.terma.jenkins.githubprcoveragestatus;

import hudson.FilePath;

import java.io.IOException;
import java.util.Map;

interface CoverageRepository {

    Map<String, ReportData> get(FilePath workspace) throws IOException, InterruptedException;

}
