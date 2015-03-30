package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DisplayDetails;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DownstreamLogsManualEmebedViaJobProperty;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.JobDisplayDetails;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.Log;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.JobProperty;

/**
 * Created by kleintid on 3/25/2015.
 */
public class JenkinsCiJob implements CiJob {

    private Job job;

    public JenkinsCiJob(Job job){
        this.job = job;
    }

    @Override
    public CiRun getBuildByNumber(int buildNumber) {
        if(job!=null){
            return new JenkinsCiRun(job.getBuildByNumber(buildNumber));
        }
        else{
            Log.warning("Tried to get build by number from a null job, returning null");
            return null;
        }
    }

    @Override
    public ItemGroup getParent() {
        if(job!=null){
            return job.getParent();
        }
        else{
            Log.warning("Tried to get job's parent from a null job, returning null");
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        if(job!=null){
            return job.getDisplayName();
        }
        else{
            Log.warning("Tried to get job's display name from a null job, returning empty string");
            return "";
        }
    }

    @Override
    public String getFullDisplayName() {
        if(job!=null){
            return job.getFullDisplayName();
        }
        else{
            Log.warning("Tried to get job's full display name from a null job, returning empty string");
            return "";
        }
    }

    @Override
    public CiRun getLastBuild() {
        if(job!=null){
            return new JenkinsCiRun(job.getLastBuild());
        }
        else{
            Log.warning("Tried to get last build from a null job, returning null");
            return null;
        }
    }

    @Override
    public JobProperty getDownstreamLogsManualEmebedViaJobProperty(){
        if(job!=null){
            return job.getProperty(DownstreamLogsManualEmebedViaJobProperty.class);
        }
        Log.warning("Tried to get job property from a null job, returning null");
        return null;
    }

    @Override
    public JobDisplayDetails getDetails() {
        return new JobDisplayDetails(job);
    }
}
