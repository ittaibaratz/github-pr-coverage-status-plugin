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
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
final class GetCoverageCallable extends MasterToSlaveFileCallable<Map<String, ReportData>> implements CoverageRepository {

    private final boolean disableSimpleCov;
    private String jacocoCounterType = "";
    private List<String> coverageFilePaths;
    private final List<ReportMetaData> reportsMetaDataList;
    private Map<String, String> keyByFilePath = new HashMap<>();
    private Map<String, ReportData> coverageByKey = new HashMap<>();
    private final Logger LOGGER = Logger.getLogger(GetCoverageCallable.class.getName());

    GetCoverageCallable(final boolean disableSimpleCov, final String jacocoCounterType, List<ReportMetaData> reportsMetaDataList) {
        this.disableSimpleCov = disableSimpleCov;
        this.jacocoCounterType = jacocoCounterType;
        this.reportsMetaDataList = reportsMetaDataList;
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
                this.coverageFilePaths.add(new File(ds.getBasedir(), file).getAbsolutePath());
            }
        }
        LOGGER.log(Level.INFO, this.coverageFilePaths.toString());
    }

    /**
     * Add all coverage to "repo" key, reportsMetaDataList should not contain ReportMetaData with "repo" key
     */
    private void categorizeFilePathsToKeys() throws Exception {
        String key;
        coverageByKey.put("repo", new ReportData());

        for(ReportMetaData reportMetaData: this.reportsMetaDataList) {
            key = reportMetaData.getKey();
            // coverageByKey -> keys.forEach(key -> new ReportData())
            if(!coverageByKey.containsKey(key)) coverageByKey.put(key, new ReportData());
            else throw new Exception("repeated key: " + key);

            for(String filePath: this.coverageFilePaths) {
                if(reportMetaData.validate(filePath)) {
                    if(!keyByFilePath.containsKey(filePath)) keyByFilePath.put(filePath, key);
                    else throw new Exception("conflicting keys, coverage path: "
                            + filePath
                            + " maps to "
                            + keyByFilePath.get(filePath)
                            + " and "
                            + key
                    );
                }
            }
        }
    }

    private void generateCoverageData() {
        String key;
        ReportData reportData;
        for(String filePath: this.coverageFilePaths) {
            key = keyByFilePath.get(filePath);
            if (filePath.contains("cobertura.xml") || filePath.contains("cobertura-coverage.xml"))
                reportData = new CoberturaParser().get(filePath);
            else
                reportData = new JacocoParser(jacocoCounterType).get(filePath);
            
            coverageByKey.get(key).add(reportData);
            coverageByKey.get("repo").add(reportData);
        }
    }

    @Override
    public Map<String, ReportData> get(final FilePath workspace) throws IOException, InterruptedException {
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
            categorizeFilePathsToKeys();
        } catch (Exception e) {
            e.printStackTrace();
        }
        generateCoverageData();

        return this.coverageByKey;
    }

}
