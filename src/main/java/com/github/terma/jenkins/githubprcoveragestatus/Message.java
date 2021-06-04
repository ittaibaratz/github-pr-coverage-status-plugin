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

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

@SuppressWarnings("WeakerAccess")
class Message {

    //see http://shields.io/ for reference
    private static final String BADGE_TEMPLATE = "https://img.shields.io/badge/%s-%s-%s.svg";

    private static final String COLOR_RED = "red";
    private static final String COLOR_GREEN = "brightgreen";

    private final String label;
    private final float coverage;
    private final float targetCoverage;
    private final String branchName;
    private final String changeTarget;

    public Message(String label, float coverage, float targetCoverage, String branchName, String changeTarget) {
        this.label = label;
        this.coverage = Percent.roundFourAfterDigit(coverage);
        this.targetCoverage = Percent.roundFourAfterDigit(targetCoverage);
        this.branchName = branchName;
        this.changeTarget = changeTarget;
    }

    public String forConsole() {
        return String.format("%s : %s coverage %s changed %s vs %s coverage %s",
                label,
                branchName,
                Percent.toWholeNoSignString(coverage),
                Percent.toString(Percent.change(coverage, targetCoverage)),
                changeTarget,
                Percent.toWholeNoSignString(targetCoverage));
    }

    @Override
    public String toString() {
        return forConsole();
    }

    public String forComment(
            final String buildUrl, final String jenkinsUrl,
<<<<<<< HEAD
=======
            final int yellowThreshold, final int greenThreshold,
>>>>>>> e83207f0709499d795260ce707d81732c608b54d
            final boolean useShieldsIo) {
        final String icon = forIcon();
        if (useShieldsIo) {
            return "[![" + icon + "](" + shieldIoUrl(icon) + ")](" + buildUrl + ")";
        } else {
            return "[![" + icon + "](" + jenkinsUrl + "/coverage-status-icon/" +
                    "?label=" + label +
                    "&branchName=" + branchName +
                    "&coverage=" + coverage +
                    "&changeTarget=" + changeTarget +
                    "&targetCoverage=" + targetCoverage +
                    ")](" + buildUrl + ")";
        }
    }

    public String forStatusCheck() {
        return String.format("%s : %s %s (%s) vs %s %s",
                label,
                branchName,
                Percent.toWholeNoSignString(coverage),
                Percent.toString(Percent.change(coverage, targetCoverage)),
                changeTarget,
                Percent.toWholeNoSignString(targetCoverage));
<<<<<<< HEAD
=======
    }

    public boolean hasFailed(final int yellowThreshold, final int greenThreshold) {
        return !getColor(yellowThreshold, greenThreshold).equals(COLOR_GREEN);
>>>>>>> e83207f0709499d795260ce707d81732c608b54d
    }

    private String shieldIoUrl(String icon) {
        final String color = getColor();
        // dash should be encoded as two dash
        icon = icon.replace("-", "--");
        try {
            return String.format(BADGE_TEMPLATE, label, URIUtil.encodePath(icon), color);
        } catch (URIException e) {
            throw new RuntimeException(e);
        }
    }

<<<<<<< HEAD
    private String getColor() {
        return coverage >= targetCoverage ? COLOR_GREEN : COLOR_RED;
=======
    private String getColor(int yellowThreshold, int greenThreshold) {
        String color = COLOR_GREEN;
        final int coveragePercent = Percent.of(coverage);
        final boolean isCoverageHigher = Percent.change(coverage, targetCoverage) >= 0;
        if (isCoverageHigher) {
            return color;
        } else if (coveragePercent < yellowThreshold) {
            color = COLOR_RED;
        } else if (coveragePercent < greenThreshold) {
            color = COLOR_YELLOW;
        }
        return color;
>>>>>>> e83207f0709499d795260ce707d81732c608b54d
    }

    /**
     * Example: PR-12 92% (+23%) vs master 70%
     */
    public String forIcon() {
        return String.format("%s %s (%s) vs %s %s",
                branchName,
                Percent.toWholeNoSignString(coverage),
                Percent.toString(Percent.change(coverage, targetCoverage)),
                changeTarget,
                Percent.toWholeNoSignString(targetCoverage));
    }

}
