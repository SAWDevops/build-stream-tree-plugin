package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.DownstreamLogsSideMenuLink

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.*
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.CiRun
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.services.JenkinsCiRun
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.Table
import jenkins.model.Jenkins
import org.kohsuke.stapler.Stapler

l = new JenkinsLikeXmlHelper(output)

def void render(buildEntry) {

// we display all rows here, the filtering is done on the tree
    l.tr() {
        cols.eachWithIndex { col, i ->

            def column = tableConf.columnExtenders[i].column
            def display = true
            try {
                //TODO build entry filters don't get access to init object?
                display = column.buildEntryFilter.display(buildEntry)
            }
            catch (Exception e) {
                Log.warning("failed to execute filter for $column.header: $e.message")
                Log.throwing(
                        "/com/hp/mercury/ci/jenkins/plugins/downstreamlogs/DownstreamLogsSideMenuLink/content.groovy",
                        "render",
                        e)
            }

            try {
                if (display) {
                    def cellMetadata = col.metaClass.respondsTo(col, "cellMetadata", BuildStreamTreeEntry) ?
                            col.cellMetadata(buildEntry) : Collections.emptyMap();
                    l.td(cellMetadata) {
                        col.render(l, buildEntry)
                    }
                } else {
                    l.td { l.text(" ") }
                }
            }
            catch (Exception e) {
                //show the error in the table
                l.td {
                    l.text(e.message)
                }

                //log the exception details
                Log.throwing(
                        "/com/hp/mercury/ci/jenkins/plugins/downstreamlogs/DownstreamLogsSideMenuLink/content.groovy",
                        "render",
                        e)
            }

        }
    }
}


def recursiveRender(node) {

    render(node.value)

    def children = node.children

    children.each { childNode ->
        recursiveRender(childNode)
    }
}

def renderTree() {
    l.div(class: "downstream-logs") {

        l.h1("Build Stream Tree")
        ////////////////////////////////////////////////////////
        // SELECT BUILD STREAM TREE CONFIGURATION
        ////////////////////////////////////////////////////////

        if (!emailMode) {
            l.script() {
                l.raw("""
//nathang: when evaluating the inner.html of the new buildstream tree, we must also add the relevant <script> tags.
//nathang: see renderOnDemand in hudson-behavior

                    function addScriptsFromElement(c) {

                        var elements = [];
                        var childCount = c.childNodes.length
                        for (var i = 0 ; i < childCount ; i++ ) {
                            var n = c.childNodes[i];
                            if (n.nodeType==1 )
                                elements.push(n);
                        }

                        evalInnerHtmlScripts(c.innerHTML,function() {
                            Behaviour.applySubtree(elements,true);
                        });
                    };


                    function switchBuildStreamTreeLayout(tableConfName) {

                        //these are defined by whatever it is that's loading this class (summary.jelly etc)
                        var oldDownstreamLogsDiv = getByClass("downstream-logs");

                        var p = oldDownstreamLogsDiv.parentNode;
                        var next = oldDownstreamLogsDiv.nextSibling;
                        var removeSpinner = addSpinner(p, oldDownstreamLogsDiv);
                        p.removeChild(oldDownstreamLogsDiv);

                        var getUrl = "${Jenkins.instance.rootUrl}${init.content.my.build.url}downstreamLogs/content?buildStreamTreeTableName=" + tableConfName;

                        new Ajax.Request(
                            getUrl,
                            {   method: "get",
                                onComplete: function(rsp,_) {

                                    removeSpinner();
                                    var newDownstreamLogs = document.createElement('div');
                                    newDownstreamLogs.classList.add("downstream-logs");
                                    newDownstreamLogs.innerHTML = rsp.responseText;
                                    p.insertBefore(newDownstreamLogs, next);
                                    addScriptsFromElement(newDownstreamLogs);
                                }
                            }
                        )
                    }
                """)
            }

            l.table(class: "pane bigtable") {
                l.tr() {
                    l.th() {
                        l.text("select build-stream-tree layout ")
                        l.select(onchange: "switchBuildStreamTreeLayout(this.options[this.selectedIndex].value)") {
                            DownstreamLogsAction.getDescriptorStatically().guiConfigurationTables.each { guiConfigTable ->
                                def optionAttributes = [value: guiConfigTable.name]
                                if (init.content.tableConf.name.equals(guiConfigTable.name)) {
                                    optionAttributes["selected"] = "true";
                                }
                                l.option(optionAttributes) {
                                    l.raw(guiConfigTable.name)
                                }
                            }
                        }
                        l.text(" (login to remember choice)")
                    }
                }
            }
        }


        def tableCSS = tableConf.css?.toString()
        if (tableCSS != null) {
            l.style {
                l.raw(tableCSS)
            }
        }

        def tableJS = tableConf.js?.toString()
        if (!emailMode && tableJS != null) {
            l.script {
                l.raw(tableJS)
            }
        }

        def tableAdditionalTopLayout = tableConf.additionalTopLayout?.toString()
        if (tableAdditionalTopLayout != null && !tableAdditionalTopLayout.isEmpty()) {
            //whatever happens in the script is evaluated when it is instantiated.
            tableConf.getAdditionalTopLayoutFactory().newInstance(init)
        }

        l.table(id: "$Table.TABLE_HTML_ID", style: "margin-top:0px; border-top: none;", class: "pane bigtable sortable") {

            l.tr() {
                tableConf.columnExtenders.each { colExt ->
                    l.th() {
                        l.raw(colExt.column.header)
                    }
                }
            }

            forest.each { rootNode ->
                recursiveRender(rootNode)
            }
        }

        if (!emailMode) {
            tableConf.columnExtenders.each { colExt ->
                def colJs = colExt.column?.js.toString()
                if (colJs != null && !colJs.isEmpty()) {
                    l.script() {
                        l.raw(colJs)
                    }
                }
            }
        }
    }

}

