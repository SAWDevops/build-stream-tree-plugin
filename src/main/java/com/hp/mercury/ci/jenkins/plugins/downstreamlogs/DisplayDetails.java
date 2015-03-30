package com.hp.mercury.ci.jenkins.plugins.downstreamlogs;

import hudson.model.*;

import java.util.List;

/**
 * Created by kleintid on 3/28/2015.
 */
public class DisplayDetails {

    private String fullName;
    private int number;
    private String url;
    private Job parent;
    private List<BuildBadgeAction> badgeActions;
    private Result result;
    private List<ParametersAction> parameterActions;
    private Run.Summary buildStatusSummary;
    private String timestampString;
    private long timeInMillis;


    public DisplayDetails(Run run){
        if(run!=null){
            this.fullName = run.getFullDisplayName();
            this.number = run.getNumber();
            this.url = run.getUrl();
            this.parent = run.getParent();
            this.badgeActions = run.getBadgeActions();
            this.result = run.getResult();
            this.parameterActions = run.getActions(ParametersAction.class);
            this.buildStatusSummary = run.getBuildStatusSummary();
            this.timestampString = run.getTimestampString();
            this.timeInMillis = run.getTimeInMillis();
        }
    }

    public String getFullName() {
        return fullName;
    }

    public int getNumber() {
        return number;
    }

    public String getUrl() {
        return url;
    }

    public Job getParent() {
        return parent;
    }

    public List<BuildBadgeAction> getBadgeActions() {
        return badgeActions;
    }

    public Result getResult() {
        return result;
    }

    public List<ParametersAction> getParameterActions() {
        return parameterActions;
    }

    public Run.Summary getBuildStatusSummary() {
        return buildStatusSummary;
    }

    public String getTimestampString() {
        return timestampString;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }
}
