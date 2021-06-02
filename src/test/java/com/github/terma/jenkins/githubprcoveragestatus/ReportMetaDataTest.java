package com.github.terma.jenkins.githubprcoveragestatus;

import org.junit.Assert;
import org.junit.Test;

public class ReportMetaDataTest {

    private ReportMetaData reportMetaData;

    @Test
    public void validWhenIncludesAndExcludesNull() {
        reportMetaData = new ReportMetaData("frontend");
        Assert.assertTrue(reportMetaData.validate("/trunk/webapps/cxstudio-u"));
    }

    @Test
    public void notValidWhenIncludesNoMatchAndExcludesNull() {
        reportMetaData = new ReportMetaData("frontend", "cxstudio-ui|DashboardFrontend", null);
        Assert.assertFalse(reportMetaData.validate("/trunk/webapps/cxstudio-u"));
    }

    @Test
    public void notValidWhenIncludesNullAndExcludesNoMatch() {
        reportMetaData = new ReportMetaData("frontend", null, "cxstudio-ui");
        Assert.assertFalse(reportMetaData.validate("/trunk/webapps/cxstudio-ui"));
    }

    @Test
    public void validWhenIncludesMatchAndExcludesNull() {
        reportMetaData = new ReportMetaData("frontend", "cxstudio-ui|DashboardFrontend", null);
        Assert.assertTrue(reportMetaData.validate("/trunk/webapps/cxstudio-ui"));
    }

    @Test
    public void validWhenIncludesNullAndExcludesMatch() {
        reportMetaData = new ReportMetaData("frontend", null, "authentication");
        Assert.assertTrue(reportMetaData.validate("/trunk/webapps/cxstudio-ui"));
    }

    @Test
    public void validWhenIncludesAndExcludesMatch() {
        reportMetaData = new ReportMetaData("frontend", "cxstudio-ui|DashboardFrontend", "authentication");
        Assert.assertTrue(reportMetaData.validate("/trunk/webapps/cxstudio-ui"));
    }

    @Test
    public void notValidWhenOneOfIncludesOrExcludesMatch() {
        reportMetaData = new ReportMetaData("frontend", "cxstudio-ui|DashboardFrontend", "authentication");
        Assert.assertFalse(reportMetaData.validate("/trunk/webapps/cxstudio-u"));
    }
}
