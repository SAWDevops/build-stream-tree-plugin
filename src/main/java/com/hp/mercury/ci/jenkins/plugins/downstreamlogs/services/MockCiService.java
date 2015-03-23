package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import hudson.model.Item;
import jenkins.model.Jenkins;

/**
 * Created by kleintid on 3/23/2015.
 */
public class MockCiService implements CiService {
    @Override
    public <T> Jenkins getCiInstance() {
        return null;
    }

    @Override
    public <T> Item getItemByFullName(String itemName, Class clazz) {
        return null;
    }
}
