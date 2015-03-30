package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.StringProvider.DefaultStringProvider.defaults

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.BuildStreamTreeEntry
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.JenkinsLikeXmlHelper
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.behavior.ColumnRenderer
import jenkins.model.Jenkins

/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 08/07/13
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class EmailTreeColumnRenderer implements ColumnRenderer {

    def content
    def EmailTreeColumnRenderer(init) {
        this.content = init
    }

    def addConnector(l, name) {
        l.img(src: "$Jenkins.instance.rootUrl/plugin/downstream-logs/images/24x24/$name")
    }

    //additional data for sorting
    def rowCounter = 0

    def space(l) {
        l.pre("   ")
    }

    def me(l) {
        addConnector(l, "me.png")
    }

    Map cellMetadata(BuildStreamTreeEntry entry) {
        def treeNode = this.content.findTreeNodeForBuildEntry(entry)
        def d = treeNode.depth

        --rowCounter

        return [style:"margin-left:${d*40}px",
            data: rowCounter]
    }

    /**
     *
     * @param l xml helper
     * @param d depth
     * @param r remaining
     * @param o currently iterated object, real object, not build entry
     * @param f callback to render current line view
     * @return
     */
    def render(l,o,f) {

        if (o instanceof BuildStreamTreeEntry.BuildEntry && o.run.equals(this.content.content.my.build)) {
            me(l)
        }
        else {
            space(l)
        }

        f()
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.BuildEntry buildEntry) {
        render (l, buildEntry) {

            def projectUrl = "${Jenkins.instance.rootUrl}${buildEntry.run.details.parent.url}"
            def buildUrl = "${projectUrl}/${buildEntry.run.details.number}"

            l.a(href: projectUrl) {
                l.text(buildEntry.run.details.parent.fullDisplayName)
            }

            l.raw(" ")

            l.a(href: buildUrl) {
                l.text("#${buildEntry.run.details.number}")
            }
        }
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.JobEntry jobEntry) {

        def projectUrl = "$Jenkins.instance.rootUrl$jobEntry.job.details.url"

        render (l, jobEntry) {
            l.a(href: projectUrl) {
                l.text("$jobEntry.job.fullDisplayName #???")
            }
        }
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.StringEntry stringEntry) {
        render (l, stringEntry) {
            l.text(stringEntry.string)
        }
    }

}
