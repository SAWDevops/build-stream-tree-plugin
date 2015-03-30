package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.StringProvider.DefaultStringProvider.defaults

import com.hp.commons.core.collection.CollectionUtils
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.BuildStreamTreeEntry
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DownstreamLogsAction
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.Log
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.Table
import hudson.Functions
import hudson.Util
import hudson.model.AbstractBuild
import hudson.model.Result
import hudson.model.ViewGroup
import hudson.scm.ChangeLogSet
import hudson.slaves.EnvironmentVariablesNodeProperty
import jenkins.model.Jenkins
import hudson.matrix.MatrixConfiguration

import java.sql.Timestamp


/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 09/07/13
 * Time: 12:21
 * To change this template use File | Settings | File Templates.
 *
 */

//this is actually a lie, thsi script isn't an initializer it's a layout contributor, it executes differently
// and its argument is the initializer, not content.groovy
def init = this.binding.getVariable("content.groovy")

//init.content points to content.groovy
def l = init.content.l

def alignLeft = ["text-align":"left", align:"left"]
def forest = init.content.forest

//////////////////////////////////////
//GENERAL FLOW INFORMATION
//////////////////////////////////////

l.script() {
    l.raw("""
    function toggleRowsDisplay(row) {
        row.style.display = row.style.display == "" ? "none" : "";
        var nextRow = row.nextSibling;
        if (nextRow != null) {
            toggleRowsDisplay(nextRow);
        }
    }
    """)
}

