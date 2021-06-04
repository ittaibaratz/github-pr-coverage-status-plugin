package com.github.terma.jenkins.githubprcoveragestatus;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;

@Extension
public class CoverageStatusIconAction implements UnprotectedRootAction {

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "coverage-status-icon";
    }

    /**
     * Used by Jenkins Stapler service when get request on URL jenkins_host/getUrlName()
     *
     * @param request - request
     * @param response - response
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public void doIndex(StaplerRequest request, StaplerResponse response) throws IOException {
        final String label = request.getParameter("label");
        final String branchName = request.getParameter("branchName");
        final float coverage = Float.parseFloat(request.getParameter("coverage"));
        final String changeTarget = request.getParameter("changeTarget");
        final float targetCoverage = Float.parseFloat(request.getParameter("targetCoverage"));

        response.setContentType("image/svg+xml");

        String svg = IOUtils.toString(this.getClass().getResourceAsStream(
                "/com/github/terma/jenkins/githubprcoveragestatus/Icon/icon.svg"));

        final Message message = new Message(label, coverage, targetCoverage, branchName, changeTarget);
        svg = StringUtils.replace(svg, "{{ message }}", message.forIcon());
        svg = StringUtils.replace(svg, "coverage", label);

        String color = coverage >= targetCoverage ? "#97CA00" : "#b94947";
        svg = StringUtils.replace(svg, "{{ color }}", color);

        response.getWriter().write(svg);
    }

}
