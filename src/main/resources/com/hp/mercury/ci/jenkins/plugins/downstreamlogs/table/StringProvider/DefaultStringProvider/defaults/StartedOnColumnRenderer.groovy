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
class StartedOnColumnRenderer implements ColumnRenderer {

    Map cellMetadata(BuildStreamTreeEntry entry) {

        if (entry instanceof BuildStreamTreeEntry.BuildEntry) {
            return [data: entry.run.details.timestamp.getTimeInMillis()]
        }

        return [data:0]
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.BuildEntry buildEntry) {
        l.text(buildEntry.run.details.getTimestampString() + " ago")
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
