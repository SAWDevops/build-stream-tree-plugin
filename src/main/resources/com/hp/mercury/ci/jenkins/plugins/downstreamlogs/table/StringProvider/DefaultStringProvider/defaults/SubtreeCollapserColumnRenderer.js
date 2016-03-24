/**
 *
 * THIS BIT OF CODE HANDLES COOKIES
 * from: https://developer.mozilla.org/en-US/docs/Web/API/document.cookie
 *
 */

/*\
 |*|
 |*|  :: cookies.js ::
 |*|
 |*|  A complete cookies reader/writer framework with full unicode support.
 |*|
 |*|  https://developer.mozilla.org/en-US/docs/DOM/document.cookie
 |*|
 |*|  This framework is released under the GNU Public License, version 3 or later.
 |*|  http://www.gnu.org/licenses/gpl-3.0-standalone.html
 |*|
 |*|  Syntaxes:
 |*|
 |*|  * docCookies.setItem(name, value[, end[, path[, domain[, secure]]]])
 |*|  * docCookies.getItem(name)
 |*|  * docCookies.removeItem(name[, path], domain)
 |*|  * docCookies.hasItem(name)
 |*|  * docCookies.keys()
 |*|
 \*/
var COUNTER_TD_CLASS = "counterTd";
var TABLE_ROW_CLASS = "tableRow";
var EXPAND_COLLAPSE_DIV_CLASS = "expandCollapseDiv";
var cookiePrefix = 'buildStreamTree-State-Collapsed-';
var docCookies = {
    getItem: function (sKey) {
        return decodeURIComponent(document.cookie.replace(new RegExp("(?:(?:^|.*;)\\s*" + encodeURIComponent(sKey).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\=\\s*([^;]*).*$)|^.*$"), "$1")) || null;
    },
    setItem: function (sKey, sValue, vEnd, sPath, sDomain, bSecure) {
        if (!sKey || /^(?:expires|max\-age|path|domain|secure)$/i.test(sKey)) {
            return false;
        }
        var sExpires = "";
        if (vEnd) {
            switch (vEnd.constructor) {
                case Number:
                    sExpires = vEnd === Infinity ? "; expires=Fri, 31 Dec 9999 23:59:59 GMT" : "; max-age=" + vEnd;
                    break;
                case String:
                    sExpires = "; expires=" + vEnd;
                    break;
                case Date:
                    sExpires = "; expires=" + vEnd.toUTCString();
                    break;
            }
        }
        document.cookie = encodeURIComponent(sKey) + "=" + encodeURIComponent(sValue) + sExpires + (sDomain ? "; domain=" + sDomain : "") + (sPath ? "; path=" + sPath : "") + (bSecure ? "; secure" : "");
        return true;
    },
    removeItem: function (sKey, sPath, sDomain) {
        if (!sKey || !this.hasItem(sKey)) {
            return false;
        }
        document.cookie = encodeURIComponent(sKey) + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT" + ( sDomain ? "; domain=" + sDomain : "") + ( sPath ? "; path=" + sPath : "");
        return true;
    },
    hasItem: function (sKey) {
        return (new RegExp("(?:^|;\\s*)" + encodeURIComponent(sKey).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\=")).test(document.cookie);
    },
    keys: /* optional method: you can safely remove it! */ function () {
        var aKeys = document.cookie.replace(/((?:^|\s*;)[^\=]+)(?=;|$)|^\s*|\s*(?:\=[^;]*)?(?:\1|$)/g, "").split(/\s*(?:\=[^;]*)?;\s*/);
        for (var nIdx = 0; nIdx < aKeys.length; nIdx++) {
            aKeys[nIdx] = decodeURIComponent(aKeys[nIdx]);
        }
        return aKeys;
    }
};


/**
 * try to make the build-stream-tree table not resize when you click on the +/- buttons
 */
fixBuildStreamTreeTableWidth = function (done) {
    return function () {
        //fix table width after initial rendering
        if (!done) {

            done = true;

            var widthProperty = "clientWidth";

            var buildStreamTreeTable = document.getElementById("build-stream-tree-table");

            var headers = buildStreamTreeTable.getElementsByTagName("th");
            var headersLength = headers.length;
            for (var i = 0; i < headersLength; i++) {
                var header = headers[i];
                header.style.width = header[widthProperty] + "px";
            }

            buildStreamTreeTable.style.width = buildStreamTreeTable[widthProperty] + "px";
          //  buildStreamTreeTable.style.tableLayout = "fixed";
        }
    }
}(false);

//plusMinusDiv xpath is //*[@id="build-stream-tree-table"]/tbody/tr[4]/td[1]/div
function expandCollapse(plusMinusDiv) {

    var newImage = "plus.gif";
    var newVisibility = "none";

    var newExpanded = "false";

    if (plusMinusDiv.getAttribute("expanded") === "false") {
        newImage = "minus.gif";
        newVisibility = "";
        newExpanded = "true";
    }

    doExpandCollapse(plusMinusDiv, newImage, newVisibility, newExpanded, true)
}

function expand(plusMinusDiv, unselect) {

    newImage = "minus.gif";
    newVisibility = "";
    newExpanded = "true";
    doExpandCollapse(plusMinusDiv, newImage, newVisibility, newExpanded, unselect)
}

function collapse(plusMinusDiv, unselect) {

    var newImage = "plus.gif";
    var newVisibility = "none";
    var newExpanded = "false";
    doExpandCollapse(plusMinusDiv, newImage, newVisibility, newExpanded, unselect)
}

function doExpandCollapse(plusMinusDiv, newImage, newVisibility, newExpanded, unselect) {

    //make width not change when we hide rows
   fixBuildStreamTreeTableWidth();

    //function from abstract pipeline additional top layout
    if (unselect && (typeof unselectAbstractPipeline == 'function')) {
        unselectAbstractPipeline();
    }

    plusMinusDiv.setAttribute("expanded", newExpanded);

    var img = plusMinusDiv.getElementsByTagName("img")[0];
    img.src = img.src.substring(0, img.src.lastIndexOf('/') + 1) + newImage;

    var prefix = plusMinusDiv.parentNode.getAttribute("prefix");

    var jobName = plusMinusDiv.parentNode.getAttribute("jobName");
    var cookieCollapseState = cookiePrefix + jobName;

    var locationIndex = plusMinusDiv.parentNode.cellIndex;
    var tr = plusMinusDiv.parentNode.parentNode;

    var rows = tr.parentNode.rows;
    if (newExpanded === "true") {
        docCookies.setItem(cookieCollapseState, "false", null, "/");
        var trPrefix = getTrPrefixByPlusMinusDiv(plusMinusDiv);
        expandChildRows(trPrefix);
    }
    else {
        docCookies.setItem(cookieCollapseState, "true", null, "/");
        var trPrefix = getTrPrefixByPlusMinusDiv(plusMinusDiv);
        collapseChildRows(trPrefix);
    }

//    for (var i = tr.rowIndex + 1; i < rows.length; i++) {
//
//        var row = rows[i];
//        var td = row.cells[locationIndex];
//        var location = td.getAttribute("prefix");
//
//        if (!prefix) {
//            continue;
//        }
//
//        if (location.indexOf(prefix) == 0) {
//
//            var divs = td.getElementsByTagName("div");
//            var div = divs[0];
//
//            if (div) {
//                var currentJobName = div.parentNode.getAttribute("jobName");
//                collapseExpandIfNeeded(div, currentJobName)
//                div.setAttribute("expanded", newExpanded);
//                div.getElementsByTagName("img")[0].src = img.src;
//            }
//
//            row.style.display = newVisibility;
//        }
//
//        else {
//            break;
//        }
//    }
}

//collapse all child rows according to the prefix of the row that was clicked
function collapseChildRows(prefix) {
    var allChildrenRows = getAllChildrenRows(prefix);
    for (i = 0; i < allChildrenRows.length; i++) {
        var plusMinusDiv = getPlusMinusDivFromTd(allChildrenRows[i]);
        var tr;
        if (typeof plusMinusDiv == "undefined") {
            tr = getTrFromTd(allChildrenRows[i]);
        }
        else {
            tr = getTrFromPlusMinusDiv(plusMinusDiv);
        }
        //skip current row
        if (getTrPrefixByTd(allChildrenRows[i]) == prefix) {
            continue;
        }
        tr.style.display = "none";
    }
}

//show all child rows that have a parent row which is displayed and exapnded
function expandChildRows(prefix) {
    var allChildrenRows = getAllChildrenRows(prefix);
    for (i = 0; i < allChildrenRows.length; i++) {
        var td = allChildrenRows[i];
        console.info("expanding row with prefix " + prefix);
        var currentPrefix = getTrPrefixByTd(td);
        console.info("checking if need to expand row with prefix " + currentPrefix);
        //skip current row
        if (currentPrefix == prefix) {
            continue;
        }
        var tr;
        var parentRowDisplayedState = getParentRowDisplayedState(currentPrefix);
        var parentRowExpandedStateByTd = getParentRowExpandedStateByTd(td);

        console.info("parentRowDisplayedState= " + parentRowDisplayedState);
        console.info("parentRowExpandedStateByTd= " + parentRowExpandedStateByTd);

        if (parentRowDisplayedState != "display: none;" && parentRowExpandedStateByTd === "true") {
            console.info("Chagning tr to be visible")
            tr = getTrFromTd(allChildrenRows[i]);
            tr.style.display = "";
        }
    }
}

function changeRowDisplay(tr, plusMinusDiv) {
    if (getParentRowExpandedState(plusMinusDiv) == "true") {
        tr.style.display = "true"
    }
    else {
        tr.style.display = "false"
    }
}

function getParentRowExpandedStateByPlusMinusDiv(plusMinusDiv) {
    var currentLevel = getTrPrefixByPlusMinusDiv(plusMinusDiv);
    return getParentRowExpandedState(currentLevel);
}

function getParentRowExpandedStateByTd(td) {
    var currentLevel = getTrPrefixByTd(td);
    return getParentRowExpandedState(currentLevel);
}

function getParentRowExpandedState(currentLevel) {
    if (currentLevel != null && !currentLevel.empty()) {
        var parentLevel = getParentLevel(currentLevel);
        var parentTd = getTdByPrefixValue(parentLevel);
        var parentPlusMinusDiv = getPlusMinusDivFromTd(parentTd);
        var expandedState = parentPlusMinusDiv.getAttribute("expanded");
        return expandedState;
    }
    return true;
}

function getParentRowAttributeValue(currentLevel, attributeName) {
    if (currentLevel != null && !currentLevel.empty()) {
        var parentLevel = getParentLevel(currentLevel);
        var parentTd = getTdByClassAndPrefix(COUNTER_TD_CLASS, parentLevel);
        var parentTr = getTrFromTd(parentTd);
        var attributeValue = parentTr.getAttribute(attributeName);
        return attributeValue;
    }
    console.warn("Tried to get attribute " + attributeName + " from parent row with an illegal level " + currentLevel);
    return true;
}

function getParentLevel(currentLevel) {
    var tempLevel = currentLevel.substring(0, currentLevel.length - 1);
    return tempLevel.substring(0, tempLevel.lastIndexOf(".") + 1);
}

function getParentRowDisplayedState(currentLevel) {
    return getParentRowAttributeValue(currentLevel, "style");
}

function getAllChildrenRows(prefix) {
    return document.querySelectorAll("*[prefix^=\'" + prefix + "\']");
}

function getDirectChildrenRows(prefix) {
    var nodeList = document.querySelectorAll("*[prefix^=\'" + prefix + "\']");
    var childList;
    for (i = 0; i < nodeList.length; i++) {
        if (nodeList[i].getAttribute("prefix").length == prefix.length + 2) {

            childList.push(nodeList[i]);
        }
    }

    return childList;
}

function getTrPrefixByPlusMinusDiv(plusMinusDiv) {
    return plusMinusDiv.parentNode.getAttribute("prefix");
}

function getTrPrefixByTd(td) {
    return td.getAttribute("prefix");
}

function getTdByPrefixValue(prefix) {
    var td = Array.prototype.slice.call(document.querySelectorAll("*[prefix=\'" + prefix + "\']"));
    return td[0];
}

function getTdByClassAndPrefix(className, prefix) {
    tds = Array.prototype.slice.call(document.getElementsByClassName(className));
    var correctTd = tds.filter(function(el) { return el.getAttribute("prefix")===prefix});
    if (correctTd == null) {
        console.warn("Cannot find td element with className " + className + " and prefix " + prefix);
        return correctTd;
    }
    return correctTd[0];
}

function getPlusMinusDivFromTd(td) {
    var divs;
    try {
        divs = td.getElementsByTagName("div");
    } catch (err) {
        divs = td[0].getElementsByTagName("div");
    }

    return divs[0]
}

function getTrFromPlusMinusDiv(plusMinusDiv) {
    return plusMinusDiv.parentNode.parentNode;
}

function getTrFromTd(td) {
    return td.parentNode;
}

function getTrFromTdByClassName(td, trClassName) {
    tr = td.closest('.' + trClassName);
    console.info("Get tr with class name " + trClassName + " for td " + td + ". tr is: " + tr);
    return tr;

}

function collapseExpandByCookieState(div, jobName) {
    if (docCookies.getItem(cookiePrefix + jobName) == "true") {
        collapse(div);
    }
    else if (docCookies.getItem(cookiePrefix + jobName) == "false") {
        expand(div);
    }
}

function collapseExpandByTreeDepth(div) {
    var tableElement = document.getElementById("build-stream-tree-table");
    var treeDepth = tableElement.getAttribute("depth");
    var rowDepth = div.getAttribute("nodeDepth")
    if (rowDepth > treeDepth) {
        collapse(div)
    }
    else {
        expand(div)
    }
}

function collapseExpandIfNeeded(div, jobName) {
    if (docCookies.hasItem(cookiePrefix + jobName)) {
        collapseExpandByCookieState(div, jobName)
    }
    else {
        collapseExpandByTreeDepth(div)
    }
}

var toType = function (obj) {
    return ({}).toString.call(obj).match(/\s([a-zA-Z]+)/)[1].toLowerCase()
}

//this javascript is loaded after the table is loaded, so all the elements are ready and we can collapse/uncollapse according to what we stored in memory...
window.setTimeout(function () {

    var i;
    var cell;

    var buildStreamTreeTable = document.getElementById("build-stream-tree-table");
    var rows = buildStreamTreeTable.rows;
    var firstRow = rows[1];
    if (firstRow) {

        var indexOfCollapseColumn = -1;

        var firstRowCells = firstRow.cells;
        var firstRowCellsCount = firstRowCells.length;
        for (i = 0; i < firstRowCellsCount; i++) {
            cell = firstRowCells[i];
            if (cell.getAttribute('prefix')) {
                indexOfCollapseColumn = i;
                break;
            }
        }

        if (indexOfCollapseColumn > -1) {

            var rowsLength = rows.length;
            for (var rowIdx = 1; rowIdx < rowsLength; rowIdx++) {

                var row = rows[rowIdx];
                var collapseTd = row.cells[indexOfCollapseColumn];
                var jobName = collapseTd.getAttribute('jobName');
                var div = collapseTd.childNodes[0];

//                if (docCookies.hasItem(cookiePrefix + jobName)) {
//                    collapse(div);
//                }
                if (docCookies.hasItem(cookiePrefix + jobName)) {
                    collapseExpandByCookieState(div, jobName);
                }
            }
        }
    }

}, 100);





