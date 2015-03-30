package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.StringProvider.DefaultStringProvider.defaults

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.BuildStreamTreeEntry
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.JenkinsLikeXmlHelper
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.behavior.ColumnRenderer

/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 09/07/13
 * Time: 09:25
 * To change this template use File | Settings | File Templates.
 */
class DurationColumnRenderer implements ColumnRenderer {

    Map cellMetadata(BuildStreamTreeEntry entry) {
        if (entry instanceof BuildStreamTreeEntry.BuildEntry) {
            def duration = entry.run.isBuilding() ?
                (System.currentTimeMillis() - entry.run.details.timeInMillis) :
                entry.run.details.duration
            return [data: duration]
        }

        return Collections.emptyMap();
    }


    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.BuildEntry buildEntry) {

        l.text(buildEntry.run.details.getDurationString())
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
