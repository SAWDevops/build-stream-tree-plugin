package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.StringProvider.DefaultStringProvider.defaults

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.BuildStreamTreeEntry
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.JenkinsLikeXmlHelper
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.behavior.ColumnRenderer
import hudson.model.Run

/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 09/07/13
 * Time: 09:25
 * To change this template use File | Settings | File Templates.
 */
class LastSuccessColumnRenderer implements ColumnRenderer {

    Map cellMetadata(BuildStreamTreeEntry entry) {

        if (entry instanceof BuildStreamTreeEntry.BuildEntry && (entry?.run?.parent?.lastSuccessfulBuild)) {
            return [data: entry.run.parent.lastSuccessfulBuild.getTimeInMillis()]
        }

        else if (entry instanceof BuildStreamTreeEntry.JobEntry && (entry.job.lastSuccessfulBuild)) {
            return [data: entry.job.lastSuccessfulBuild.getTimeInMillis()]
        }

        return [data:0];
    }

    private void renderNullSafe(Run build, JenkinsLikeXmlHelper l) {
        if (build == null) {
            l.text("N/A")
        } else {
            l.text(build.getTimestampString() + " ago")
        }
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.BuildEntry buildEntry) {

        def build = buildEntry.getInnerRun().parent.lastSuccessfulBuild

        renderNullSafe(build, l)
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.JobEntry jobEntry) {
        def build = jobEntry.job.lastSuccessfulBuild
        renderNullSafe(build, l)
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.StringEntry stringEntry) {
        l.text(" ")
    }
}
