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
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

/**
 * Record branch coverage of Jenkins Build.
 * Branch coverage will be used to compare Pull Request coverage and provide status message in Pull Request.
 * Optional step as coverage could be taken from Sonar. Take a look on {@link Configuration}
 *
 * @see CompareCoverageAction
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BranchCoverageAction extends Recorder implements SimpleBuildStep {

    public static final String DISPLAY_NAME = "Record Branch Coverage";
    private static final long serialVersionUID = 1L;

    private String jacocoCounterType;
    private Map<String, String> scmVars;
    private CoverageMetaData coverageMetaData;

    @DataBoundConstructor
    public BranchCoverageAction() {

    }

    public String getJacocoCounterType() {
        return jacocoCounterType;
    }

    // TODO why is this needed for no public field ‘scmVars’ (or getter method) found in class ....
    public Map<String, String> getScmVars() {
        return scmVars;
    }

    public CoverageMetaData getCoverageMetaData() {
        return coverageMetaData;
    }

    @DataBoundSetter
    public void setJacocoCounterType(String jacocoCounterType) {
        this.jacocoCounterType = jacocoCounterType;
    }

    @DataBoundSetter
    public void setScmVars(Map<String, String> scmVars) {
        this.scmVars = scmVars;
    }

    @DataBoundSetter
    public void setCoverageMetaData(CoverageMetaData coverageMetaData) {
        this.coverageMetaData = coverageMetaData;
    }


    @SuppressWarnings("NullableProblems")
    @Override
    public void perform(final Run build, final FilePath workspace, final Launcher launcher,
                        final TaskListener listener) throws InterruptedException, IOException {
        if (build.getResult() != Result.SUCCESS) return;

        final PrintStream buildLog = listener.getLogger();
        final String gitUrl = PrIdAndUrlUtils.getGitUrl(scmVars, build, listener);
        final String gitBranch = PrIdAndUrlUtils.getGitBranch(scmVars, build, listener);
        buildLog.println("Git URL: " + gitUrl);
        buildLog.println("Git Branch: " + gitBranch);
        coverageMetaData.setGitUrl(gitUrl);
        coverageMetaData.setGitBranch(gitBranch);

        final boolean disableSimpleCov = ServiceRegistry.getSettingsRepository().isDisableSimpleCov();
        final String jacocoCounterType = this.jacocoCounterType;

        final float branchCoverage = ServiceRegistry.getCoverageRepository(disableSimpleCov, jacocoCounterType, coverageMetaData.getReportMetaDataList())
                .get(workspace);
        buildLog.println("Branch coverage " + Percent.toWholeString(branchCoverage));
        Configuration.setBranchCoverage(gitUrl, gitBranch, branchCoverage);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        @Nonnull
        public String getDisplayName() {
            return DISPLAY_NAME;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }

}
