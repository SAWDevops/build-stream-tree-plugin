package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services;

import com.hp.commons.core.collection.CollectionUtils;
import com.hp.commons.core.criteria.InstanceOfCriteria;
import com.hp.commons.core.handler.Handler;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DisplayDetails;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DownstreamLogsCacheAction;
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.Log;
import hudson.model.Cause;
import hudson.model.Run;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by kleintid on 3/25/2015.
 */
public class JenkinsCiRun implements CiRun {

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
        if (run != null) {
            return (Cause.UpstreamCause) run.getCause(Cause.UpstreamCause.class);
        }
        Log.warning("Tried to get UpstreamCause from a null run, returning null");
        return null;
    }

    @Override
    public List<CiRun> getUpstreamRuns(final CiRun build) {

        Collection<Cause.UpstreamCause> upstreamCauses = getUpstreamCauses(build);

        //take runs from causes
        final List<CiRun> upstreamRuns = CollectionUtils.map(upstreamCauses, new Handler<CiRun, Cause.UpstreamCause>() {

            public CiRun apply(Cause.UpstreamCause upstreamCause) {
                return new JenkinsCiRun(upstreamCause.getUpstreamRun());
            }
        });
    }

    public boolean isUpstream(CiRun downstream) {
        for (Cause.UpstreamCause uc : getUpstreamCauses(downstream)) {
            //TODO: Refactor - remove casting when replacing Cause
            if (uc.getUpstreamRun().equals(this.getRun())) {
                return true;
            }
        }
        return false;
    }

    public CiRun isStartByBuild(CiRun buildToReference, Integer projectDownstreamExecutionIndexOnBuild) {
        for (Cause.UpstreamCause upstreamCause : getUpstreamCauses(this)) {
            //TODO: Refactor - remove this casting when replacing Cause
            if (((JenkinsCiRun) buildToReference).getRun().equals(upstreamCause.getUpstreamRun())) {

                Log.debug(buildToReference.toString() + " is upstream of " + this + " according to " +
                        upstreamCause.getClass().getSimpleName() + ", " + upstreamCause.getUpstreamProject() + ", " +
                        upstreamCause.getUpstreamBuild());

                if (--projectDownstreamExecutionIndexOnBuild < 0) {

                    Log.debug(this + " is the build that was triggered by " + buildToReference + " " +
                            "on this parsed line");
                    return this;
                } else {
                    Log.debug("skipping " + this + " because it's the " + projectDownstreamExecutionIndexOnBuild + "'th execution of the build," +
                            "which was triggered in a previous step...");

                }
            }
        }
    }

    private static List<Cause.UpstreamCause> getUpstreamCauses(CiRun run) {

        //the newly allocated list has causes, but we filter all instances in it to be upstreamcauses,
        //so de-facto is is an upstreamcause list, but this won't work with java's typing.
        //see http://stackoverflow.com/questions/933447/how-do-you-cast-a-list-of-objects-from-one-type-to-another-in-java
        List<?> temp = new ArrayList<Cause>(run.getCauses());
        CollectionUtils.filter(temp, new InstanceOfCriteria(Cause.UpstreamCause.class));
        return (List<Cause.UpstreamCause>) temp;
    }

    public List<CiRun> getUpstreamCiRunCauses(){
        ArrayList<CiRun> ciRuns = new ArrayList<CiRun>(1);

        for (Cause.UpstreamCause upstreamCause : getUpstreamCauses(this)){
            ciRuns.add(new JenkinsCiRun(upstreamCause.getUpstreamRun()));
        }

        return ciRuns;
    }

    public String getUpstreamProject() {

        return this.getUpstreamCause().getUpstreamProject();
    }

    public int getUpstreamBuild() {
        return this.getUpstreamCause().getUpstreamBuild();
    }


    public DisplayDetails getDetails() {
        return new DisplayDetails(run);
    }

    public boolean isBuilding() {
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
    public boolean equals(Object o) {
        if (run != null) {
            if (o instanceof Run) {
                return run.equals((Run) o);
            } else if (o instanceof JenkinsCiRun) {
                return run.equals(((JenkinsCiRun) o).getRun());
            }
        }
        return false;
    }
}