def class TreeNode {

    def parent
    def children
    def value
    def depth
    def index
    def buildEntryToNodeMap

    def TreeNode(buildEntryToNodeMap, value, parent, depth, index) {

        this.buildEntryToNodeMap = buildEntryToNodeMap
        this.value = value
        this.parent = parent
        this.children = []
        this.depth = depth
        this.index = index
    }

    def addChild(c) {
        def childNode = new TreeNode(buildEntryToNodeMap, c, this, this.depth + 1, children.size())
        buildEntryToNodeMap.put(c, childNode)
        children.add(childNode)
        return childNode
    }
}

def createTreeNode(value, parent, depth, index) {
    def node = new TreeNode(buildEntryToNodeMap, value, parent, depth, index)
    buildEntryToNodeMap.put(value, node)
    return node
}

def shouldAddToTree(be) {
    def displayRow
    try {
        //never hide a row describing this current build...
        def rowDescribesThisBuild = (be instanceof BuildStreamTreeEntry.BuildEntry) && be.run.equals(my.build)
        displayRow = rowDescribesThisBuild || rowFilter.display(be)
    }
    catch (Exception e) {
        displayRow = true;
        Log.warning("failed to evaluate row filter!")
        Log.throwing(
                "/com/hp/mercury/ci/jenkins/plugins/downstreamlogs/DownstreamLogsSideMenuLink/content.groovy",
                "render",
                e)

    }
    return displayRow
}

/*
we have a build filter, and these nodes are next to be displayed, but maybe some of them should be filtered out.
in that case, their children should be added to the list (or not, if they're filtered, and so on, and so forth)
 */

/*
in the words of noam kachko: why like this?
well, sometimes we have a string-entry parent that contributes child nodes, so we may as well already use
TreeNodes where we know we may have to modify things (namely indices) if there are rowFilters, and if so update just the indices.

 */

def void recursiveBuildTree(ListIterator<TreeNode> listIterator) {

    def index = 0
    while (listIterator.hasNext()) {
        //partially instantiated because child elements may or may not be set, and index may need adjusting
        def partiallyInstantiatedNode = listIterator.next()

        def be = partiallyInstantiatedNode.value

        if (shouldAddToTree(be)) {
            partiallyInstantiatedNode.index = index++
            if (be instanceof BuildStreamTreeEntry.BuildEntry) {
                DownstreamLogsUtils.getDownstreamRuns(be.run).each { triggeredBe ->
                    partiallyInstantiatedNode.addChild(triggeredBe)
                }
            }
            //child elements can also be set not by buildentries, but string entries in createRootNodes
            recursiveBuildTree(partiallyInstantiatedNode.children.listIterator())
        } else {

            listIterator.remove()

            //we need an index to set values of children with, but it's not the real index because things may change in the recursion...
            def indexForRecursion = index

            def childrenToAddToThisLevel
            //pull all of its kids to be "my" kids, recursively.
            if (be instanceof BuildStreamTreeEntry.BuildEntry) {
                def runs = DownstreamLogsUtils.getDownstreamRuns(be.run)
                childrenToAddToThisLevel = new ArrayList<TreeNode>(runs.size())

                runs.each { e ->
                    def c = createTreeNode(e, partiallyInstantiatedNode.parent, partiallyInstantiatedNode.depth, indexForRecursion++)
                    childrenToAddToThisLevel.add(c)
                }
            }
            //children element can already be set when we're a stringentry, by createRootNodes
            else {
                childrenToAddToThisLevel = new ArrayList<TreeNode>(partiallyInstantiatedNode.children.size())

                partiallyInstantiatedNode.children.each { c ->
                    c.index = indexForRecursion++;
                    c.depth = partiallyInstantiatedNode.depth
                    c.parent = partiallyInstantiatedNode.parent
                    childrenToAddToThisLevel.add(c)
                }
            }

            recursiveBuildTree(childrenToAddToThisLevel.listIterator())

            childrenToAddToThisLevel.each { c ->
                //fix indexes, the different recursions don't know about eachother...
                c.index = index++
                listIterator.add(c)
            }
        }
    }
}

