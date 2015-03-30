package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DisplayDetails;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DownstreamLogsCacheAction;
import hudson.model.*;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Created by kleintid on 3/25/2015.
 */
public interface CiRun {

    public long getStartTimeInMillis();

    public CiJob getParent();
    public int getNumber();

    public List<Cause> getCauses();

    public CiRun getPreviousBuild();

    public Reader getLogReader();

    public boolean isLogUpdated();

    //TODO: refactor - should it be that specific action?
    public DownstreamLogsCacheAction getAction();

    public void addAction(DownstreamLogsCacheAction action);

    public void save() throws IOException;

    public Cause.UpstreamCause getUpstreamCause();

    public DisplayDetails getDetails();

    public boolean isBuilding();


}
