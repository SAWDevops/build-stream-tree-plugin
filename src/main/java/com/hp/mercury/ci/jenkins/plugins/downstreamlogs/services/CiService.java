package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import hudson.model.Run;

/**
 * Created by kleintid on 3/23/2015.
 */
public interface CiService {

    public jenkins.model.Jenkins getCiInstance();

    public hudson.model.Job getJobByName(String itemName);
    public Run getBuildByNameAndNumber(String itemName, Integer buildNumber);
}
