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
import java.util.*;

@SuppressWarnings("WeakerAccess")
final class GetCoverageCallable extends MasterToSlaveFileCallable<Map<String, ReportData>> implements CoverageRepository {

    private final boolean disableSimpleCov;
    private String jacocoCounterType = "";
    private final List<ReportMetaData> reportsMetaDataList;

    GetCoverageCallable(final boolean disableSimpleCov, final String jacocoCounterType, List<ReportMetaData> reportsMetaDataList) {
        this.disableSimpleCov = disableSimpleCov;
        this.jacocoCounterType = jacocoCounterType;
        this.reportsMetaDataList = reportsMetaDataList;
    }

    /**
     * Fetch file path to all coverage reports for categorization based on ReportMetaData configuration
     * @param ws
     * @return coverageFilePaths
     */
    private List<String> getAllReportPaths(final File ws) {
        List<String> coverageFilePaths = new ArrayList<String>();
        DirectoryScanner ds;
        String[] files, paths = new String[]{"**/cobertura.xml", "**/cobertura-coverage.xml", "**/jacoco.xml", "**/jacocoTestReport.xml"};
        for (String path: paths) {
            ds = Util.createFileSet(ws, path).getDirectoryScanner();
            files = ds.getIncludedFiles();
            for (String file: files) {
                coverageFilePaths.add(new File(ds.getBasedir(), file).getAbsolutePath());
            }
        }
//        System.out.println("coverageFilePaths: " + coverageFilePaths);
        return coverageFilePaths;
    }

    /**
     * initialize coverageByLabel and include overall coverage under "repo" label in addition to other labels
     * @param coverageFilePaths
     * @return coverageByLabel
     * @throws Exception
     */
    public Map<String, ReportData> initializeCoverageByLabel(List<String> coverageFilePaths) throws Exception{
        String label;
        Map<String, ReportData> coverageByLabel = new HashMap<>();
        coverageByLabel.put("repo", new ReportData());

        for(ReportMetaData reportMetaData: reportsMetaDataList) {
            label = reportMetaData.getLabel();
            if(!coverageByLabel.containsKey(label)) coverageByLabel.put(label, new ReportData());
            else throw new Exception("Repeated label: " + label);
        }

        return coverageByLabel;
    }

    /**
     * Add all coverage to "repo" label, reportsMetaDataList should not contain ReportMetaData with "repo" label
     * @param coverageFilePaths
     * @return labelByFilePath
     * @throws Exception
     */
    private Map<String, String> categorizeFilePathsToLabels(List<String> coverageFilePaths) throws Exception {
        String label;
        Map<String, String> labelByFilePath = new HashMap<>();

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
//        System.out.println("labelByFilePath: " + labelByFilePath);
        return labelByFilePath;
    }

    /**
     * aggregates coverage based on RepoMetaData configuration under a specific label
     * @param coverageFilePaths
     * @param labelByFilePath
     * @param coverageByLabel
     * @return coverageByLabel
     */
    private Map<String, ReportData> generateCoverageData(List<String> coverageFilePaths,
                                      Map<String, String> labelByFilePath,
                                      Map<String, ReportData> coverageByLabel) {
        ReportData reportData;
        for(String filePath: coverageFilePaths) {
            if (filePath.contains("cobertura.xml") || filePath.contains("cobertura-coverage.xml"))
                reportData = new CoberturaParser().get(filePath);
            else
                reportData = new JacocoParser(jacocoCounterType).get(filePath);
            if(labelByFilePath.containsKey(filePath)) coverageByLabel.get(labelByFilePath.get(filePath)).add(reportData);
            coverageByLabel.get("repo").add(reportData);
        }
//        System.out.println("coverageByLabel: " + coverageByLabel);
        return coverageByLabel;
    }

    @Override
    public Map<String, ReportData> get(final FilePath workspace) throws IOException, InterruptedException {
        if (workspace == null) {
            throw new IllegalArgumentException("Workspace should not be null!");
        }
//        return workspace.act(new GetCoverageCallable(disableSimpleCov, jacocoCounterType, reportsMetaDataList));
        return workspace.act(this);
    }

    @Override
    public Map<String, ReportData> invoke(final File ws, final VirtualChannel channel) throws IOException {
        try {
            List<String> coverageFilePaths = getAllReportPaths(ws);
            Map<String, ReportData> coverageByLabel = initializeCoverageByLabel(coverageFilePaths);
            Map<String, String> labelByFilePath = categorizeFilePathsToLabels(coverageFilePaths);
            return generateCoverageData(coverageFilePaths, labelByFilePath, coverageByLabel);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

}
