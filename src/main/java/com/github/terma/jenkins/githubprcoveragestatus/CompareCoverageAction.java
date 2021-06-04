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
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHPullRequestCommitDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Build step to publish pull request coverage status message to GitHub pull request.
 * <p>
 * Workflow:
 * <ul>
 * <li>find coverage of current build and assume it as pull request coverage</li>
 * <li>find branch coverage for repository URL taken by {@link BranchCoverageAction}</li>
 * <li>Publish nice status message to GitHub PR page</li>
 * </ul>
 *
 * @see BranchCoverageAction
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class CompareCoverageAction extends Recorder implements SimpleBuildStep {

    public static final String BUILD_LOG_PREFIX = "[GitHub PR Status] ";

    private static final long serialVersionUID = 1L;
    private String publishResultAs;
    private String jacocoCounterType;
    private Map<String, String> scmVars;
    private List<ReportMetaData> reportMetaDataList;

    @DataBoundConstructor
    public CompareCoverageAction() {
    }

    public String getPublishResultAs() {
        return publishResultAs;
    }

    public String getJacocoCounterType() {
        return jacocoCounterType;
    }

    // TODO why is this needed for no public field ‘scmVars’ (or getter method) found in class ....
    public Map<String, String> getScmVars() {
        return scmVars;
    }

    public List<ReportMetaData> getReportMetaDataList() {
        return reportMetaDataList;
    }

    @DataBoundSetter
    public void setPublishResultAs(String publishResultAs) {
        this.publishResultAs = publishResultAs;
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
    public void setReportMetaDataList(List<ReportMetaData> reportMetaDataList) {
        this.reportMetaDataList = reportMetaDataList;
    }


    // todo show message that addition comment in progress as it could take a while
    @SuppressWarnings("NullableProblems")
    @Override
    public void perform(
            final Run build,
            final FilePath workspace,
            final Launcher launcher,
            final TaskListener listener
    ) throws InterruptedException, IOException {
        final PrintStream buildLog = listener.getLogger();

        if (build.getResult() != Result.SUCCESS) {
            buildLog.println(BUILD_LOG_PREFIX + "skip, build is red");
            return;
        }

        buildLog.println(BUILD_LOG_PREFIX + "start");

        final SettingsRepository settingsRepository = ServiceRegistry.getSettingsRepository();

        final String buildUrl = Utils.getBuildUrl(build, listener);

        String jenkinsUrl = settingsRepository.getJenkinsUrl();
        if (jenkinsUrl == null) jenkinsUrl = Utils.getJenkinsUrlFromBuildUrl(buildUrl);

        final int prId = PrIdAndUrlUtils.getPrId(scmVars, build, listener);
        final String gitUrl = PrIdAndUrlUtils.getGitUrl(scmVars, build, listener);
        final String changeTarget = PrIdAndUrlUtils.getChangeTarget(scmVars, build, listener);
        final String branchName = PrIdAndUrlUtils.getBranchName(scmVars, build, listener);
        buildLog.println(BUILD_LOG_PREFIX + "Git URL: " + gitUrl);
        buildLog.println(BUILD_LOG_PREFIX + "Change Target: " + changeTarget);
        buildLog.println(BUILD_LOG_PREFIX + "Branch Name: " + branchName);

        CoverageMetaData coverageMetaData = new CoverageMetaData(gitUrl, changeTarget, reportMetaDataList);
        buildLog.println(BUILD_LOG_PREFIX + "CoverageMetaData: " + coverageMetaData);

        final GHRepository gitHubRepository = ServiceRegistry.getPullRequestRepository().getGitHubRepository(buildLog, gitUrl);

        buildLog.println(BUILD_LOG_PREFIX + "getting target coverage...");
        Map<String, ReportData> targetCoverageData = ServiceRegistry
                .getTargetCoverageRepository(buildLog).get(coverageMetaData);
        buildLog.println(BUILD_LOG_PREFIX + " targetCoverage: " + targetCoverageData);

        buildLog.println(BUILD_LOG_PREFIX + "collecting build coverage...");
        Map<String, ReportData> coverageData = ServiceRegistry.getCoverageRepository(settingsRepository.isDisableSimpleCov(),
                jacocoCounterType, coverageMetaData.getReportMetaDataList()).get(workspace);
        buildLog.println(BUILD_LOG_PREFIX + " coverage: " + coverageData);

        if(targetCoverageData==null) buildLog.println(BUILD_LOG_PREFIX + " Record Branch Coverage with CoverageMetaData: " + coverageMetaData);

        List<Message> messages = new ArrayList<>();
        float coverage, targetCoverage;
        for(String label: coverageData.keySet()) {
            coverage = coverageData.get(label).getRate();

            if(targetCoverageData!=null && targetCoverageData.containsKey(label)) targetCoverage = targetCoverageData.get(label).getRate();
            else targetCoverage = 0;

            messages.add(new Message(label, coverage, targetCoverage, branchName, changeTarget));
        }
        buildLog.println(BUILD_LOG_PREFIX + " messages: " + messages);

        if ("comment".equalsIgnoreCase(publishResultAs)) {
            buildLog.println(BUILD_LOG_PREFIX + "publishing result as comment");
            publishComment(messages, buildUrl, jenkinsUrl, settingsRepository, gitHubRepository, prId, listener);
        } else {
            buildLog.println(BUILD_LOG_PREFIX + "publishing result as status check");
            publishStatusCheck(messages, buildUrl, settingsRepository, gitHubRepository, prId, listener);
        }

    }

    private void publishComment(
            List<Message> messages,
            String buildUrl,
            String jenkinsUrl,
            SettingsRepository settingsRepository,
            GHRepository gitHubRepository,
            int prId,
            TaskListener listener
    ) {
        try {
            String comment = messages.stream()
                    .map(message -> message.forComment(
                        buildUrl,
                        jenkinsUrl,
<<<<<<< HEAD
=======
                        settingsRepository.getYellowThreshold(),
                        settingsRepository.getGreenThreshold(),
>>>>>>> e83207f0709499d795260ce707d81732c608b54d
                        settingsRepository.isPrivateJenkinsPublicGitHub()
                    )).collect(Collectors.joining("\n"));
            ServiceRegistry.getPullRequestRepository().comment(gitHubRepository, prId, comment);
        } catch (Exception ex) {
            PrintWriter pw = listener.error("Couldn't add comment to pull request #" + prId + "!");
            ex.printStackTrace(pw);
        }
    }

    private void publishStatusCheck(
            List<Message> messages,
            String buildUrl,
            SettingsRepository settingsRepository,
            GHRepository gitHubRepository,
            int prId,
            TaskListener listener
    ) {
        try {
            List<GHPullRequestCommitDetail> commits = gitHubRepository.getPullRequest(prId).listCommits().asList();
            for(Message message: messages) {
                ServiceRegistry.getPullRequestRepository().createCommitStatus(
                    gitHubRepository,
                    commits.get(commits.size() - 1).getSha(),
<<<<<<< HEAD
                    GHCommitState.SUCCESS,
=======
                    message.hasFailed(
                        settingsRepository.getYellowThreshold(),
                        settingsRepository.getGreenThreshold()
                    ) ? GHCommitState.FAILURE : GHCommitState.SUCCESS,
>>>>>>> e83207f0709499d795260ce707d81732c608b54d
                    buildUrl,
                    message.forStatusCheck()
                );
            }
        } catch (Exception e) {
            PrintWriter pw = listener.error("Couldn't add status check to pull request #" + prId + "!");
            e.printStackTrace(pw);
        }
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
            return "Publish coverage to GitHub";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }

}
