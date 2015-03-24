package com.hp.mercury.ci.jenkins.plugins.downstreamlogs;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiService;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.JenkinsCiService;
import hudson.model.Job;
import hudson.model.Run;

/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 18/06/13
 * Time: 04:52
 * To change this template use File | Settings | File Templates.
 */
public abstract class BuildStreamTreeEntry implements Comparable<BuildStreamTreeEntry> {

    @Deprecated
    transient private String path;
    @Deprecated
    transient private String template;

    protected CiService ciService;

    public static class BuildEntry extends BuildStreamTreeEntry {

        transient Run run;
        private final String jobName;
        private final int buildNumber;

        public Run getRun() {
            if (run == null) {
                this.run = ciService.getBuildByNameAndNumber(jobName, buildNumber);
            }
            return run;
        }

        public BuildEntry(Run run) {
            this(run, new JenkinsCiService());
        }

        public BuildEntry(Run run, CiService ciService) {
            this.run = run;
            this.ciService = ciService;
            this.jobName = this.run.getParent().getFullDisplayName();
            this.buildNumber = this.run.getNumber();
        }

        @Override
        public String toString() {
            return "BuildEntry{" +
                    "jobName='" + jobName + '\'' +
                    ", buildNumber=" + buildNumber +
                    '}';
        }

        public String getJobName() {
            return jobName;
        }

        public int getBuildNumber() {
            return buildNumber;
        }

        public int compareTo(BuildStreamTreeEntry other) {
            if (!(other instanceof BuildEntry)) {
                return 0;
            }

            Run thisRun = this.getRun();
            Run otherRun = ((BuildEntry) (other)).getRun();
            if (thisRun == null || otherRun == null) {
                Log.warning("Tried to compare BuildStreamTreeEntry objects but at least one is null " +
                        "thisRun: " + thisRun + "otherRun: " + otherRun);
                return 0;
            }

            long buildEntry1StartTime = thisRun.getStartTimeInMillis();
            long buildEntry2StartTime = otherRun.getStartTimeInMillis();
            if (buildEntry1StartTime < buildEntry2StartTime) {
                return -1;
            } else if (buildEntry1StartTime == buildEntry2StartTime) {
                //if start time and job name are the same, put the build with the lower number first
                Job thisRunParent = thisRun.getParent();
                Job otherRunParent = otherRun.getParent();
                if (thisRunParent == null || otherRunParent == null) {
                    Log.warning("Tried to compare BuildStreamTreeEntry parent objects but at least one is null " +
                            "thisRun: " + thisRunParent + "otherRun: " + otherRunParent);
                    return 0;
                }

                if (thisRunParent.getDisplayName().equals(otherRunParent.getDisplayName())) {
                    return Integer.compare(this.getBuildNumber(), ((BuildEntry) (other)).getBuildNumber());
                }
                return 0;
            } else {
                return 1;
            }
        }
    }

    public static class JobEntry extends BuildStreamTreeEntry {

        transient Job job;
        private final String jobName;

        public Job getJob() {
            if (job == null) {
                job = ciService.getJobByName(jobName);
            }
            return job;
        }

        public JobEntry(Job job) {

            this(job, new JenkinsCiService());
        }

        public JobEntry(Job job, CiService ciService) {
            this.ciService = ciService;
            this.job = job;
            this.jobName = job.getFullDisplayName();
        }

        public String getJobName() {
            return jobName;
        }

        @Override
        public String toString() {
            return "JobEntry{" +
                    "jobName='" + jobName + '\'' +
                    '}';
        }

        public int compareTo(BuildStreamTreeEntry o) {
            return 0;
        }
    }

    public static class StringEntry extends BuildStreamTreeEntry {

        String string;

        public String getString() {
            return string;
        }

        public StringEntry(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return "StringEntry{" +
                    string +
                    '}';
        }

        public int compareTo(BuildStreamTreeEntry o) {
            return 0;
        }
    }

}


