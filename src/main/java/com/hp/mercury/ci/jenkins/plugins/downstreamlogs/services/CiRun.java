package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import hudson.model.ItemGroup;

/**
 * Created by kleintid on 3/25/2015.
 */
public interface CiRun {

    public long getStartTimeInMillis();

    public CiJob getParent();
    public int getNumber();
}
