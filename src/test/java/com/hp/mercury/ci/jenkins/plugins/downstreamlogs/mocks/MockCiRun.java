package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.mocks;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DisplayDetails;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DownstreamLogsCacheAction;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiJob;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiRun;
import hudson.model.Cause;
import hudson.model.Run;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

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

    @Override
    public List<Cause> getCauses() {
        return null;
    }

    @Override
    public CiRun getPreviousBuild() {
        return null;
    }

    @Override
    public Reader getLogReader() {
        return null;
    }

    @Override
    public boolean isLogUpdated() {
        return false;
    }

    @Override
    public DownstreamLogsCacheAction getAction() {
        return null;
    }

    @Override
    public void addAction(DownstreamLogsCacheAction action) {

    }

    @Override
    public void save() throws IOException {

    }

    @Override
    public Cause.UpstreamCause getUpstreamCause() {
        return null;
    }

    @Override
    public DisplayDetails getDetails() {
        return null;
    }

    @Override
    public boolean isBuilding() {
        return false;
    }

    public List<CiRun> getUpstreamRuns(CiRun build) {
        return null;
    }

    public boolean isUpstream(CiRun downstream) {
        return false;
    }

    public List<CiRun> getUpstreamCiRunCauses() {
        return null;
    }

    public String getUpstreamProject() {
        return null;
    }

    public int getUpstreamBuild() {
        return 0;
    }

    public List<CiRun> getInternalRuns() {
        return null;
    }


}
