package com.github.terma.jenkins.githubprcoveragestatus;

import java.io.Serializable;

public class ReportData implements Serializable {
    private int covered;
    private int total;

    public ReportData() {
        this.covered = 0;
        this.total = 0;
    }

    public ReportData(int covered, int total) {
        this.covered = covered;
        this.total = total;
    }

    public void add(ReportData other) {
        this.covered += other.covered;
        this.total += other.total;
    }

    public float getRate() {
        return  (float) covered / (float) total;
    }

    @Override
    public String toString() {
        return "ReportData{" +
                "covered=" + covered +
                ", total=" + total +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportData that = (ReportData) o;

        if (covered != that.covered) return false;
        return total == that.total;
    }

    @Override
    public int hashCode() {
        int result = covered;
        result = 31 * result + total;
        return result;
    }
}
