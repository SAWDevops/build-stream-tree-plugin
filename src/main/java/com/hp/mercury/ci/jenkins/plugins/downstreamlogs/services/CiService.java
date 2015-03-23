package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

/**
 * Created by kleintid on 3/23/2015.
 */
public interface CiService {

    public <T> jenkins.model.Jenkins getCiInstance();
}
