package com.hp.mercury.ci.jenkins.plugins.downstreamlogs;

import hudson.model.*;

import java.util.Calendar;
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
    private Calendar timestamp;
    private long timeInMillis;
    private BallColor iconColor;
    private long duration;
    private String durationString;
    private long estimatedDuration;



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
            this.timestamp = run.getTimestamp();
            this.timeInMillis = run.getTimeInMillis();
            this.iconColor = run.getIconColor();
            this.duration = run.getDuration();
            this.durationString = run.getDurationString();
            this.estimatedDuration = run.getEstimatedDuration();
        }
        else {
            throw new IllegalStateException("Cannot create display details from a null Run object");
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

    public Calendar getTimestamp() {
        return timestamp;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public BallColor getIconColor() {
        return iconColor;
    }

    public long getDuration() {
        return duration;
    }

    public String getDurationString() {
        return durationString;
    }

    public long getEstimatedDuration() {
        return estimatedDuration;
    }
}