def List<TreeNode> createRootNodes(List<BuildStreamTreeEntry.BuildEntry> bes) {

    def index = 0

    def ret = new ArrayList<TreeNode>(bes.size())

    for (BuildStreamTreeEntry.BuildEntry be : bes) {

        def node

        def upstreamCause = be.run.getUpstreamCause()

        //if there's an upstream cause it's either cause the run doesn't exist, or because it's not really ours (faked by rebuild plugin for example)
        if (upstreamCause != null &&
                //when the upstream run is deleted, we don't collect it into the roots,
                // so if it is null it means this is the use case where we display partial information because the upstream run is deleted,
                // and if it's not null - then there's some other reason why this isn't a root, for example, rebuild plugin -
                // and then we don't display anything.
                upstreamCause.getUpstreamRun() == null) {

            def replacingRoot = new BuildStreamTreeEntry.StringEntry("$upstreamCause.upstreamProject #$upstreamCause.upstreamBuild")
            node = createTreeNode(replacingRoot, null, 1, index)
            node.addChild(be)
        } else {
            node = createTreeNode(be, null, 1, index)
        }

        ret.add(node)
        index++
    }

    return ret
}

def calculateForest(build) {

    buildEntryToNodeMap = new HashMap()

    def index = 0
    //this is naive: some of these have an upstream that may no longer exist, but should still be displayed as string
    //also, some may need to be filtered out.
    def forest = DownstreamLogsUtils.getRoots(build).collect{
        new BuildStreamTreeEntry.BuildEntry(it)
    }

    //after create root nodes we have nodes that either don't have children, and originated by build entries,
    //or do have children and are the string entries of the no longer existing upstream of the some build in forest,
    //and their children are actually a child who is a member of forest.
    forest = createRootNodes(forest)

    //from the roots, build the tree, recursively, taking into account row filter and the data from the string entry like roots
    recursiveBuildTree(forest.listIterator())

    return forest

}

def userSpecificLayout() {

    def descriptor = DownstreamLogsAction.getDescriptorStatically()

    //if not email mode, we have stapler, we can get current request
    def requestTableName = Stapler.getCurrentRequest().getParameter("buildStreamTreeTableName")
    if (requestTableName != null) {
        //do our best to remember current user choice, if logged in
        try {
            descriptor.setUserGuiTableName(Jenkins.instance.me, requestTableName)
        } catch (Exception e) {
        }
        return specifiedTableName = requestTableName;
    } else {
        def userChoice;
        //
        try {
            userChoice = descriptor.getUserGuiTableName(Jenkins.instance.me)
        } catch (Exception e) {
        }
        if (userChoice != null) {

            return specifiedTableName = userChoice
        }
    }

    //if no new info from request, use default
    return descriptor.getGuiConfigurationTables().get(0).name
}

def renderTreeForBuild(build) {

    def descriptor = DownstreamLogsAction.getDescriptorStatically()
    def tables = descriptor.getGuiConfigurationTables()

    emailMode = this.binding.variables.get("emailMode") ?: false

    //this may be defined externally, for example, via DownstreamLogsEmailContent
    //if not, check the global email table
    def specifiedTableName =
            //if tableName is specified in binding (like when specifying via $BUILD_STREAM_TREE)
            this.binding.variables.get("tableName") ?:
                    //or if it's an email, use default email
                    (emailMode ?
                            (descriptor.getEmailTable() ?: tables.get(0).name) :
                            userSpecificLayout())

    //if there's a specified value try to use it
    def tableConfiguration = tables.find { currentTableConfig -> currentTableConfig.name.equals(specifiedTableName) }

    //put in global scope, if can't find selected config, take default table
    tableConf = tableConfiguration ?: tables.get(0)

    init = tableConf.getInitObjectFactory()?.newInstance(this)
    rowFilter = tableConf.getRowFilterFactory()?.newInstance(init)

    //calculating the tree requiress knowing the rows to ignore, so row filter above
    forest = calculateForest(build);

    //columns may need access to the forest
    cols = tableConf.columnExtenders.collect { colExt ->
        colExt.column.getColumnRendererFactory()?.newInstance(init)
    }

    renderTree()
}


try {
    //TODO: Refactor - use CiRun
    renderTreeForBuild(new JenkinsCiRun(my.build))
}
catch (Exception e) {
    Log.warning("failed to display build-stream-tree for $my.build")
    Log.throwing("content.groovy", "", e)
    def baos = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(baos);
    e.printStackTrace(out);
    out.flush();
    out.close();
    l.text("failed rendering build stream tree: " + new String(baos.toByteArray()));
}


