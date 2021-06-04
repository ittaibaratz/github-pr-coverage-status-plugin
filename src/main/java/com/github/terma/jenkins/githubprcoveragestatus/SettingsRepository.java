package com.github.terma.jenkins.githubprcoveragestatus;

interface SettingsRepository {

    String getGitHubApiUrl();

    String getPersonalAccessToken();

    String getJenkinsUrl();

    boolean isPrivateJenkinsPublicGitHub();

<<<<<<< HEAD
    boolean isDisableSimpleCov();

=======
//    boolean isUseSonarForTargetCoverage();

    boolean isDisableSimpleCov();

//    String getSonarUrl();
//
//    String getSonarToken();
>>>>>>> e83207f0709499d795260ce707d81732c608b54d
}