l.table(class:"pane bigtable") {
    l.tr() {

        l.td() {

            l.table(style:"width:100%;") {
                l.tr(onclick:"toggleRowsDisplay(this.nextSibling)") {
                    l.th() {
                        l.text("General Flow Information (click to expand)")
                    }
                }

                forest.each { rootNode ->

                    def be = rootNode.value
                    if (be instanceof BuildStreamTreeEntry.BuildEntry) {

                        def run = be.run

                        def changeSet = run instanceof AbstractBuild ? run.changeSet : null

                        l.tr(style:"display:none;") {
                            l.th(alignLeft) {
                                l.text("$run")
                                if (changeSet != null && !changeSet.isEmptySet()) {
                                    l.text(" ")
                                    l.a(href:"$Jenkins.instance.rootUrl$run.url/changes") {
                                        l.text("(changes)")
                                    }

                                } else {
                                    l.text(" (no changes)")
                                }
                                l.text(" was started due to:")
                            }
                        }

                        run.causes.each { cause ->
                            l.tr(style:"display:none;") {
                                l.td() {
                                    l.text(cause.shortDescription)
                                }
                            }
                        }

                        if (!changeSet.isEmptySet()) {
                            l.tr(style:"display:none;") {
                                l.th(alignLeft) {
                                    l.text("Flow contributors:")
                                }
                            }

                            def changeEntryIter = changeSet.iterator()
                            l.tr(style:"display:none;") {
                                l.td() {
                                    l.table() {
                                        while (changeEntryIter.hasNext()) {
                                            ChangeLogSet.Entry entry = changeEntryIter.next();
                                            l.tr() {
                                                l.td() {l.text("${new Timestamp(entry.getTimestamp())}")}
                                                l.td() {l.text("$entry.commitId")}
                                                l.td() {l.text("$entry.author")}
                                                l.td() {l.text("$entry.msgEscaped")}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/////////////////////////////////////////////
//ABSTRACT PIPELINE
/////////////////////////////////////////////



String pipelineClusteringView = Jenkins.instance.globalNodeProperties.find { gnp ->
    return (gnp instanceof EnvironmentVariablesNodeProperty) ? gnp : null
}?.envVars.get("PIPELINE_CLUSTERING_VIEW")

Log.debug("AbstractPipelineAdditionalTopLayout: pipelineClusteringView " + pipelineClusteringView)

def associate(treeNode, clusterName) {
    def cluster = buildToClusterAssociation.get(clusterName, [])
    cluster.add(treeNode)
}

def recursiveAssociateToCluster(treeNode, clusterName) {

    if (treeNode.value instanceof BuildStreamTreeEntry.BuildEntry) {
        associate(treeNode, clusterName)
    }

}

viewToContainedJobsCache = [:]
def viewContains(view, job) {

    //don't use get with default because we don't want to evaluate getItems on view, which could be expensive
    def cache = viewToContainedJobsCache.get(view)

    if (cache == null) {
        cache = view.items
        viewToContainedJobsCache.put(view, cache)
    }

    return cache.contains(job)
}

def associateSameAsAncestor(nodeToAssociate, currentAncestor, containingViews) {

    if (currentAncestor == null) {
        containingViews.each { view ->
            associate(nodeToAssociate, view.viewName)
        }
    }

    else {
        def be = currentAncestor.value

        if (be instanceof BuildStreamTreeEntry.BuildEntry) {

            def previousAncestorContainingViews = containingViews;

            containingViews = containingViews.findResults { view ->
                viewContains(view, be.run.details.parent) ? view : null
            }

            //if this ancestor isn't contained anywhere - skip it
            if (containingViews.isEmpty()) {
                associateSameAsAncestor(nodeToAssociate, currentAncestor.parent, previousAncestorContainingViews)
            }
            //if this ancestor is contained in exactly one spot from the original node's options - use that.
            if (containingViews.size() == 1) {
                associate(nodeToAssociate, containingViews[0].name)
            }
            //if this ancestor narrowed down the list of choices of the original node...
            else {
                associateSameAsAncestor(nodeToAssociate, currentAncestor.parent, containingViews)
            }
        }
    }
}


def associateBuildToCluster(treeNode) {

    def be = treeNode.value

    if (be instanceof BuildStreamTreeEntry.BuildEntry) {

        def job = be.run.details.parent
        if (job instanceof MatrixConfiguration) {
            job = job.parent
        }

        def containingViews = pipelineClusterViews.findResults { view ->
            viewContains(view, job) ? view : null
        }

        if (!containingViews.isEmpty()) {
            if (containingViews.size() == 1) {
                associate(treeNode, containingViews[0].name)
            }
            else {
                associateSameAsAncestor(treeNode, treeNode.parent, containingViews)
            }
        }
    }

    treeNode.children.each { associateBuildToCluster(it) }
}

def renderBallForCluster(l, clusterTreeNodes) {

    def result

    if (clusterTreeNodes == null || clusterTreeNodes.isEmpty()) {

        result = Result.NOT_BUILT
    }

    else {

        result = Result.SUCCESS

        def iter = clusterTreeNodes.iterator()
        while (iter.hasNext()) {
            def iteratedResult = iter.next().value.run.details.result
            result = iteratedResult == null ? Result.NOT_BUILT : result.combine(iteratedResult)
        }
    }

    l.img(src:"$Jenkins.instance.rootUrl$Functions.resourcePath/images/24x24/$result.color.image")
}

def recGetNestingString(node) {
    if (node == null) {
        return ""
    }
    return recGetNestingString(node.parent) + (node.index+1) + "."
}


if (pipelineClusteringView != null) {

    ViewGroup view = Jenkins.instance
    pipelineClusteringView.tokenize('/').each { viewName ->
        view = view?.getView(viewName)
        Log.debug("finding pipeline configuration, entering view:" + view.displayName)
    }

    //this should list pipelines
    if (view.hasProperty("views")) {

        for (pipeline in view.views) {

            if (pipeline.hasProperty("views")) {

                pipelineClusterViews = pipeline.views

                buildToClusterAssociation = [:]

                forest.each { root ->

                    associateBuildToCluster(root)
                }

                if (!buildToClusterAssociation.isEmpty()) {

                    l.style() {
                        l.raw("""

                    #abstract-pipeline td {
                        background-color: white;
                        text-align: center;
                    }

                    #abstract-pipeline td:hover {
                        background-color: #f0f0f0;
                    }

                    #abstract-pipeline td.selected {
                        background-color: #00FFFF;
                    }

                    #${Table.TABLE_HTML_ID} tr.clusterSelected {
                        background-color: #00FFFF;
                    }

                """)
                    }

                    l.script() {
                        l.raw("""

                var cachedTree;
                var collapseCellIndex = null;
                var inRow = false;

                function getCollapseCell(row) {
                    if (collapseCellIndex === null) {
                        var cellsCount = row.cells.length;
                        for (var i = 0; i < cellsCount ; i++) {
                            var expandCollapseContainer = row.cells[i];
                            var prefix = expandCollapseContainer.getAttribute("prefix");
                            if (prefix != null) {
                                collapseCellIndex = i;
                                return expandCollapseContainer;
                            }
                        }
                        return null;
                    }
                    else {
                        return row.cells[collapseCellIndex];
                    }
                }

                function collapseTreeByCluster(clusterNodes) {

                    //when we just entered the row - save the configuration.
                    //has to be done this way because onmouseover of td happens before on tr
                    if (!inRow) {
                        inRow = true;
                        saveTree();
                    }

                    if (window.expandCollapse == null) return;

                    var coordinates = eval(clusterNodes);
                    var coordsLength = coordinates.length;

                    var tree = document.getElementById("$Table.TABLE_HTML_ID");
                    var rows = tree.rows;
                    var size = rows.length;

                    for (var i = 0 ; i < size ; i++) {
                        var expandCollapseContainer = getCollapseCell(rows[i]);

                        if (expandCollapseContainer != null) {

                            var prefix = expandCollapseContainer.getAttribute("prefix");

                            var expanded = false;

                            //now go over all entries in cluster, and see if this node is a prefix to any of them...
                            for (var j = 0 ; j < coordsLength ; j++) {

                                var coord = coordinates[j];
                                if (coord.indexOf(prefix) == 0) {

                                    if (coord === prefix) {
                                        rows[i].classList.add("clusterSelected");
                                    }
                                    else {
                                        rows[i].classList.remove("clusterSelected");
                                        expanded = true;
                                    }
                                    break;
                                }
                                else {
                                    rows[i].classList.remove("clusterSelected");
                                }
                            }

                            //next make sure we're not a leaf
                            var containingDiv = expandCollapseContainer.getElementsByTagName("div")[0];
                            if (containingDiv != null) {

                                expanded ? expand(containingDiv, false) : collapse(containingDiv, false);
                            }
                        }
                    }
                }

                function restoreTree() {

                    //we just left the row
                    inRow = false;

                    if (window.expandCollapse == null) return;

                    var tree = document.getElementById("$Table.TABLE_HTML_ID");
                    var rowsCount = tree.rows.length;
                    for (var i = 0 ; i < rowsCount ; i++) {
                        var row = tree.rows[i];
                        var expandCollapseContainer = getCollapseCell(row);
                        var previousState = cachedTree[i];
                        if (previousState["clusterSelected"]) {
                            row.classList.add("clusterSelected");
                        }
                        else {
                            row.classList.remove("clusterSelected");
                        }
                        if (expandCollapseContainer != null) {
                            var containingDiv = expandCollapseContainer.getElementsByTagName("div")[0];
                            if (containingDiv != null) {

                                var expanding = previousState["expanded"] === "true";
                                expanding ? expand(containingDiv,false) : collapse(containingDiv, false) ;
                            }
                        }
                    }
                }

                function saveTree() {

                    if (window.expandCollapse == null) return;

                    cachedTree = new Array();
                    var tree = document.getElementById("$Table.TABLE_HTML_ID");
                    var rowsCount = tree.rows.length;
                    for (var i = 0 ; i < rowsCount ; i++) {
                        var row = tree.rows[i];
                        var expandCollapseContainer = getCollapseCell(row);
                        var state = new Array();
                        if (row.classList.contains("clusterSelected")) {
                            state["clusterSelected"] = true;
                        }
                        if (expandCollapseContainer != null) {
                            var containingDiv = expandCollapseContainer.getElementsByTagName("div")[0];
                            if (containingDiv != null) {
                                state["expanded"] = containingDiv.getAttribute("expanded");
                            }
                        }
                        cachedTree[i] = state;
                    }
                }

                function unselectAbstractPipeline(keepClusterSelected) {

                    var pipelineTable = document.getElementById("abstract-pipeline");

                    var row = pipelineTable.rows[2];
                    var cellsCount = row.cells.length;
                    for (var i = 0 ; i < cellsCount ; i++) {
                        var cell = row.cells[i];
                        cell.setAttribute("class","");
                    }

                    var tree = document.getElementById("$Table.TABLE_HTML_ID");
                    var treeRowsCount = tree.rows.length;
                    for (var i = 0 ; i < treeRowsCount ; i++) {
                        var treeRow = tree.rows[i];
                        if (!keepClusterSelected) {
                            treeRow.classList.remove("clusterSelected")
                        }
                    }
                }

                function selectCluster(td) {
                    saveTree();
                    unselectAbstractPipeline(true);
                    td.setAttribute("class", "selected");
                }

                """)
                    }

                    def colWidth = "${(int)(100 / pipelineClusteringView.size())}%"

                    l.table (id:"abstract-pipeline",class:"pane bigtable") {

                        l.tr() {
                            l.th(colspan:pipelineClusterViews.size()) {
                                l.text("Pipeline: $pipeline.viewName")
                            }
                        }

                        l.tr() {
                            pipelineClusterViews.each { pcv ->
                                l.th(width:colWidth) {
                                    l.text(" "+pcv.viewName)
                                }
                            }
                        }

                        l.tr(onmouseout: "restoreTree();") {
                            pipelineClusterViews.each { pcv ->
                                def clusterName = pcv.viewName
                                def clusterTreeNodes = buildToClusterAssociation.get(clusterName)
                                l.td(clusterNodes: clusterTreeNodes.collect{"\"${recGetNestingString(it)}\""}.toString(),
                                        onmouseover: "collapseTreeByCluster(this.getAttribute(\"clusterNodes\"))",
                                        onclick: "selectCluster(this)") {
                                    l.text(" ")
                                    renderBallForCluster(l, clusterTreeNodes)
                                }
                            }
                        }
                    }

                    //we found a cluster that's right for us, now we stop
                    break
                }
            }
            else {
                throw new RuntimeException("The jenkins admin has defined an abstract pipeline header for " +
                        "build-stream-tree, but pipeline " + pipeline.displayName + " of type " +
                        pipeline.class.simpleName + " doesn't have subviews (it's not a NestedView)")
            }
        }


    }
    else {
        throw new RuntimeException("Jenkins Admin has defined an abstract pipeline header for build-stream-tree, but there are no pipelines defined.");
        Log.debug("unable to list subviews of supposed pipeline view " + view.displayName + " of type " + view.class.simpleName + " (should be NestedView...)");
    }

}