package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.mocks;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiService;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.mockito.Mockito;

/**
 * Created by kleintid on 3/23/2015.
 */
public class MockCiService implements CiService {

    @Override
    public Jenkins getCiInstance() {
        return null;
    }

    @Override
    public Job getJobByName(String itemName) {
        Job job = Mockito.mock(Job.class);
        Mockito.when(job.getDisplayName()).thenReturn(itemName);
        return job;
    }

    @Override
    public Run getBuildByNameAndNumber(String itemName, int buildNumber) {
        AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getNumber()).thenReturn(buildNumber);
        Mockito.when(build.getParent().getDisplayName()).thenReturn(itemName);
        return build;
    }
}
