package com.hp.mercury.ci.jenkins.plugins.downstreamlogs;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.mocks.MockCiService;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiJob;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiRun;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiService;
import hudson.model.Build;
import hudson.model.Job;
import hudson.model.Run;
import junit.framework.TestCase;
import org.mockito.Mockito;

import java.util.GregorianCalendar;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

public class BuildStreamTreeEntryTest extends TestCase {

    CiService ciService = new MockCiService();

    public void testGetJobName(){
        String testJobName = "myJob";
        int buildNumber = 1;

        //1. legal job name and build number
        BuildStreamTreeEntry.BuildEntry buildEntry =
                new BuildStreamTreeEntry.BuildEntry(ciService.getBuildByNameAndNumber(testJobName, buildNumber));
        assert testJobName.equals(buildEntry.getJobName());

        //2. null job name and legal build number
        buildEntry =
                new BuildStreamTreeEntry.BuildEntry(ciService.getBuildByNameAndNumber(null, buildNumber));
        assert null == buildEntry.getJobName();

        //3. legal job name and negative build number
        buildEntry =
                new BuildStreamTreeEntry.BuildEntry(ciService.getBuildByNameAndNumber(testJobName, -1));
        assert testJobName.equals(buildEntry.getJobName());
    }

    public void testGetBuildNumber(){
        String testJobName = "myJob";
        int buildNumber = 1;

        //1. legal job name and build number
        BuildStreamTreeEntry.BuildEntry buildEntry =
                new BuildStreamTreeEntry.BuildEntry(ciService.getBuildByNameAndNumber(testJobName, buildNumber));
        assert buildNumber == buildEntry.getBuildNumber();

        //2. null job name and legal build number
        buildEntry =
                new BuildStreamTreeEntry.BuildEntry(ciService.getBuildByNameAndNumber(null, buildNumber));
        assert buildNumber == buildEntry.getBuildNumber();

        //3. legal job name and negative build number
        buildNumber = -1;
        buildEntry =
                new BuildStreamTreeEntry.BuildEntry(ciService.getBuildByNameAndNumber(testJobName, -1));
        assert buildNumber == buildEntry.getBuildNumber();
    }

    public void testGetJob() {
        String testJobName = "myJob";
        CiJob testJob = ciService.getJobByName(testJobName);

        //1. legal job name and build number
        BuildStreamTreeEntry.JobEntry jobEntry =
                new BuildStreamTreeEntry.JobEntry(testJob, ciService);
        assert testJob.equals(jobEntry.getJob());
    }


    public void testCompareTo(){
        String testJobName = "myJob";
        int firstBuildNumber = 1;
        int secondBuildNumber = 2;
        long firstStartTime = 123456789;
        long secondStartTime = 123456790;

        CiRun firstRun = ciService.getBuildByNameAndNumber(testJobName, firstBuildNumber);
        CiRun secondRun = ciService.getBuildByNameAndNumber(testJobName, secondBuildNumber);

        assert firstRun.getStartTimeInMillis() == 0;

        //1. 2 builds of the same job with same start time --> -1
        BuildStreamTreeEntry.BuildEntry firstBuildEntry =
                new BuildStreamTreeEntry.BuildEntry(firstRun);

        BuildStreamTreeEntry.BuildEntry secondBuildEntry =
                new BuildStreamTreeEntry.BuildEntry(secondRun);

        assert firstBuildEntry.compareTo(secondBuildEntry) == -1;



        /*//TODO: those mocks fails since that method cannot be stubbed, need to figure out how to mock
        //the get start time
        Mockito.when(ciService.getBuildStartTimeInMillis(firstRun)).thenReturn(firstStartTime);
        Mockito.when(ciService.getBuildStartTimeInMillis(secondRun)).thenReturn(secondStartTime);
        */
    }
}