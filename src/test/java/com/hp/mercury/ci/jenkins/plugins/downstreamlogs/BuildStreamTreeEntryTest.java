package com.hp.mercury.ci.jenkins.plugins.downstreamlogs;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.mocks.MockCiService;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiService;
import junit.framework.TestCase;

import static org.junit.Assert.*;

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

}