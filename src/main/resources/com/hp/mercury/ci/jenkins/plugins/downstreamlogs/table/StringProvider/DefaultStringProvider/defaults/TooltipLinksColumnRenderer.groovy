package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.StringProvider.DefaultStringProvider.defaults

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.BuildStreamTreeEntry
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.JenkinsLikeXmlHelper
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.behavior.ColumnRenderer
import jenkins.model.Jenkins

/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 08/07/13
 * Time: 23:31
 * To change this template use File | Settings | File Templates.
 */
class TooltipLinksColumnRenderer implements ColumnRenderer {

    Map cellMetadata(BuildStreamTreeEntry entry) {
        switch (entry) {
            case BuildStreamTreeEntry.BuildEntry:
                return [data: 2]
                break
            case BuildStreamTreeEntry.JobEntry:
                return [data: 1]
                break
            case BuildStreamTreeEntry.StringEntry:
                return [data: 0]
                break
        }
    }


    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.BuildEntry buildEntry) {

        def projectUrl = "${Jenkins.instance.rootUrl}${buildEntry.getInnerRun().parent.url}"
        def buildUrl = "${projectUrl}/${buildEntry.getInnerRun().number}"

        l.a(href: projectUrl, class: " model-link tl-tr ") {
            l.text("JOB")
        }

        l.raw(" ")

        l.a(href: buildUrl, class: " model-link tl-tr ") {
            l.text("BUILD")
        }
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.JobEntry jobEntry) {
        def projectUrl = "$Jenkins.instance.rootUrl$jobEntry.job.url"
        l.a(href: projectUrl, class: " model-link tl-tr ") {
            l.text("JOB")
        }
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.StringEntry stringEntry) {
        l.text(" ")
    }
}
