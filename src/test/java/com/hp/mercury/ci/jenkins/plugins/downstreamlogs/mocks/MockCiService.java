package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.mocks;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiJob;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiRun;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiService;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.JenkinsCiRun;
import hudson.model.AbstractBuild;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.mockito.Mockito;

import java.util.GregorianCalendar;

/**
 * Created by kleintid on 3/23/2015.
 */
public class MockCiService implements CiService {


    @Override
    public Jenkins getCiInstance() {
        return null;
    }

    public CiJob getJobByName(String itemName) {

        ItemGroup parent = Mockito.mock(ItemGroup.class);
        Mockito.when(parent.getFullDisplayName()).thenReturn("");

        CiJob job = Mockito.mock(CiJob.class);
        Mockito.when(job.getParent()).thenReturn(parent);
        Mockito.when(parent.getDisplayName()).thenReturn("");
        Mockito.when(job.getFullDisplayName()).thenReturn(itemName);
        return job;
    }


    @Override
    public CiRun getBuildByNameAndNumber(String itemName, int buildNumber) {

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
        return new JenkinsCiRun(build);
    }


}
