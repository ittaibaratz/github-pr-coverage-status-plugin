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

import hudson.EnvVars;
import hudson.model.Build;
import hudson.model.Result;
import hudson.model.TaskListener;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class CompareCoverageActionTest {

    private static final String GIT_URL = "git@github.com:some/my-project.git";
    private static final String CHANGE_TARGET = "master";
    private static final String BRANCH_NAME_PROPERTY = "feature-1";
    private Build build = mock(Build.class);
    private PrintWriter printWriter = mock(PrintWriter.class);
    private TaskListener listener = mock(TaskListener.class);
    private EnvVars envVars = mock(EnvVars.class);

    private TargetCoverageRepository branchCoverageRepository = mock(TargetCoverageRepository.class);
    private CoverageRepository coverageRepository = mock(CoverageRepository.class);
    private SettingsRepository settingsRepository = mock(SettingsRepository.class);
    private PullRequestRepository pullRequestRepository = mock(PullRequestRepository.class);
    private GHRepository ghRepository = mock(GHRepository.class);
    private GHPullRequestCommitDetail commit = mock(GHPullRequestCommitDetail.class);

    private List<GHPullRequestCommitDetail> commits = new ArrayList<GHPullRequestCommitDetail>() {{
        add(mock(GHPullRequestCommitDetail.class));
        add(commit);
    }};
    private PagedIterable<GHPullRequestCommitDetail> pagedIterable = mock(PagedIterable.class);

    private CompareCoverageAction coverageAction = new CompareCoverageAction();

    @Before
    public void initMocks() throws IOException, InterruptedException {
        ServiceRegistry.setTargetCoverageRepository(branchCoverageRepository);
        ServiceRegistry.setCoverageRepository(coverageRepository);
        ServiceRegistry.setSettingsRepository(settingsRepository);
        ServiceRegistry.setPullRequestRepository(pullRequestRepository);
        when(pullRequestRepository.getGitHubRepository(GIT_URL)).thenReturn(ghRepository);
        when(listener.getLogger()).thenReturn(System.out);
    }

    @Before
    public void reinitializeCoverageRepositories() {
        branchCoverageRepository = mock(TargetCoverageRepository.class);
        coverageRepository = mock(CoverageRepository.class);
    }

    @Test
    public void skipStepIfResultOfBuildIsNotSuccess() throws IOException, InterruptedException {
        new CompareCoverageAction().perform(build, null, null, listener);
    }

    @Test
    public void postCoverageStatusToPullRequestAsComment() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        coverageAction.setPublishResultAs("comment");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).comment(ghRepository, 12, "[![feature-1 0.0% (0.0%) vs master 0.0%](aaa/coverage-status-icon/?branchName=feature-1&coverage=0.0&changeTarget=master&targetCoverage=0.0)](aaa/job/a)");
    }

    @Test
    public void postResultAsStatusCheck() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        prepareCommit();
        coverageAction.setPublishResultAs("statusCheck");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).createCommitStatus(
                ghRepository,
                "fh3k2l",
                GHCommitState.SUCCESS,
                "aaa/job/a",
                "feature-1 coverage 0.0% changed 0.0% vs master coverage 0.0%"
        );
    }

    @Test
    public void postResultAsSuccessfulStatusCheck() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        prepareCommit();
        prepareCoverageData(0.887f, 0.955f);
        coverageAction.setPublishResultAs("statusCheck");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).createCommitStatus(
                ghRepository,
                "fh3k2l",
                GHCommitState.SUCCESS,
                "aaa/job/a",
                "feature-1 coverage 95.5% changed +6.8% vs master coverage 88.7%"
        );
    }

    @Test
    public void postResultAsFailedStatusCheck() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        prepareCommit();
        prepareCoverageData(0.9542f, 0.9032f);
        coverageAction.setPublishResultAs("statusCheck");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).createCommitStatus(
                ghRepository,
                "fh3k2l",
                GHCommitState.FAILURE,
                "aaa/job/a",
                "feature-1 coverage 90.32% changed -5.1% vs master coverage 95.42%"
        );
    }

    @Test
    public void keepBuildGreenAndLogErrorIfExceptionDuringGitHubAccess() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        when(listener.error(anyString())).thenReturn(printWriter);
        coverageAction.setPublishResultAs("comment");

        doThrow(new IOException("???")).when(pullRequestRepository).comment(any(GHRepository.class), anyInt(), anyString());

        coverageAction.perform(build, null, null, listener);

        verify(listener).error("Couldn't add comment to pull request #12!");
        verify(printWriter, atLeastOnce()).println(any(Throwable.class));
    }

    @Test
    public void postCoverageStatusToPullRequestAsCommentWithShieldIoIfPrivateJenkinsPublicGitHubTurnOn()
            throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        when(settingsRepository.isPrivateJenkinsPublicGitHub()).thenReturn(true);
        coverageAction.setPublishResultAs("comment");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).comment(ghRepository, 12, "[![feature-1 0.0% (0.0%) vs master 0.0%](https://img.shields.io/badge/coverage-feature--1%200.0%25%20(0.0%25)%20vs%20master%200.0%25-brightgreen.svg)](aaa/job/a)");
    }

    @Test
    public void postCoverageStatusToPullRequestAsCommentWithCustomJenkinsUrlIfConfigured() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        when(settingsRepository.getJenkinsUrl()).thenReturn("customJ");
        coverageAction.setPublishResultAs("comment");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).comment(ghRepository, 12, "[![feature-1 0.0% (0.0%) vs master 0.0%](customJ/coverage-status-icon/?branchName=feature-1&coverage=0.0&changeTarget=master&targetCoverage=0.0)](aaa/job/a)");
    }
    
    private void prepareCoverageData(float targetCoverage, float prCoverage) throws IOException, InterruptedException {
        when(branchCoverageRepository.get(new CoverageMetaData(GIT_URL, CHANGE_TARGET, null))).thenReturn(targetCoverage);
        when(coverageRepository.get(null)).thenReturn(prCoverage);
        initMocks();
    }

    private void prepareCommit() throws IOException {
        GHPullRequest ghPullRequest = mock(GHPullRequest.class);
        when(ghRepository.getPullRequest(12)).thenReturn(ghPullRequest);
        when(ghPullRequest.listCommits()).thenReturn(pagedIterable);
        when(pagedIterable.asList()).thenReturn(commits);
        when(commit.getSha()).thenReturn("fh3k2l");
    }

    private void prepareBuildSuccess() throws IOException, InterruptedException {
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
    }

    private void prepareEnvVars() {
        String buildUrl = "aaa/job/a";
        String prId = "12";
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(prId);
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn(buildUrl);
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(GIT_URL);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_TARGET_PROPERTY)).thenReturn(CHANGE_TARGET);
        when(envVars.get(PrIdAndUrlUtils.BRANCH_NAME_PROPERTY)).thenReturn(BRANCH_NAME_PROPERTY);
    }

}
