package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.StringProvider.DefaultStringProvider.defaults

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.BuildStreamTreeEntry
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.JenkinsLikeXmlHelper
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.behavior.ColumnRenderer
import hudson.Util

/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 09/07/13
 * Time: 09:25
 * To change this template use File | Settings | File Templates.
 */
class ETAColumnRenderer implements ColumnRenderer {


    Map cellMetadata(BuildStreamTreeEntry entry) {
        if (entry instanceof BuildStreamTreeEntry.BuildEntry) {
            def run = entry.run

            if (run.isBuilding()) {
                def estimated = run.getEstimatedDuration()
                def duration = System.currentTimeMillis() - run.details.timeInMillis
                def eta = estimated - duration
                return [data:eta]
            }
            else {
                return [data:0]
            }
        }

        return [data:0]
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.BuildEntry buildEntry) {

        def run = buildEntry.run

        if (run.isBuilding()) {
            def estimated = run.getEstimatedDuration()
            def duration = System.currentTimeMillis() - run.details.timeInMillis
            def eta = estimated - duration

            l.raw("${Util.getTimeSpanString(Math.abs(eta))} ${eta < 0 ? " overdue" : ""}")
        }
        else {

            l.text(" ")
        }
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.JobEntry jobEntry) {
        l.text(" ")
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.StringEntry stringEntry) {
        l.text(" ")
    }
}
