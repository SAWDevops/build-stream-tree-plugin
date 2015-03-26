package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DownstreamLogsManualEmebedViaJobProperty;
import hudson.model.ItemGroup;
import hudson.model.JobProperty;

/**
 * Created by kleintid on 3/25/2015.
 */
public interface CiJob {

    public CiRun getBuildByNumber(int buildNumber);

    public ItemGroup getParent();

    public String getDisplayName();

    public String getFullDisplayName();

    public CiRun getLastBuild();

    //TODO: refactor - change to a generic method
    public JobProperty getDownstreamLogsManualEmebedViaJobProperty();




}
