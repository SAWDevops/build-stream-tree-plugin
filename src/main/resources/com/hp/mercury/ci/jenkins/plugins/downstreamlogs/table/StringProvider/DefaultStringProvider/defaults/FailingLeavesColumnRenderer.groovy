package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.StringProvider.DefaultStringProvider.defaults

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.BuildStreamTreeEntry
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.JenkinsLikeXmlHelper
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.behavior.ColumnRenderer
import hudson.Functions
import hudson.model.Result
import hudson.model.Run
import jenkins.model.Jenkins

/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 08/07/13
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class FailingLeavesColumnRenderer implements ColumnRenderer {


    def problems

    def findProblems(node) {

        def buildEntry = node.value

        if (buildEntry instanceof BuildStreamTreeEntry.BuildEntry) {
            def result = buildEntry.run.details.result
            //note, if there are no children then children.any is false always, so i'm source of problem is true
            //also, if we just do any, it stops after the first match and stops scanning for problems...
            def problemNotInherited = !node.children.collect { child -> findProblems(child) }.any{it}
            def inProblem = (result != null && result != Result.SUCCESS)
            if (problemNotInherited && inProblem) {

                //run and buildEntry don't implement equals and hashcode, but we can use the string rep of a run as a uid
                problems.add(buildEntry.run.toString())
            }
            return inProblem
        }

        else {
            return false
        }
    }

    def FailingLeavesColumnRenderer(init) {

        //run and buildEntry don't implement equals and hashcode, but we can use the string rep of a run as a uid
        problems = new HashSet<String>();

        //launch recursion for each node wrapping root in tree defined by content given via init object
        init.content.forest.each{ rootNode -> findProblems(rootNode) }

    }

    Map cellMetadata(BuildStreamTreeEntry entry) {

        return ( (entry instanceof BuildStreamTreeEntry.BuildEntry) && entry?.run?.details.result ?
            [data:entry?.run?.details.result?.ordinal] :
            [data:Result.SUCCESS.ordinal]);
    }


    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.BuildEntry buildEntry) {

        def run = buildEntry.run
        //run and buildEntry don't implement equals and hashcode, but we can use the string rep of a run as a uid
        if (run.details.result != null && problems.contains(run.toString())) {

            def image = run.details.iconColor.image

            l.img(src:"$Jenkins.instance.rootUrl$Functions.resourcePath/images/24x24/$image")
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
