package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import hudson.model.ItemGroup;

/**
 * Created by kleintid on 3/25/2015.
 */
public interface CiJob {

    public CiRun getBuildByNumber(int buildNumber);

    public ItemGroup getParent();

    public String getDisplayName();

    public String getFullDisplayName();

}
