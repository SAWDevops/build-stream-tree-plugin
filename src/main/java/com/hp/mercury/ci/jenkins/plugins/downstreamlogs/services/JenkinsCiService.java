package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.Log;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;

/**
 * Created by kleintid on 3/23/2015.
 */
public class JenkinsCiService implements CiService {

    @Override
    public Jenkins getCiInstance() {
        return Jenkins.getInstance();
    }

    public Job getJobByName(String itemName) {
        return getCiInstance().getItemByFullName(itemName, Job.class);
    }

    @Override
    public long getBuildStartTimeInMillis(Run run) {
        if(run!=null){
            return run.getStartTimeInMillis();
        }
        Log.warning("Tried to get start time from a non existing Run object, returning -1");
        return -1l;
    }

    @Override
    public Run getBuildByNameAndNumber(String itemName, int buildNumber) {
        Job job = this.getJobByName(itemName);
        if(job!=null){
            return job.getBuildByNumber(buildNumber);
        }
        else{
            Log.warning("Job object is null, cannot return Build");
            return null;
        }
    }
}
