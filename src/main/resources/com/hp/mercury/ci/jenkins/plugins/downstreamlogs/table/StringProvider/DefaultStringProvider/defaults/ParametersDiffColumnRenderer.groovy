package com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.StringProvider.DefaultStringProvider.defaults

import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.BuildStreamTreeEntry
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.JenkinsLikeXmlHelper
import com.hp.mercury.ci.jenkins.plugins.downstreamlogs.table.behavior.ColumnRenderer
import hudson.model.ParameterValue
import hudson.model.ParametersAction
import jenkins.model.Jenkins
import org.apache.commons.jelly.XMLOutput

/**
 * Created with IntelliJ IDEA.
 * User: grunzwei
 * Date: 08/07/13
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class ParametersDiffColumnRenderer implements ColumnRenderer {

    def init
    def counter = 0
    def lHelper

    def ParametersDiffColumnRenderer(init) {

        this.init = init

        if (!init.content.emailMode) {
            def l = init.content.l
            l.script() {
                l.raw("""
                    tooltip = new YAHOO.widget.Tooltip("tt", {context:[], zindex:999, width:\"100%\"});
                """)
            }
            lHelper = l
        }

    }

    def toMap(List<ParametersAction> parameters) {
        def ret = [:]

        if (parameters != null) {
            for (pa in parameters) {
                def pvi = pa.iterator()
                while (pvi.hasNext()) {
                    def pv = pvi.next()
                    ret.put(pv.name, pv);
                }
            }
        }

        return ret;
    }

    def addImage(l, name) {
        l.img(src: "$Jenkins.instance.rootUrl/plugin/downstream-logs/images/24x24/$name")
    }

    def renderParamValue(l, ParameterValue pv) {

        if (this.init.content.emailMode) {

            l.raw(pv.toString())
        }

        else {

            def st = this.init.getStapler()
            //we wrap in table because some bring <tr> directly...
            l.table() {
                st.include(page:"value", optional:"true", it:pv) {
                    //this happens if optional page is not found or rendered
                    l.raw(pv.toString())
                }
            }
        }
    }

    def calculateChanges(List<ParametersAction> currentParameters, List<ParametersAction> parentParameters) {
        def parentParametersMap = toMap(parentParameters)
        def currentParametersMap = toMap(currentParameters)

        def newParameters = new HashSet(currentParametersMap.keySet())
        newParameters.removeAll(parentParametersMap.keySet())
        newParameters = newParameters.collect {name -> currentParametersMap.get(name)}

        def removedParameters = new HashSet(parentParametersMap.keySet())
        removedParameters.removeAll(currentParametersMap.keySet())
        removedParameters = removedParameters.collect {name -> parentParametersMap.get(name)}

        def modifiedParameters = new HashSet(currentParametersMap.keySet())
        modifiedParameters.retainAll(parentParametersMap.keySet())
        modifiedParameters = modifiedParameters.findResults { paramName ->
            def value = currentParametersMap.get(paramName)
            def parentValue = parentParametersMap.get(paramName)

            //the == true because it may evaluate to null, which isn't exactly a boolean, so make sure and be explicit
            return (value?.equals(parentValue) == true) ?
                null :
                [parentValue, value]
        }

        return [newParameters, removedParameters, modifiedParameters];
    }

    def renderParamDiff(l,
                        List<ParametersAction> currentParameters,
                        List<ParametersAction> parentParameters) {

        def (newParameters, removedParameters, modifiedParameters) = calculateChanges(currentParameters, parentParameters);

        l.raw(" ")

        if (!newParameters.isEmpty()) {
            addImage(l,"svn_added.png")
        }

        if (!removedParameters.isEmpty()) {
            addImage(l,"svn_deleted.png")
        }

        if (!modifiedParameters.isEmpty()) {
            addImage(l,"svn_modified.png")
        }
    }

    def renderContent(l, newParams, removedParams, modifiedParams) {

        def output = new ByteArrayOutputStream()
        def xmlOutput = XMLOutput.createXMLOutput(output)

        //make a back up of output
        def realXmlOutput = l.output

        //hijack xml output so that we can generate a string, even via stapler etc..
        l.output = xmlOutput
        this.init.content.output = xmlOutput

        l.table(width: "100%") {
            renderParamsCollection(l, "Added", "svn_added.png", newParams)
            renderParamsCollection(l, "Removed", "svn_deleted.png", removedParams)
            renderParamsCollection(l, "Modified", "svn_modified.png", modifiedParams)
        }

        xmlOutput.close()

        //restore xml output
        this.init.content.output = realXmlOutput
        l.output = realXmlOutput

        return new String(output.toByteArray())
    }

    private void renderParamsCollection(l,title,image,pvs) {
        if (!pvs.isEmpty()) {
            l.tr() {
                l.td(colspan: "6") {
                    l.h3(title)
                }
            }
            pvs.each { param ->
                l.tr() {
                    if (param instanceof Collection) {
                        l.td() { addImage(l, image) }

                        l.td() { l.text(param[0].name) }
                        l.td() { renderParamValue(l, param[0]) }

                        l.td() { addImage(l,"me.png") }

                        l.td() { l.text(param[1].name) }
                        l.td() { renderParamValue(l, param[1]) }
                    }
                    else {
                        l.td(colspan:"2") { addImage(l, image) }
                        l.td(colspan:"2") { l.text(param.name) }
                        l.td(colspan:"2") { renderParamValue(l, param) }
                    }
                }
            }
        }
    }

    Map cellMetadata(BuildStreamTreeEntry entry) {
        counter--

        if (entry instanceof BuildStreamTreeEntry.BuildEntry) {
            def (currentParameters, parentParameters) = calculateParameters(entry)
            def (newParameters, removedParameters, modifiedParameters) = calculateChanges(currentParameters, parentParameters);

            def isChanged = !newParameters.isEmpty() || !removedParameters.isEmpty() || !modifiedParameters.isEmpty()

            def tdAttributes = [data: counter--]
            if (isChanged) {
                tdAttributes.put("tooltip", renderContent(lHelper, newParameters, removedParameters, modifiedParameters))
                tdAttributes.put("nodismiss", "")
            }

            return tdAttributes
        }

        else  {
            return [data: counter--]
        }

    }

    def calculateParameters(buildEntry) {

        def currentParameters = buildEntry.run.details.parameterActions

        def parentBuildEntry = this.init.findTreeNodeForBuildEntry(buildEntry).parent?.value
        def parentParameters = parentBuildEntry instanceof BuildStreamTreeEntry.BuildEntry ?
            parentbuildEntry?.run?.details?.parameterActions:
            null

        return [currentParameters, parentParameters]
    }

    @Override
    void render(JenkinsLikeXmlHelper l, BuildStreamTreeEntry.BuildEntry buildEntry) {

        def (currentParameters, parentParameters) = calculateParameters(buildEntry)
        renderParamDiff(l,
                currentParameters,
                parentParameters)
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
