package com.github.terma.jenkins.githubprcoveragestatus;

import hudson.FilePath;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

interface CoverageRepository {

    Map<String, ReportData> get(PrintStream buildLog, FilePath workspace) throws IOException, InterruptedException;

}
