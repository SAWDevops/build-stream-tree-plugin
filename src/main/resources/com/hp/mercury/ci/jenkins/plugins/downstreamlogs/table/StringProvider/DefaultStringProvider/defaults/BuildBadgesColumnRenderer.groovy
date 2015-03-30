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
 * Time: 23:31
 * To change this template use File | Settings | File Templates.
 *
 * THIS COLUMN RENDERER MUST NOT BE INCLUDED IN EMAILS, IT WILL FAIL THERE BECAUSE THERE IS NO STAPLER
 *
 */
class BuildBadgesColumnRenderer implements ColumnRenderer {

    def content

    public BuildBadgesColumnRenderer(init) {
        this.content = init
    }

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


            l.text(" ")

            /*
            <j:forEach var="badge" items="${badges}">
                <st:include it="${badge}" page="badge.jelly" />
            </j:forEach>
            */

            def context = this.content.content.context

        if (!this.content.content.emailMode) {

            context.setVariable("build", buildEntry.run)

            /*
            copy paste from Functions initPageVariables
             */

            Functions h = new Functions();
            context.setVariable("h", h);

            def rootURL = Jenkins.instance.rootUrl
            // The path starts with a "/" character but does not end with a "/" character.
            context.setVariable("rootURL", rootURL);

            /*
                load static resources from the path dedicated to a specific version.
                This "/static/VERSION/abc/def.ghi" path is interpreted by stapler to be
                the same thing as "/abc/def.ghi", but this avoids the stale cache
                problem when the user upgrades to new Jenkins. Stapler also sets a long
                future expiration dates for such static resources.

                see https://wiki.jenkins-ci.org/display/JENKINS/Hyperlinks+in+HTML
             */
            context.setVariable("resURL",rootURL+h.getResourcePath());
            context.setVariable("imagesURL",rootURL+h.getResourcePath()+"/images");

            buildEntry.run.details.badgeActions.each { badge ->
                context.setVariable("badge", badge)
                //context.setVariable("rootURL", Jenkins.instance.rootUrl);
                this.content.getStapler().include(it:badge, page:"badge.jelly")
            }
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
