/*

    Copyright 2015-2016 Artem Stasiuk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package com.github.terma.jenkins.githubprcoveragestatus;

import hudson.FilePath;
import hudson.Util;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
final class GetCoverageCallable extends MasterToSlaveFileCallable<Float> implements CoverageRepository {

    private final boolean disableSimpleCov;
    private String jacocoCounterType = "";
    private PrintStream buildLog;

    GetCoverageCallable(final boolean disableSimpleCov, final String jacocoCounterType) {
        this.disableSimpleCov = disableSimpleCov;
        this.jacocoCounterType = jacocoCounterType;
    }

    private List<Float> getFloats(File ws, String path, CoverageReportParser parser) {
        FileSet fs = Util.createFileSet(ws, path);
        DirectoryScanner ds = fs.getDirectoryScanner();
        String[] files = ds.getIncludedFiles();
        List<Float> cov = new ArrayList<Float>();
        float coverage;
        for (String file : files) {
            coverage = parser.get(new File(ds.getBasedir(), file).getAbsolutePath());
            this.buildLog.println(file + ": " + coverage);
            cov.add(coverage);
        }
        return cov;
    }

    @Override
    public float get(PrintStream buildLog, final FilePath workspace) throws IOException, InterruptedException {
        this.buildLog = buildLog;
        this.buildLog.println(CompareCoverageAction.BUILD_LOG_PREFIX + "parsing coverage files....");
        if (workspace == null) {
            throw new IllegalArgumentException("Workspace should not be null!");
        }
        return workspace.act(this);
    }

    @Override
    public Float invoke(final File ws, final VirtualChannel channel) throws IOException {
        final List<Float> cov = new ArrayList<Float>();
        cov.addAll(getFloats(ws, "**/cobertura.xml", new CoberturaParser()));
        cov.addAll(getFloats(ws, "**/cobertura-coverage.xml", new CoberturaParser()));
        cov.addAll(getFloats(ws, "**/jacoco.xml", new JacocoParser(jacocoCounterType)));
        //default for gradle
        cov.addAll(getFloats(ws, "**/jacocoTestReport.xml", new JacocoParser(jacocoCounterType)));
        cov.addAll(getFloats(ws, "**/clover.xml", new CloverParser()));
        if (!disableSimpleCov) {
            cov.addAll(getFloats(ws, "**/coverage.json", new SimpleCovParser()));
        }

        float s = 0;
        for (float v : cov) {
            s += v;
        }

        if (cov.isEmpty()) {
            return 0f;
        } else {
            return s / cov.size();
        }
    }

}
