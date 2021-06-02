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

import org.junit.Assert;
import org.junit.Test;

public class MessageTest {

    @Test
    public void buildNiceForConsole() {
        Assert.assertEquals("repo : feature-1 coverage 100.0% changed 0.0% vs master coverage 100.0%", new Message("repo", 1, 1, "feature-1", "master").forConsole());
        Assert.assertEquals("repo : feature-1 coverage 0.0% changed 0.0% vs master coverage 0.0%", new Message("repo", 0, 0, "feature-1", "master").forConsole());
        Assert.assertEquals("repo : feature-1 coverage 50.0% changed +50.0% vs master coverage 0.0%", new Message("repo", 0.5f, 0, "feature-1", "master").forConsole());
        Assert.assertEquals("repo : feature-1 coverage 0.0% changed -50.0% vs master coverage 50.0%", new Message("repo", 0, 0.5f, "feature-1", "master").forConsole());
        Assert.assertEquals("repo : feature-1 coverage 70.0% changed +20.0% vs master coverage 50.0%", new Message("repo", 0.7f, 0.5f, "feature-1", "master").forConsole());
        Assert.assertEquals("repo : feature-1 coverage 0.07% changed +0.02% vs staging coverage 0.05%", new Message("repo", 0.0007f, 0.0005f, "feature-1", "staging").forConsole());
        Assert.assertEquals("repo : feature-1 coverage 0.0% changed 0.0% vs develop coverage 0.0%", new Message("repo", 0.000007f, 0.000005f, "feature-1", "develop").forConsole());
    }

    @Test
    public void buildNiceForIcon() {
        Assert.assertEquals("feature-1 100.0% (0.0%) vs master 100.0%", new Message("repo", 1, 1, "feature-1", "master").forIcon());
        Assert.assertEquals("feature-1 0.0% (0.0%) vs master 0.0%", new Message("repo", 0, 0, "feature-1", "master").forIcon());
        Assert.assertEquals("feature-1 50.0% (+50.0%) vs master 0.0%", new Message("repo", 0.5f, 0, "feature-1", "master").forIcon());
        Assert.assertEquals("feature-1 0.0% (-50.0%) vs master 50.0%", new Message("repo", 0, 0.5f, "feature-1", "master").forIcon());
        Assert.assertEquals("feature-1 70.0% (+20.0%) vs master 50.0%", new Message("repo", 0.7f, 0.5f, "feature-1", "master").forIcon());
        Assert.assertEquals("feature-1 68.6% (-0.7%) vs master 69.3%", new Message("repo", 0.686f, 0.693f, "feature-1", "master").forIcon());
        Assert.assertEquals("feature-1 60.07% (+0.06%) vs master 60.01%", new Message("repo", 0.6007f, 0.6001f, "feature-1", "master").forIcon());
        Assert.assertEquals("feature-1 0.01% (+0.01%) vs staging 0.0%", new Message("repo", 0.00007f, 0.00001f, "feature-1", "staging").forIcon());
        Assert.assertEquals("feature-1 0.0% (0.0%) vs develop 0.0%", new Message("repo", 0.000007f, 0.000001f, "feature-1", "develop").forIcon());
    }

