package com.hp.mercury.ci.jenkins.plugins.downstreamlogs;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.mocks.MockCiRun;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.mocks.MockCiService;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiRun;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiService;
import org.junit.Test;

import static org.junit.Assert.*;

public class DownstreamLogsUtilsTest {


    CiService ciService = new MockCiService();

    @Test
    public void testIsDownstream() throws Exception {

        CiRun ciRunParent = new MockCiRun(1l);
        CiRun ciRunChild = new MockCiRun(1l);
        assertFalse(DownstreamLogsUtils.isDownstream(ciRunParent, ciRunChild));

    }
}