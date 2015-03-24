package com.hp.mercury.ci.jenkins.plugins.downstreamlogs;

import com.hp.commons.core.collection.CollectionUtils;
import com.hp.commons.core.criteria.Criteria;
import com.hp.commons.core.criteria.InstanceOfCriteria;
import com.hp.commons.core.handler.Handler;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import hudson.console.ConsoleNote;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.apache.commons.jelly.XMLOutput;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;


/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 17/06/13
 * Time: 15:09
 * To change this template use File | Settings | File Templates.
 */
public class DownstreamLogsUtils {

    //UTILITY CLASS
    private DownstreamLogsUtils() {
    }

    public static String collectStringFromGroovyExecution(Class scriptClass, Binding binding) {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        final XMLOutput xmlOutput;
        try {
            xmlOutput = XMLOutput.createXMLOutput(output);

            binding.setVariable("output", xmlOutput);

            InvokerHelper.createScript(scriptClass, binding).run();

            xmlOutput.flush();
            xmlOutput.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String ret = new String(output.toByteArray());

        return ret;
    }

    private static boolean isRefreshModeEnabled() {
        return false;
    }

    public static Class groovySourceToScript(ClassLoader classLoader, String path) {

        Class ret = isRefreshModeEnabled() ? null : compiledGroovyCache.get(path);
        if (ret == null) {

            CompilerConfiguration cc = new CompilerConfiguration();

            cc.setRecompileGroovySource(true);

            final GroovyClassLoader gcl = new GroovyClassLoader(classLoader, cc);

            gcl.clearCache();

            final GroovyCodeSource codeSource;
            try {
                codeSource = new GroovyCodeSource(classLoader.getResource(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ret = gcl.parseClass(codeSource);
            compiledGroovyCache.put(path, ret);
        }

        return ret;
    }

    private static Map<String, Class> compiledGroovyCache = new HashMap<String, Class>();

    public static Collection<BuildStreamTreeEntry> getDownstreamRuns(Run run) {

        Log.debug("getting downstream runs of " + run.toString());

        Job parent = null;
        //write explicitly instead of using ternary for different lines to appear in debug/exceptions.
        final boolean isMatrixRun = run instanceof MatrixRun;
        if (isMatrixRun) {
            final MatrixRun matrixRun = (MatrixRun) run;
            final MatrixBuild parentBuild = matrixRun.getParentBuild();
            if (parentBuild != null) {
                parent = parentBuild.getParent();
            }
        } else {
            parent = run.getParent();
        }

        DownstreamLogsManualEmebedViaJobProperty property = (parent != null) ?
                (DownstreamLogsManualEmebedViaJobProperty) parent.getProperty(DownstreamLogsManualEmebedViaJobProperty.class) :
                null;

        boolean cache = ((property != null) && (property.getOverrideGlobalConfig())) ?
                (property.getCacheBuild()) :
                DownstreamLogsAction.getDescriptorStatically().getCacheBuilds();

        Log.debug("downstream of " + run + " should" + (cache ? " " : " not ") + "be retrieved from cache");

        return (cache) ?
                retrieveFromCache(run) :
                calculateDownstreamBuilds(run);
    }

    private static List<BuildStreamTreeEntry> retrieveFromCache(Run run) {

        Log.debug("retrieving " + run + " from cache");

        List<BuildStreamTreeEntry> triggered = null;

        //don't cache while build is running...
        if (run.isLogUpdated()) {
            Log.debug("run is still being updated, not using cache.");
            return calculateDownstreamBuilds(run);
        }

        DownstreamLogsCacheAction cache = run.getAction(DownstreamLogsCacheAction.class);
        if (cache == null) {
            //the value inside cache will be set in a moment because triggered = null;
            Log.debug("initializing cache in " + run);
            cache = new DownstreamLogsCacheAction(null);
            run.addAction(cache);
        }
        //only use cache if regexes haven't changed - if there are new ones we need to recalculate...
        else if (cache.getParserConfigs().equals(DownstreamLogsAction.getDescriptorStatically().getParserConfigs())) {

            Log.debug("checking if cache is valid for " + run + " with entries " + cache.getCachedEntries());
            //when updating entries its possible for a buildentry to become a string entry or somesuch, meaning we can't cache.
            //if that some entry won't be buildentry. so triggered will be null. so we'll reach the save condition...
            cache.updateEntries();
            //we only cache when we know the build, otherwise it might be that we're still building or waiting for something... a build in queue etc...
            if (cache.allEntriesGiveSpecificBuild()) {

                Log.debug("cache is valid for " + run + " with " + cache.getCachedEntries());
                triggered = new ArrayList<BuildStreamTreeEntry>(cache.getCachedEntries());
            }
        }

        if (triggered == null) {
            Log.debug("triggered jobs are uninstantiated in cache of " + run + ", calculating.");
            triggered = calculateDownstreamBuilds(run);
            cache.setCachedEntries(triggered);

            try {
                run.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return triggered;
    }

    private static List<BuildStreamTreeEntry> calculateDownstreamBuilds(final Run run) {

        Log.debug("calculating downstream builds of " + run);

        ArrayList<BuildStreamTreeEntry> triggered = new ArrayList<BuildStreamTreeEntry>();

        if (run instanceof MatrixBuild) {
            MatrixBuild mb = (MatrixBuild) run;
            Log.debug("run is a matrix build with exact runs " + mb.getExactRuns());
            for (Run internalMatrixRun : mb.getExactRuns()) {
                triggered.add(new BuildStreamTreeEntry.BuildEntry(internalMatrixRun));
            }
        }

        BuildExecutionByProjectCounter buildExecutionByProject = new BuildExecutionByProjectCounter();
        Reader reader;

        try {
            if (run != null && ((reader = run.getLogReader()) != null)) {

                BufferedReader bufferedReader = new BufferedReader(reader);

                for (String line = bufferedReader.readLine();
                     line != null;
                     line = bufferedReader.readLine()) {

                    //strip off jenkins specific encoding
                    line = ConsoleNote.removeNotes(line);

                    Collection<BuildStreamTreeEntry> referencingBuilds =
                            DownstreamLogsUtils.parseLineForTriggeredBuilds(line, run, buildExecutionByProject);

                    if (referencingBuilds != null) {

                        //sort referencingBuilds into new collection and addAll to triggered
                        triggered.addAll(referencingBuilds);
                        Collections.sort(triggered);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //sanitize: make sure all downstream that we've just found agree that our upstream is really their upstream.
        Log.debug("filtering out downstream builds that don't mark this build as their upstream...");
        CollectionUtils.filter(triggered, new Criteria<BuildStreamTreeEntry>() {
            public boolean isSuccessful(BuildStreamTreeEntry buildStreamTreeEntry) {
                if (buildStreamTreeEntry instanceof BuildStreamTreeEntry.BuildEntry) {
                    return isUpstream(run, ((BuildStreamTreeEntry.BuildEntry) buildStreamTreeEntry).getRun());
                }
                //we have no new way to verify, but we're optimistic, so we'll say: ok!
                else {
                    return true;
                }
            }
        });

        Log.debug("downstream builds of " + run + " are " + triggered);

        return triggered;
    }

    private static boolean isUpstream(Run upstream, Run downstream) {
        for (Cause.UpstreamCause uc : getUpstreamCauses(downstream)) {
            if (uc.getUpstreamRun().equals(upstream)) {
                return true;
            }
        }
        return false;
    }

    private static class BuildExecutionByProjectCounter {

        Map<String, Integer> perBuildProjectExecutionMap = new HashMap<String, Integer>();

        private String key(Run build, Job project) {
            return build.getParent().getFullDisplayName() + "#" + build.number + "/" + project.getFullDisplayName();

        }

        public Integer getProjectExecutionsSetFromUpstreamBuild(Run build, Job project) {
            //we need a linked hash set because we may insert the same build multiple times,
            // and we need to know the order of insertions later on
            final String key = key(build, project);
            Integer ret = perBuildProjectExecutionMap.get(key);

            return ret == null ? 0 : ret;
        }

        public void addProjectBuildToProjectExecutionsFromUpstreamBuildSet(Run build, Job project) {
            perBuildProjectExecutionMap.put(key(build, project), getProjectExecutionsSetFromUpstreamBuild(build, project) + 1);
        }
    }

    public static Run getRunStartedByThisRun(
            Job referencingProject,
            Run buildToReference,
            BuildExecutionByProjectCounter buildExecutionByProject) {

        Log.debug("searching for a build of " + referencingProject.getFullDisplayName() + " that was triggered by " +
                buildToReference);

        if (buildToReference.getParent() == referencingProject) {
            return buildToReference;
        } else {

            Integer projectDownstreamExecutionIndexOnBuild =
                    buildExecutionByProject.getProjectExecutionsSetFromUpstreamBuild(
                            buildToReference, referencingProject);

            /**
             * THIS IMPLEMENTATION IS REALLY BAD MEMORY PERFORMANCE WISE. IT LOOKS LIKE getBuilds() does
             * _getRuns.values() and that really looks like it's loading ALL OF PAST BUILDS into memory...
             */

//            final RunList reversedRunList = referencingProject.getBuilds().byTimestamp(
//                    buildToReference.getTimeInMillis(),
//                    System.currentTimeMillis());
//
//            //runlist should be treated as iterable and not as list according to jenkins docs
//            final Stack<Run> runList = new Stack();
//            final Iterator<Run> reversedIterator = reversedRunList.iterator();
//            while (reversedIterator.hasNext()) {
//                runList.push(reversedIterator.next());
//            }
//
//            while (!runList.isEmpty()) {

            long cutoffTime = buildToReference.getStartTimeInMillis();

            //true, it makes more sense to check the other way round starting from the earliest possible and continuing onwards.
            //the jenkins API for this however seems to have horrible memory implications. see above comment.
            for (Run referencingBuild = referencingProject.getLastBuild();
                 referencingBuild != null && referencingBuild.getStartTimeInMillis() >= cutoffTime;
                 referencingBuild = referencingBuild.getPreviousBuild()) {

                Log.debug("checking if " + referencingBuild + " was started by " + buildToReference);

                for (Cause.UpstreamCause upstreamCause : getUpstreamCauses(referencingBuild)) {

                    if (buildToReference.equals(upstreamCause.getUpstreamRun())) {

                        Log.debug(buildToReference.toString() + " is upstream of " + referencingBuild + " according to " +
                                upstreamCause.getClass().getSimpleName() + ", " + upstreamCause.getUpstreamProject() + ", " +
                                upstreamCause.getUpstreamBuild());

                        if (--projectDownstreamExecutionIndexOnBuild < 0) {

                            Log.debug(referencingBuild + " is the build that was triggered by " + buildToReference + " " +
                                    "on this parsed line");
                            return referencingBuild;
                        } else {
                            Log.debug("skipping " + referencingBuild + " because it's the " + projectDownstreamExecutionIndexOnBuild + "'th execution of the build," +
                                    "which was triggered in a previous step...");

                        }
                    }
                }

            }

        }

        return null;
    }

    private static List<Cause.UpstreamCause> getUpstreamCauses(Run run) {

        //the newly allocated list has causes, but we filter all instances in it to be upstreamcauses,
        //so de-facto is is an upstreamcause list, but this won't work with java's typing.
        //see http://stackoverflow.com/questions/933447/how-do-you-cast-a-list-of-objects-from-one-type-to-another-in-java
        List<?> temp = new ArrayList<Cause>(run.getCauses());
        CollectionUtils.filter(temp, new InstanceOfCriteria(Cause.UpstreamCause.class));
        return (List<Cause.UpstreamCause>) temp;
    }

    private static List<BuildStreamTreeEntry> fromProjectName(
            Matcher matcher,
            ProjectAndBuildRegexParserConfig config,
            Run currentBuild,
            BuildExecutionByProjectCounter buildExecutionByProject) {

        String[] projectNames = matcher.group(config.getProjectIndex()).split(",");

        List<BuildStreamTreeEntry> ret = new ArrayList();

        for (String projectName : projectNames) {

            projectName = projectName.trim();
            Job referencingProject = (Job) Jenkins.getInstance().getItemByFullName(projectName);
            if (referencingProject != null) {

                Run referencingBuild = getRunStartedByThisRun(referencingProject, currentBuild, buildExecutionByProject);

                if (referencingBuild != null) {
                    buildExecutionByProject.addProjectBuildToProjectExecutionsFromUpstreamBuildSet(
                            currentBuild,
                            referencingProject);

                    ret.add(new BuildStreamTreeEntry.BuildEntry(referencingBuild));
                } else {
                    ret.add(new BuildStreamTreeEntry.JobEntry(referencingProject));
                }
            } else {
                ret.add(new BuildStreamTreeEntry.StringEntry(projectName));
            }
        }

        return ret;
    }

    private static List<BuildStreamTreeEntry> fromProjectNameAndBuildNumber(
            Matcher matcher,
            ProjectAndBuildRegexParserConfig config,
            Run currentBuild,
            BuildExecutionByProjectCounter buildExecutionByProject) {

        String projectName = matcher.group(config.getProjectIndex());
        final String group = matcher.group(config.getBuildIndex());
        Integer buildNumber = null;
        try {
            buildNumber = Integer.parseInt(group);
        } catch (NumberFormatException nfe) {
            Log.warning("error when parsing int " + group + " obtained with " + config.getGroovyRegex());
        }

        if (projectName != null && buildNumber != null) {
            Job referencingProject = (Job) Jenkins.getInstance().getItemByFullName(projectName);
            if (referencingProject != null) {
                Run referencingBuild = referencingProject.getBuildByNumber(buildNumber);
                if (referencingBuild != null) {
                    buildExecutionByProject.addProjectBuildToProjectExecutionsFromUpstreamBuildSet(
                            currentBuild,
                            referencingProject);
                    return Collections.singletonList(
                            (BuildStreamTreeEntry) new BuildStreamTreeEntry.BuildEntry(referencingBuild));
                } else {
                    return Collections.singletonList(
                            (BuildStreamTreeEntry) new BuildStreamTreeEntry.JobEntry(referencingProject));
                }
            } else {
                return Collections.singletonList(
                        (BuildStreamTreeEntry) new BuildStreamTreeEntry.StringEntry(projectName + " " + buildNumber));
            }
        }

        return null;
    }

    private static List<BuildStreamTreeEntry> parseLineForTriggeredBuilds(
            String line,
            Run currentBuild,
            BuildExecutionByProjectCounter buildExecutionByProject) {

        for (ProjectAndBuildRegexParserConfig config : DownstreamLogsAction.getDescriptorStatically().getParserConfigs()) {
            Matcher matcher = config.getRegexPattern().matcher(line);
            if (matcher.matches()) {

                List<BuildStreamTreeEntry> parsedBuild;
                if (config.getRegexParseOrder() == ProjectAndBuildRegexParserConfig.RegexParseOrder.PROJECT_ONLY) {
                    parsedBuild = fromProjectName(matcher, config, currentBuild, buildExecutionByProject);
                } else {

                    parsedBuild = fromProjectNameAndBuildNumber(matcher, config, currentBuild, buildExecutionByProject);
                }

                return parsedBuild;
            }
        }

        return null;
    }


    public static Collection<Run> getRoots(final Run build) {

        Collection<Cause.UpstreamCause> upstreamCauses = getUpstreamCauses(build);

        //take runs from causes
        final List<Run> upstreamRuns = CollectionUtils.map(upstreamCauses, new Handler<Run, Cause.UpstreamCause>() {

            public Run apply(Cause.UpstreamCause upstreamCause) {
                return upstreamCause.getUpstreamRun();
            }
        });

        //if someone uses the "rebuild" plugin the upstream build can belong to a different build stream, so we validate that it really references our build...
        CollectionUtils.filter(upstreamRuns, new Criteria<Run>() {
            public boolean isSuccessful(Run run) {
                //check for null, maybe that build has been removed...?
                return run != null && isDownstream(run, build);
            }
        });

        //if we have no upstreams, we're the root
        if (upstreamRuns.isEmpty()) {
            return Collections.singletonList(build);
        }

        //if we have upstreams, we need to find their roots recursively, and return the aggregation of the results: all the combined roots
        //of all the upstream jobs.
        else {
            Collection<Run> upstreamRoots = new HashSet<Run>();
            for (Run upstreamRun : upstreamRuns) {
                upstreamRoots.addAll(getRoots(upstreamRun));
            }
            return upstreamRoots;
        }
    }

    public static boolean isDownstream(Run parent, Run child) {
        final Collection<BuildStreamTreeEntry> downstreamRuns = getDownstreamRuns(parent);

        for (BuildStreamTreeEntry e : downstreamRuns) {

            if ((e instanceof BuildStreamTreeEntry.BuildEntry &&
                    ((BuildStreamTreeEntry.BuildEntry) e).getRun().equals(child))) {
                return true;
            }
        }

        return false;
    }
}
