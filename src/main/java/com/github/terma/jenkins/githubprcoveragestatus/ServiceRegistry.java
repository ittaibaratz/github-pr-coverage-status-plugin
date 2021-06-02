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

import java.io.PrintStream;
import java.util.List;

public class ServiceRegistry {

    private static TargetCoverageRepository targetCoverageRepository;
    private static CoverageRepository coverageRepository;
    private static SettingsRepository settingsRepository;
    private static PullRequestRepository pullRequestRepository;

    public static TargetCoverageRepository getTargetCoverageRepository(PrintStream buildLog) {
        if (targetCoverageRepository != null) return targetCoverageRepository;

        return new BuildTargetCoverageRepository(buildLog);
    }

    public static void setTargetCoverageRepository(TargetCoverageRepository targetCoverageRepository) {
        ServiceRegistry.targetCoverageRepository = targetCoverageRepository;
    }

    public static CoverageRepository getCoverageRepository(
            final boolean disableSimpleCov,
            final String jacocoCoverageCounter,
            List<ReportMetaData> reportsMetaData
    ) {
        return coverageRepository != null ? coverageRepository
                : new GetCoverageCallable(disableSimpleCov, jacocoCoverageCounter, reportsMetaData);
    }

    public static void setCoverageRepository(CoverageRepository coverageRepository) {
        ServiceRegistry.coverageRepository = coverageRepository;
    }

    public static SettingsRepository getSettingsRepository() {
        return settingsRepository != null ? settingsRepository : Configuration.DESCRIPTOR;
    }

    public static void setSettingsRepository(SettingsRepository settingsRepository) {
        ServiceRegistry.settingsRepository = settingsRepository;
    }

    public static PullRequestRepository getPullRequestRepository() {
        return pullRequestRepository != null ? pullRequestRepository : new GitHubPullRequestRepository();
    }

    public static void setPullRequestRepository(PullRequestRepository pullRequestRepository) {
        ServiceRegistry.pullRequestRepository = pullRequestRepository;
    }
}
