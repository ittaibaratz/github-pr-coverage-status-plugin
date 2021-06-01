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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

@SuppressWarnings("WeakerAccess")
final class GetCoverageCallable extends MasterToSlaveFileCallable<Map<String, ReportData>> implements CoverageRepository {

    private final boolean disableSimpleCov;
    private String jacocoCounterType = "";
    private List<String> coverageFilePaths;
    private final List<ReportMetaData> reportsMetaDataList;
    private Map<String, String> labelByFilePath;
    private Map<String, ReportData> coverageByLabel;
    private transient PrintStream buildLog;

    GetCoverageCallable(final boolean disableSimpleCov, final String jacocoCounterType, List<ReportMetaData> reportsMetaDataList) {
        this.disableSimpleCov = disableSimpleCov;
        this.jacocoCounterType = jacocoCounterType;
        this.reportsMetaDataList = reportsMetaDataList;
        this.labelByFilePath = new HashMap<>();
        this.coverageByLabel = new HashMap<>();
    }

    /**
     * Fetch file path to all coverage reports for categorization based on ReportMetaData configuration
     * @param ws
     */
    private void getAllReportPaths(final File ws) {
        DirectoryScanner ds;
        String[] files, paths = new String[]{"**/cobertura.xml", "**/cobertura-coverage.xml", "**/jacoco.xml", "**/jacocoTestReport.xml"};
        for (String path: paths) {
            ds = Util.createFileSet(ws, path).getDirectoryScanner();
            files = ds.getIncludedFiles();
            for (String file: files) {
                coverageFilePaths.add(new File(ds.getBasedir(), file).getAbsolutePath());
            }
        }
        buildLog.println("coverageFilePaths: " + coverageFilePaths);
    }

    /**
     * Add all coverage to "repo" label, reportsMetaDataList should not contain ReportMetaData with "repo" label
     */
    private void categorizeFilePathsToLabels() throws Exception {
        String label;
        coverageByLabel.put("repo", new ReportData());

        for(ReportMetaData reportMetaData: reportsMetaDataList) {
            label = reportMetaData.getLabel();
            if(!coverageByLabel.containsKey(label)) coverageByLabel.put(label, new ReportData());
            else throw new Exception("Repeated label: " + label);
        }

        for(String filePath: coverageFilePaths) {
            for(ReportMetaData reportMetaData: reportsMetaDataList) {
                label = reportMetaData.getLabel();
                if(reportMetaData.validate(filePath)) {
                    if(!labelByFilePath.containsKey(filePath)) labelByFilePath.put(filePath, label);
                    else throw new Exception("Conflicting labels for coverage path: "
                            + filePath
                            + " maps to "
                            + labelByFilePath.get(filePath)
                            + " and "
                            + label
                    );
                }
            }

        }

        buildLog.println("labelByFilePath: " + labelByFilePath);
    }

    private void generateCoverageData() {
        ReportData reportData;
        for(String filePath: coverageFilePaths) {
            if (filePath.contains("cobertura.xml") || filePath.contains("cobertura-coverage.xml"))
                reportData = new CoberturaParser().get(filePath);
            else
                reportData = new JacocoParser(jacocoCounterType).get(filePath);
            if(labelByFilePath.containsKey(filePath)) coverageByLabel.get(labelByFilePath.get(filePath)).add(reportData);
            coverageByLabel.get("repo").add(reportData);
        }
        buildLog.println("coverageByLabel: " + coverageByLabel);
    }

    @Override
    public Map<String, ReportData> get(PrintStream buildLog, final FilePath workspace) throws IOException, InterruptedException {
        this.buildLog = buildLog;
        if (workspace == null) {
            throw new IllegalArgumentException("Workspace should not be null!");
        }
        this.coverageFilePaths = new ArrayList<String>();
//        return workspace.act(new GetCoverageCallable(disableSimpleCov, jacocoCounterType, reportsMetaDataList));
        return workspace.act(this);
    }

    @Override
    public Map<String, ReportData> invoke(final File ws, final VirtualChannel channel) throws IOException {
        getAllReportPaths(ws);
        try {
            categorizeFilePathsToLabels();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        generateCoverageData();

        return coverageByLabel;
    }

}
