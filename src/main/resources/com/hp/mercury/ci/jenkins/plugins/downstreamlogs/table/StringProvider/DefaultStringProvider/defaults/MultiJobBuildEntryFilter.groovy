package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.StringProvider.DefaultStringProvider.defaults

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.BuildStreamTreeEntry
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.behavior.BuildEntryFilter

public class MultiJobBuildEntryFilter implements BuildEntryFilter {

    @Override
    public Boolean display(BuildStreamTreeEntry.BuildEntry e) {
        return !e.run.details.parent.class.name.toLowerCase().contains("multijob")
    }

    @Override
    Boolean display(BuildStreamTreeEntry.JobEntry e) {
        return true
    }

    @Override
    Boolean display(BuildStreamTreeEntry.StringEntry e) {
        return true
    }
}