package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.mocks;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiJob;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiRun;
import org.mockito.Mockito;

/**
 * Created by kleintid on 3/25/2015.
 */
public class MockCiRun implements CiRun {

    private long startTimeInMillis;

    public MockCiRun(long startTimeInMillis){
        this.startTimeInMillis = startTimeInMillis;
    }

    @Override
    public long getStartTimeInMillis() {
        return 0;
    }

    @Override
    public CiJob getParent() {
        return null;
    }

    @Override
    public int getNumber() {
        return 0;
    }

    public void setStartTimeInMillis(long startTimeInMillis) {
        this.startTimeInMillis = startTimeInMillis;
    }
}