    @Test
    public void forCommentWithShieldIo() {
        String buildUrl = "http://terma.com/jenkins/job/ama";
        Assert.assertEquals(
                "[![feature-1 100.0% (0.0%) vs master 100.0%](https://img.shields.io/badge/repo-feature--1%20100.0%25%20(0.0%25)%20vs%20master%20100.0%25-brightgreen.svg)](http://terma.com/jenkins/job/ama)",
                new Message("repo", 1, 1, "feature-1", "master").forComment(buildUrl, null, 80, 90, true));

        Assert.assertEquals(
                "[![feature-1 0.0% (0.0%) vs master 0.0%](https://img.shields.io/badge/repo-feature--1%200.0%25%20(0.0%25)%20vs%20master%200.0%25-brightgreen.svg)](http://terma.com/jenkins/job/ama)",
                new Message("repo", 0, 0, "feature-1", "master").forComment(buildUrl, null, 80, 90, true));

        Assert.assertEquals(
                "[![feature-1 50.0% (+50.0%) vs master 0.0%](https://img.shields.io/badge/repo-feature--1%2050.0%25%20(%2B50.0%25)%20vs%20master%200.0%25-brightgreen.svg)](http://terma.com/jenkins/job/ama)",
                new Message("repo", 0.5f, 0, "feature-1", "master").forComment(buildUrl, null, 80, 90, true));

        Assert.assertEquals(
                "[![feature-1 0.0% (-50.0%) vs staging 50.0%](https://img.shields.io/badge/backend-feature--1%200.0%25%20(--50.0%25)%20vs%20staging%2050.0%25-red.svg)](http://terma.com/jenkins/job/ama)",
                new Message("backend", 0, 0.5f, "feature-1", "staging").forComment(buildUrl, null, 80, 90, true));

        Assert.assertEquals(
                "[![feature-1 85.0% (+35.0%) vs develop 50.0%](https://img.shields.io/badge/frontend-feature--1%2085.0%25%20(%2B35.0%25)%20vs%20develop%2050.0%25-brightgreen.svg)](http://terma.com/jenkins/job/ama)",
                new Message("frontend", 0.85f, 0.5f, "feature-1", "develop").forComment(buildUrl, null, 80, 90, true));
    }

    @Test
    public void forComment() {
        String buildUrl = "http://terma.com/jenkins/job/ama";
        String jenkinsUrl = "jenkinsUrl";
        Assert.assertEquals(
                "[![feature-1 100.0% (0.0%) vs master 100.0%](jenkinsUrl/coverage-status-icon/?label=repo&branchName=feature-1&coverage=1.0&changeTarget=master&targetCoverage=1.0)](http://terma.com/jenkins/job/ama)",
                new Message("repo", 1, 1, "feature-1", "master").forComment(buildUrl, jenkinsUrl, 0, 0, false));

        Assert.assertEquals(
                "[![feature-1 0.0% (0.0%) vs master 0.0%](jenkinsUrl/coverage-status-icon/?label=repo&branchName=feature-1&coverage=0.0&changeTarget=master&targetCoverage=0.0)](http://terma.com/jenkins/job/ama)",
                new Message("repo", 0, 0, "feature-1", "master").forComment(buildUrl, jenkinsUrl, 0, 0, false));

        Assert.assertEquals(
                "[![feature-1 50.0% (+50.0%) vs master 0.0%](jenkinsUrl/coverage-status-icon/?label=repo&branchName=feature-1&coverage=0.5&changeTarget=master&targetCoverage=0.0)](http://terma.com/jenkins/job/ama)",
                new Message("repo", 0.5f, 0, "feature-1", "master").forComment(buildUrl, jenkinsUrl, 0, 0, false));

        Assert.assertEquals(
                "[![feature-1 0.0% (-50.0%) vs staging 50.0%](jenkinsUrl/coverage-status-icon/?label=backend&branchName=feature-1&coverage=0.0&changeTarget=staging&targetCoverage=0.5)](http://terma.com/jenkins/job/ama)",
                new Message("backend", 0, 0.5f, "feature-1", "staging").forComment(buildUrl, jenkinsUrl, 0, 0, false));

        Assert.assertEquals(
                "[![feature-1 70.0% (+20.0%) vs develop 50.0%](jenkinsUrl/coverage-status-icon/?label=frontend&branchName=feature-1&coverage=0.7&changeTarget=develop&targetCoverage=0.5)](http://terma.com/jenkins/job/ama)",
                new Message("frontend", 0.7f, 0.5f, "feature-1", "develop").forComment(buildUrl, jenkinsUrl, 0, 0, false));
    }

}
