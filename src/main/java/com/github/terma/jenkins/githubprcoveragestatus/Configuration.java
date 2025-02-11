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

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class Configuration extends AbstractDescribableImpl<Configuration> {

    @Extension
    public static final ConfigurationDescriptor DESCRIPTOR = new ConfigurationDescriptor();

    @DataBoundConstructor
    public Configuration() {
    }

    public static String getGitHubApiUrl() {
        return DESCRIPTOR.getGitHubApiUrl();
    }

    public static int getYellowThreshold() {
        return DESCRIPTOR.getYellowThreshold();
    }

    public static int getGreenThreshold() {
        return DESCRIPTOR.getGreenThreshold();
    }

    public static String getPersonalAccessToken() {
        return DESCRIPTOR.getPersonalAccessToken();
    }

    public static void setBranchCoverage(CoverageMetaData coverageMetaData, Map<String, ReportData> coverageData) {
        DESCRIPTOR.set(coverageMetaData, coverageData);
    }

    @Override
    public ConfigurationDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @SuppressWarnings("unused")
    public static final class ConfigurationDescriptor extends Descriptor<Configuration> implements SettingsRepository {

        private static final int DEFAULT_YELLOW_THRESHOLD = 80;
        private static final int DEFAULT_GREEN_THRESHOLD = 90;

        private final Map<CoverageMetaData, Map<String, ReportData>> coverageByCoverageMetaData = new ConcurrentHashMap<CoverageMetaData, Map<String, ReportData>>();

        private boolean disableSimpleCov;
        private String gitHubApiUrl;
        private String personalAccessToken;
        private String jenkinsUrl;
        private boolean privateJenkinsPublicGitHub;

        private int yellowThreshold = DEFAULT_YELLOW_THRESHOLD;
        private int greenThreshold = DEFAULT_GREEN_THRESHOLD;

        public ConfigurationDescriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Coverage status for GitHub Pull Requests";
        }

        @Nonnull
        public Map<CoverageMetaData, Map<String, ReportData>> getCoverageByCoverageMetaData() {
            return coverageByCoverageMetaData;
        }

        public void set(CoverageMetaData coverageMetaData, Map<String, ReportData> coverageData) {
            coverageByCoverageMetaData.put(coverageMetaData, coverageData);
            save();
        }

        @Override
        public String getGitHubApiUrl() {
            return gitHubApiUrl;
        }

        @Override
        public String getPersonalAccessToken() {
            return personalAccessToken;
        }

        @Override
        public int getYellowThreshold() {
            return yellowThreshold;
        }

        @Override
        public int getGreenThreshold() {
            return greenThreshold;
        }

        @Override
        public boolean isPrivateJenkinsPublicGitHub() {
            return privateJenkinsPublicGitHub;
        }

        @Override
        public boolean isDisableSimpleCov() {
            return disableSimpleCov;
        }

        @Override
        public String getJenkinsUrl() {
            return jenkinsUrl;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            gitHubApiUrl = StringUtils.trimToNull(formData.getString("gitHubApiUrl"));
            personalAccessToken = StringUtils.trimToNull(formData.getString("personalAccessToken"));
            yellowThreshold = NumberUtils.toInt(formData.getString("yellowThreshold"), DEFAULT_YELLOW_THRESHOLD);
            greenThreshold = NumberUtils.toInt(formData.getString("greenThreshold"), DEFAULT_GREEN_THRESHOLD);
            jenkinsUrl = StringUtils.trimToNull(formData.getString("jenkinsUrl"));
            privateJenkinsPublicGitHub = BooleanUtils.toBoolean(formData.getString("privateJenkinsPublicGitHub"));
            disableSimpleCov = BooleanUtils.toBoolean(formData.getString("disableSimpleCov"));
            save();
            return super.configure(req, formData);
        }

    }

}
