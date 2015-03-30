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
public class TreeColumnRenderer implements ColumnRenderer {

    def content
    def TreeColumnRenderer(init) {
        this.content = init
    }

    //additional data for sorting
    def rowCounter = 0

    def addConnector(l, name) {
        l.img(src: "$Jenkins.instance.rootUrl/plugin/downstream-logs/images/24x24/$name")
    }

    def addLine(l) {
        addConnector(l, "line.png")
    }
    def addLast(l) {
        addConnector(l, "last.gif")
    }
    def addNew(l) {
        addConnector(l, "new.gif")
    }
    def addEmpty(l) {
        addConnector(l, "space.png")
    }
    def addArrow(l) {
        addConnector(l, "me.png")
    }

    def depthFinished = new HashMap<Integer,Boolean>()

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

        def treeNode = this.content.findTreeNodeForBuildEntry(o)
        def d = treeNode.depth
        def r =
            (treeNode.parent == null ?
                this.content.content.forest.size() :
                treeNode.parent.children.size()) - treeNode.index - 1

        if (o instanceof BuildStreamTreeEntry.BuildEntry && o.run.equals(this.content.content.my.build)) {
            addArrow(l)
        }
        else {
            addEmpty(l)
        }

        for (def i = 0 ; i < d-1 ; i++) {
            depthFinished[i+1] ? addEmpty(l) : addLine(l)
        }

        if (r == 0) {
            addLast(l)
        }

        else {
            addNew(l)
        }

        depthFinished[d] = (r == 0)

        f()
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.BuildEntry buildEntry) {
        render (l, buildEntry) {

            def projectUrl = "${Jenkins.instance.rootUrl}${buildEntry.run.details.parent.url}"
            def buildUrl = "${projectUrl}/${buildEntry.run.details.number}"

            l.a(href: projectUrl, class: " model-link tl-tr ") {
                l.text(buildEntry.run.details.parent.fullDisplayName)
            }

            l.raw(" ")

            l.a(href: buildUrl, class: " model-link tl-tr ") {
                l.text("#${buildEntry.run.details.number}")
            }
        }
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.JobEntry jobEntry) {

        def projectUrl = "$Jenkins.instance.rootUrl$jobEntry.job.details.url"

        render (l, jobEntry) {
            l.a(href: projectUrl, class: " model-link tl-tr ") {
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
