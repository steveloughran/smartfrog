/*
(C) Copyright 1998-2007 Hewlett-Packard Development Company, LP

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

For more information: www.smartfrog.org
*/

function TreeTableManager() {

}


TreeTableManager.prototype.init = function() {
	var tableRows = document.getElementsByTagName("tr");

	for (var i=0;i<tableRows.length;i++) {		
		// we only care about tr elements with these classnames
		
		if (tableRows[i].className.indexOf == "treeTableTitleRow"!=-1) {
			var tree = tableRows[i];
			var childDivs = tree.getElementsByTagName("DIV");
			var treeControl = null;
			for (var j=0;j<childDivs.length;j++) {
				
				
				if (
						(childDivs[j].nodeType==1) && (childDivs[j].className =="treeControl")) treeControl = childDivs[j];
			}
			var additionalOnClick;
			if (treeControl) {
				if (treeControl.onclick) {
					additionalOnClick = treeControl.onclick;
					eval("treeControl.onclick = function(mozEvent) {var hardCodedFunction = "+treeControl.onclick+" ;ourTreeTableManager.toggleTableTree(mozEvent,this);hardCodedFunction();};");
				}
				else {
					eval("treeControl.onclick = function(mozEvent) {ourTreeTableManager.toggleTableTree(mozEvent,this);};");
				}
			}
		}
	}
}

// uses the classnames to hold open closed state. 
TreeTableManager.prototype.toggleTableTree = function(mozEvent,treeControl) {
	
	var titleRowObject = treeControl;
	while((titleRowObject) && (titleRowObject.tagName!="TR")) {
		titleRowObject = titleRowObject.parentNode;
	}
	// obtain the id, which we will use to compare against parentid's, to determine parent-child relations
	var titleRowId = titleRowObject.id;
	titleRowId= titleRowId.substring(2,titleRowId.length);

	var tableObject = titleRowObject.parentNode;
	// watch out for tbodys, which might be sitting in between.
	if (tableObject.tagName !="TABLE") tableObject = tableObject.parentNode;
	var allTableRows = tableObject.getElementsByTagName("TR");

	if (titleRowObject.className.indexOf("treeTableTitleRowOpen") != -1) {
		titleRowObject.className = titleRowObject.className.replace("treeTableTitleRowOpen","treeTableTitleRowClosed"); 
		for (var i=0;i<allTableRows.length;i++) {
			var parentId;
			if (parentId = allTableRows[i].getAttribute("parentid")) {
				
				if (parentId.indexOf(titleRowId)!=-1) {  // then we have a parent-child relationship
					allTableRows[i].style.display= "none";
					
				}
			}
		}
	}
	else {
		
		titleRowObject.className = titleRowObject.className.replace("treeTableTitleRowClosed","treeTableTitleRowOpen"); 
		for (var i=0;i<allTableRows.length;i++) {
			var parentId;
			if (parentId = allTableRows[i].getAttribute("parentid")) {
				if (parentId.indexOf(titleRowId)!=-1) {  // then we have a parent-child relationship

					// HOWEVER, if there is any other parent-child relationship under which the row is still closed, we'll have to be careful....
					
					// consider row tc_4567_7890.  if i initiate close on parent 7890,  but 4567 is still closed, dont display.
					var allParentIds = allTableRows[i].getAttribute("parentid").split("_");
					var foundACloserParentThatIsStillClosed = false;
					for (var j=0; j<allParentIds.length; j++) {
						if ((allParentIds[j]!="tc") && (document.getElementById("tc_"+allParentIds[j]).className=="treeTableTitleRowClosed")) {
							foundACloserParentThatIsStillClosed = true;
						}
					}
					if (!foundACloserParentThatIsStillClosed) {
						allTableRows[i].style.cssText= "display:table-row";
					}
				}
			}
		}
	}
}


