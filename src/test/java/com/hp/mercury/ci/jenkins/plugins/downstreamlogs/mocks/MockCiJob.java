package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.mocks;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiJob;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiRun;
import hudson.model.ItemGroup;
import hudson.model.JobProperty;

/**
 * Created by kleintid on 3/25/2015.
 */
public class MockCiJob implements CiJob {



    @Override
    public CiRun getBuildByNumber(int buildNumber) {
        return null;
    }

    @Override
    public ItemGroup getParent() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getFullDisplayName() {
        return null;
    }

    @Override
    public CiRun getLastBuild() {
        return null;
    }

    @Override
    public JobProperty getDownstreamLogsManualEmebedViaJobProperty() {
        return null;
    }
}
