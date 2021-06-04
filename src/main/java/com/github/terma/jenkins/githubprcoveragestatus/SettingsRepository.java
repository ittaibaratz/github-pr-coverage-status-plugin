package com.github.terma.jenkins.githubprcoveragestatus;

interface SettingsRepository {

    String getGitHubApiUrl();

    String getPersonalAccessToken();

    String getJenkinsUrl();

    boolean isPrivateJenkinsPublicGitHub();

    boolean isDisableSimpleCov();

}
