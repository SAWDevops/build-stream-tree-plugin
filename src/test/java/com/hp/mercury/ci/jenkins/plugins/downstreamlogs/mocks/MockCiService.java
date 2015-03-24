package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.mocks;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiService;
import hudson.model.AbstractBuild;
import hudson.model.ItemGroup;
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

    private Job getJobByName(String itemName) {
        Job job = Mockito.mock(Job.class);
        ItemGroup itemGroup = Mockito.mock(ItemGroup.class);
        Mockito.when(job.getParent()).thenReturn(itemGroup);
        Mockito.when(job.getFullDisplayName()).thenReturn(itemName);
        return job;
    }

    @Override
    public Run getBuildByNameAndNumber(String itemName, int buildNumber) {

        ItemGroup itemGroup = Mockito.mock(ItemGroup.class);
        Mockito.when(itemGroup.getFullDisplayName()).thenReturn("");

        Job parent = Mockito.mock(Job.class);
        Mockito.when(parent.getParent()).thenReturn(itemGroup);
        Mockito.when(parent.getDisplayName()).thenReturn("");
        Mockito.when(parent.getFullDisplayName()).thenReturn(itemName);

        AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getNumber()).thenReturn(buildNumber);
        Mockito.when(build.getParent()).thenReturn(parent);
        Mockito.when(build.getParent().getFullDisplayName()).thenReturn(itemName);
        return build;
    }


}
