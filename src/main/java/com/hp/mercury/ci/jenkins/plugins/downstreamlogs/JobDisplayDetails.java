package com.hp.mercury.ci.jenkins.plugins.downstreamlogs;

import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;

/**
 * Created by kleintid on 3/30/2015.
 */
public class JobDisplayDetails {


    private Run lastSuccessfulBuild;
    private ItemGroup<? extends Item> parent;
    private String url;

    public JobDisplayDetails(Job job){
        this.lastSuccessfulBuild = job.getLastSuccessfulBuild();
        this.parent = job.getParent();
        this.url = job.getUrl();
    }

    public Run getLastSuccessfulBuild() {
        return lastSuccessfulBuild;
    }

    public ItemGroup<? extends Item> getParent() {
        return parent;
    }

    public String getUrl() {
        return url;
    }
}
