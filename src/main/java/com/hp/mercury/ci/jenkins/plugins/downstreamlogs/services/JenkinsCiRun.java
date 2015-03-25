package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.Log;
import hudson.model.Run;
import jenkins.model.Jenkins;

/**
 * Created by kleintid on 3/25/2015.
 */
public class JenkinsCiRun implements CiRun {

    private Run run;

    public JenkinsCiRun(Run run){
        this.run = run;
    }


    @Override
    public long getStartTimeInMillis() {
        if(run!=null){
            return run.getStartTimeInMillis();
        }
        Log.warning("Tried to get start time from a non existing Run object, returning -1");
        return -1l;
    }

    @Override
    public CiJob getParent() {
        if(run!=null){
            return new JenkinsCiJob(run.getParent());
        }
        else{
            Log.warning("Tried to get run's parent from a null job, returning null");
            return null;
        }
    }

    @Override
    public int getNumber() {
        if(run!=null){
            return run.getNumber();
        }
        else{
            Log.warning("Tried to get run's build number from a null job, returning -1");
            return -1;
        }
    }

    public Run getRun() {
        return run;
    }
}
