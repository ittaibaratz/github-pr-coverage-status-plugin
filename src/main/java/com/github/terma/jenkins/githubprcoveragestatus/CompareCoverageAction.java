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
import java.util.List;
import java.util.Map;

/**
 * Build step to publish pull request coverage status message to GitHub pull request.
 * <p>
 * Workflow:
 * <ul>
 * <li>find coverage of current build and assume it as pull request coverage</li>
 * <li>find branch coverage for repository URL could be taken by {@link BranchCoverageAction} or Sonar {@link Configuration}</li>
 * <li>Publish nice status message to GitHub PR page</li>
 * </ul>
 *
 * @see BranchCoverageAction
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class CompareCoverageAction extends Recorder implements SimpleBuildStep {

    public static final String BUILD_LOG_PREFIX = "[GitHub PR Status] ";

    private static final long serialVersionUID = 1L;
    private String sonarLogin;
    private String sonarPassword;
    private String publishResultAs;
    private String jacocoCoverageCounter;
    private Map<String, String> scmVars;
    private CoverageMetaData coverageMetaData;

    @DataBoundConstructor
    public CompareCoverageAction() {
    }

    public String getPublishResultAs() {
        return publishResultAs;
    }

    public String getJacocoCoverageCounter() {
        return jacocoCoverageCounter;
    }

    // TODO why is this needed for no public field ‘scmVars’ (or getter method) found in class ....
    public Map<String, String> getScmVars() {
        return scmVars;
    }

    public CoverageMetaData getCoverageMetaData() {
        return coverageMetaData;
    }


    @DataBoundSetter
    public void setSonarLogin(String sonarLogin) {
        this.sonarLogin = sonarLogin;
    }

    @DataBoundSetter
    public void setSonarPassword(String sonarPassword) {
        this.sonarPassword = sonarPassword;
    }

    @DataBoundSetter
    public void setPublishResultAs(String publishResultAs) {
        this.publishResultAs = publishResultAs;
    }

    @DataBoundSetter
    public void setJacocoCoverageCounter(String jacocoCoverageCounter) {
        this.jacocoCoverageCounter = jacocoCoverageCounter;
    }

    @DataBoundSetter
    public void setScmVars(Map<String, String> scmVars) {
        this.scmVars = scmVars;
    }

    @DataBoundSetter
    public void setCoverageMetaData(CoverageMetaData coverageMetaData) {
        this.coverageMetaData = coverageMetaData;
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

        final int prId = PrIdAndUrlUtils.getPrId(scmVars, build, listener);
        final String gitUrl = PrIdAndUrlUtils.getGitUrl(scmVars, build, listener);
        final String changeTarget = PrIdAndUrlUtils.getChangeTarget(scmVars, build, listener);
        final String branchName = PrIdAndUrlUtils.getBranchName(scmVars, build, listener);
        buildLog.println(BUILD_LOG_PREFIX + "Git URL: " + gitUrl);
        buildLog.println(BUILD_LOG_PREFIX + "Change Target: " + changeTarget);
        buildLog.println(BUILD_LOG_PREFIX + "Branch Name: " + branchName);

        coverageMetaData.setGitUrl(gitUrl);
        coverageMetaData.setGitBranch(changeTarget);

        final GHRepository gitHubRepository = ServiceRegistry.getPullRequestRepository().getGitHubRepository(gitUrl);

        buildLog.println(BUILD_LOG_PREFIX + "getting target coverage...");
        TargetCoverageRepository targetCoverageRepository = ServiceRegistry
                .getTargetCoverageRepository(buildLog, sonarLogin, sonarPassword);
        final float targetCoverage = targetCoverageRepository.get(gitUrl, changeTarget);
        buildLog.println(BUILD_LOG_PREFIX + gitUrl + "/tree/" + changeTarget + " coverage: " + targetCoverage);

        buildLog.println(BUILD_LOG_PREFIX + "collecting build coverage...");
        final float coverage = ServiceRegistry.getCoverageRepository(settingsRepository.isDisableSimpleCov(),
                jacocoCoverageCounter, coverageMetaData.getReportMetaDataList()).get(workspace);
        buildLog.println(BUILD_LOG_PREFIX + gitUrl + "/tree/" + changeTarget + " coverage: " + coverage);

        final Message message = new Message(coverage, targetCoverage, branchName, changeTarget);
        buildLog.println(BUILD_LOG_PREFIX + message.forConsole());

        final String buildUrl = Utils.getBuildUrl(build, listener);

        String jenkinsUrl = settingsRepository.getJenkinsUrl();
        if (jenkinsUrl == null) jenkinsUrl = Utils.getJenkinsUrlFromBuildUrl(buildUrl);

        if ("comment".equalsIgnoreCase(publishResultAs)) {
            buildLog.println(BUILD_LOG_PREFIX + "publishing result as comment");
            publishComment(message, buildUrl, jenkinsUrl, settingsRepository, gitHubRepository, prId, listener);
        } else {
            buildLog.println(BUILD_LOG_PREFIX + "publishing result as status check");
            publishStatusCheck(message, gitHubRepository, prId, targetCoverage, coverage, buildUrl, listener);
        }
    }

    private void publishComment(
            Message message,
            String buildUrl,
            String jenkinsUrl,
            SettingsRepository settingsRepository,
            GHRepository gitHubRepository,
            int prId,
            TaskListener listener
    ) {
        try {
            final String comment = message.forComment(
                    buildUrl,
                    jenkinsUrl,
                    settingsRepository.getYellowThreshold(),
                    settingsRepository.getGreenThreshold(),
                    settingsRepository.isPrivateJenkinsPublicGitHub());
            ServiceRegistry.getPullRequestRepository().comment(gitHubRepository, prId, comment);
        } catch (Exception ex) {
            PrintWriter pw = listener.error("Couldn't add comment to pull request #" + prId + "!");
            ex.printStackTrace(pw);
        }
    }

    private void publishStatusCheck(
            Message message,
            GHRepository gitHubRepository,
            int prId,
            float targetCoverage,
            float coverage,
            String buildUrl,
            TaskListener listener
    ) {
        try {
            String text = message.forStatusCheck();
            List<GHPullRequestCommitDetail> commits = gitHubRepository.getPullRequest(prId).listCommits().asList();
            ServiceRegistry.getPullRequestRepository().createCommitStatus(
                    gitHubRepository,
                    commits.get(commits.size() - 1).getSha(),
                    coverage < targetCoverage ? GHCommitState.FAILURE : GHCommitState.SUCCESS,
                    buildUrl,
                    message.forStatusCheck()
            );
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
