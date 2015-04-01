package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import com.hp.commons.core.collection.CollectionUtils;
import com.hp.commons.core.criteria.Criteria;
import com.hp.commons.core.criteria.InstanceOfCriteria;
import com.hp.commons.core.handler.Handler;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DisplayDetails;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DownstreamLogsCacheAction;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DownstreamLogsUtils;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.Log;
import hudson.model.Cause;
import hudson.model.ParametersAction;
import hudson.model.Run;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Created by kleintid on 3/25/2015.
 */
public class JenkinsCiRun implements CiRun{

    private Run run;

    public JenkinsCiRun(Run run) {
        this.run = run;
    }


    @Override
    public long getStartTimeInMillis() {
        if (run != null) {
            return run.getStartTimeInMillis();
        }
        Log.warning("Tried to get start time from a non existing Run object, returning -1");
        return -1l;
    }

    @Override
    public CiJob getParent() {
        if (run != null) {
            return new JenkinsCiJob(run.getParent());
        } else {
            Log.warning("Tried to get run's parent from a null run, returning null");
            return null;
        }
    }

    @Override
    public int getNumber() {
        if (run != null) {
            return run.getNumber();
        } else {
            Log.warning("Tried to get run's build number from a null run, returning -1");
            return -1;
        }
    }

    @Override
    public List<Cause> getCauses() {
        List<Cause> causes = new ArrayList<Cause>(0);
        if (run != null) {
            return run.getCauses();
        } else {
            Log.warning("Tried to get run's Causes from a null run, returning null");
            return causes;
        }
    }

    @Override
    public CiRun getPreviousBuild() {
        if (run != null) {
            return new JenkinsCiRun(run.getPreviousBuild());
        } else {
            Log.warning("Tried to get previous build by from a null run, returning null");
            return null;
        }
    }

    @Override
    public Reader getLogReader() {
        if (run != null) {
            try {
                return run.getLogReader();
            } catch (IOException e) {
                Log.warning("Tried to get log reader, but failed due to IOException.");
                e.printStackTrace();
            }
        } else {
            Log.warning("Tried to get previous build by from a null run, returning null");
        }
        return null;
    }

    @Override
    public boolean isLogUpdated() {
        if (run != null) {
            return run.isLogUpdated();
        }
        Log.warning("Tried to check of log update of a null run, returning null");
        return false;
    }

    @Override
    public DownstreamLogsCacheAction getAction() {
        if (run != null) {
            return run.getAction(DownstreamLogsCacheAction.class);
        }
        Log.warning("Tried to get DownstreamLogsCacheAction from a null run, returning null");
        return null;
    }

    @Override
    public void addAction(DownstreamLogsCacheAction action) {

    }

    @Override
    public void save() throws IOException {
        if (run != null) {
            run.save();
        }
    }

    @Override
    public Cause.UpstreamCause getUpstreamCause() {
        if(run!=null){
            return (Cause.UpstreamCause)run.getCause(Cause.UpstreamCause.class);
        }
        Log.warning("Tried to get UpstreamCause from a null run, returning null");
        return null;
    }

    public DisplayDetails getDetails() {
        return new DisplayDetails(run);
    }

    public boolean isBuilding(){
        if (run != null) {
            return run.isBuilding();
        }
        Log.warning("Tried to check is building a null run, returning null");
        return false;
    }

    public Run getRun() {
        return run;
    }

    @Override
    public boolean equals(Object o){
        if(run!=null){
            if(o instanceof Run){
                return run.equals((Run)o);
            }
            else if(o instanceof JenkinsCiRun){
                return run.equals(((JenkinsCiRun)o).getRun());
            }
        }
        return false;
    }

    @Override
    public boolean isUpstream(CiRun upstream, CiRun downstream) {
            for (Cause.UpstreamCause uc : getUpstreamCauses(downstream)) {
                //TODO: Refactor - remove casting when replacing Cause
                if (uc.getUpstreamRun().equals(((JenkinsCiRun) upstream).getRun())) {
                    return true;
                }
            }
            return false;
        }

    private static List<Cause.UpstreamCause> getUpstreamCauses(CiRun run) {

        //the newly allocated list has causes, but we filter all instances in it to be upstreamcauses,
        //so de-facto is is an upstreamcause list, but this won't work with java's typing.
        //see http://stackoverflow.com/questions/933447/how-do-you-cast-a-list-of-objects-from-one-type-to-another-in-java
        List<?> temp = new ArrayList<Cause>((run.getCauses()));
        CollectionUtils.filter(temp, new InstanceOfCriteria(Cause.UpstreamCause.class));
        return (List<Cause.UpstreamCause>) temp;
    }

    @Override
    public Collection<CiRun> getRoots(final CiRun build) {
        //TODO: Refactor - change build to CiRun
        Collection<Cause.UpstreamCause> upstreamCauses = getUpstreamCauses(build);

        //take runs from causes
        final List<CiRun> upstreamRuns = CollectionUtils.map(upstreamCauses, new Handler<CiRun, Cause.UpstreamCause>() {

            public CiRun apply(Cause.UpstreamCause upstreamCause) {
                //TODO: Refactor - Wrap Upstream cause and return CiRun
                return new JenkinsCiRun(upstreamCause.getUpstreamRun());
            }
        });

        //if someone uses the "rebuild" plugin the upstream build can belong to a different build stream, so we validate that it really references our build...
        CollectionUtils.filter(upstreamRuns, new Criteria<CiRun>() {
            public boolean isSuccessful(CiRun ciRun) {
                //check for null, maybe that build has been removed...?
                return ciRun != null && DownstreamLogsUtils.isDownstream(ciRun, build);
            }
        });

        //if we have no upstreams, we're the root
        if (upstreamRuns.isEmpty()) {
            return Collections.singletonList(build);
        }

        //if we have upstreams, we need to find their roots recursively, and return the aggregation of the results: all the combined roots
        //of all the upstream jobs.
        else {
            Collection<CiRun> upstreamRoots = new HashSet<CiRun>();
            for (CiRun upstreamRun : upstreamRuns) {
                upstreamRoots.addAll(getRoots(upstreamRun));
            }
            return upstreamRoots;
        }
    }


}
