package com.github.terma.jenkins.githubprcoveragestatus;

import hudson.FilePath;

import java.io.IOException;
import java.io.PrintStream;

interface CoverageRepository {

    float get(FilePath workspace, PrintStream buildLog) throws IOException, InterruptedException;

}
