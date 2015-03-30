package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.StringProvider.DefaultStringProvider.defaults

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.BuildStreamTreeEntry
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.JenkinsLikeXmlHelper
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.behavior.ColumnRenderer
import hudson.Functions
import hudson.model.AbstractBuild
import hudson.model.Actionable
import hudson.tasks.test.AggregatedTestResultAction
import jenkins.model.Jenkins

/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 08/07/13
 * Time: 23:31
 * To change this template use File | Settings | File Templates.
 */
class TestResultsLinksColumnRenderer implements ColumnRenderer {

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

        def projectUrl = "${Jenkins.instance.rootUrl}${buildEntry.run.details.parent.url}"
        def buildUrl = "${projectUrl}/${buildEntry.run.details.number}"
        //TODO: Refactor - test result actions exists in AbstractBuild and not in Run. how to avoid the casting?
        def testResults = buildEntry.run.details.testResultAction
        def testResultsUrl = "$buildUrl/${testResults?.urlName}"

        if (testResults == null) {

            l.text(" ")
        }

        else {

            l.a(href: testResultsUrl) {
                l.img(src:"$Jenkins.instance.rootUrl$Functions.resourcePath/images/24x24/clipboard.png")
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
