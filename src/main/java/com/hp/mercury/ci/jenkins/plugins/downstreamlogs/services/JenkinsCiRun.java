package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DisplayDetails;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DownstreamLogsCacheAction;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.Log;
import hudson.model.Cause;
import hudson.model.ParametersAction;
import hudson.model.Run;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kleintid on 3/25/2015.
 */
public class JenkinsCiRun implements CiRun{

    private Run run;

    public JenkinsCiRun(Run run) {
        this.run = run;
    }


    @Override
    public long getStartTimeInMillis() {
        if (run != null) {
            return run.getStartTimeInMillis();
        }
        Log.warning("Tried to get start time from a non existing Run object, returning -1");
        return -1l;
    }

    @Override
    public CiJob getParent() {
        if (run != null) {
            return new JenkinsCiJob(run.getParent());
        } else {
            Log.warning("Tried to get run's parent from a null run, returning null");
            return null;
        }
    }

    @Override
    public int getNumber() {
        if (run != null) {
            return run.getNumber();
        } else {
            Log.warning("Tried to get run's build number from a null run, returning -1");
            return -1;
        }
    }

    @Override
    public List<Cause> getCauses() {
        List<Cause> causes = new ArrayList<Cause>(0);
        if (run != null) {
            return run.getCauses();
        } else {
            Log.warning("Tried to get run's Causes from a null run, returning null");
            return causes;
        }
    }

    @Override
    public CiRun getPreviousBuild() {
        if (run != null) {
            return new JenkinsCiRun(run.getPreviousBuild());
        } else {
            Log.warning("Tried to get previous build by from a null run, returning null");
            return null;
        }
    }

    @Override
    public Reader getLogReader() {
        if (run != null) {
            try {
                return run.getLogReader();
            } catch (IOException e) {
                Log.warning("Tried to get log reader, but failed due to IOException.");
                e.printStackTrace();
            }
        } else {
            Log.warning("Tried to get previous build by from a null run, returning null");
        }
        return null;
    }

    @Override
    public boolean isLogUpdated() {
        if (run != null) {
            return run.isLogUpdated();
        }
        Log.warning("Tried to check of log update of a null run, returning null");
        return false;
    }

    @Override
    public DownstreamLogsCacheAction getAction() {
        if (run != null) {
            return run.getAction(DownstreamLogsCacheAction.class);
        }
        Log.warning("Tried to get DownstreamLogsCacheAction from a null run, returning null");
        return null;
    }

    @Override
    public void addAction(DownstreamLogsCacheAction action) {

    }

    @Override
    public void save() throws IOException {
        if (run != null) {
            run.save();
        }
    }

    @Override
    public Cause.UpstreamCause getUpstreamCause() {
        if(run!=null){
            return (Cause.UpstreamCause)run.getCause(Cause.UpstreamCause.class);
        }
        Log.warning("Tried to get UpstreamCause from a null run, returning null");
        return null;
    }

    public DisplayDetails getDetails() {
        return new DisplayDetails(run);
    }

    public boolean isBuilding(){
        if (run != null) {
            return run.isBuilding();
        }
        Log.warning("Tried to check is building a null run, returning null");
        return false;
    }

    public Run getRun() {
        return run;
    }
}
