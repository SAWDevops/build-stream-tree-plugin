package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.StringProvider.DefaultStringProvider.defaults

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.BuildStreamTreeEntry
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.JenkinsLikeXmlHelper
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.behavior.ColumnRenderer
import hudson.Functions
import hudson.model.BallColor
import jenkins.model.Jenkins

/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 08/07/13
 * Time: 23:06
 * To change this template use File | Settings | File Templates.
 */
class BallStatusColumnRenderer implements ColumnRenderer {

    Map cellMetadata(BuildStreamTreeEntry entry) {
        switch (entry) {
            case BuildStreamTreeEntry.BuildEntry:
                return [data: entry.getRun().iconColor.image]
                break
            case BuildStreamTreeEntry.JobEntry:
                return [data: BallColor.GREY.image]
                break
            case BuildStreamTreeEntry.StringEntry:
                return [data: BallColor.GREY.image]
                break
        }
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.BuildEntry buildEntry) {
        //sort by image name
        def image = buildEntry.run.details.iconColor.image
        l.img(src:"$Jenkins.instance.rootUrl$Functions.resourcePath/images/24x24/$image")
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.JobEntry jobEntry) {

        def image = BallColor.GREY.image
        l.img(src:"$Jenkins.instance.rootUrl$Functions.resourcePath/images/24x24/${image}")
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.StringEntry stringEntry) {
        def image = BallColor.GREY.image
        l.img(src:"$Jenkins.instance.rootUrl$Functions.resourcePath/images/24x24/${image}")
    }
}
