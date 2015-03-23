package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import jenkins.model.Jenkins;

/**
 * Created by kleintid on 3/23/2015.
 */
public class JenkinsCiService implements CiService {

    @Override
    public <T> Jenkins getCiInstance() {
        return Jenkins.getInstance();
    }
}
