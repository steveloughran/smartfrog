<!-- /**
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
-->
<%-- $Id: SFActions.jsp 81 2006-05-30 06:09:38Z uppada $ --%>
<%@ page language="java" %>

<%@	page import="org.smartfrog.avalanche.core.module.*"%>
<%@	page import="org.smartfrog.avalanche.server.modules.*"%>
<%@	page import="org.smartfrog.avalanche.server.*"%>
<%@	page import="org.smartfrog.avalanche.settings.xdefault.*"%>
<%@	page import="org.smartfrog.avalanche.settings.sfConfig.*"%>

<%@ include file="InitBeans.jsp" %>

<%
  	String errMsg = null; 
  	ModulesManager manager = factory.getModulesManager();
  	SettingsManager settingsMgr = factory.getSettingsManager();
  	SettingsType defSettings = settingsMgr.getDefaultSettings();
  	SfConfigsType configs = settingsMgr.getSFConfigs();  
  	
  	if( null == manager ){
  		errMsg = "Error connecting to manager database" ;
  		throw new Exception ( "Error connecting to manager database" );
  	}

%>

<!DOCTYPE HTML PUBLIC "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
<%@ include file="common.jsp" %>
<style type="text/css">
a{
text-decoration:none;
}
</style>
</head>

<script language="javascript">
function toggle(divId) {
    var state = document.getElementById(divId).style.display ;
    if ( state == "none" )
    {
        document.getElementById(divId).style.display = "block";
    }else{
        document.getElementById(divId).style.display = "none";
    }
}  

function submit(formId, target){
    var form = document.getElementById(formId);
    form.action = target ;
    form.submit();
}
</script>

<body>
<script>
setNextSubtitle("Supported Actions Page");
</script>

<form name="actionListFrm" id='actionListFrm' method="post">
<br/>
<div align="center">
<center>
<!-- This is the page menu -->
<div align="center" style="width: 95%;">
    <script>
      oneVoiceWritePageMenu("SFActions","header");
    </script>
</div>

<%@ include file="Message.jsp" %>

<!-- Actual Body starts here -->
<table  border="0" cellpadding="0" cellspacing="0" class="dataTable tableHasCheckboxes" id="actionListTable">
  <thead>
    <tr class="captionRow">
      <th class="checkboxCell"><input type="checkbox" tableid="actionListTable"></th>
      <th>Title</th>
      <th>Action Type</th>
    </tr>
  </thead>
  <tbody>
<%
    String rowClass = "";
    SfDescriptionType []descs = configs.getSfDescriptionArray();
    for( int i=0;i<descs.length;i++){
      rowClass = rowClass == "" ? "class='altRowColor'" : "";
%>
    <tr <%=rowClass %>>
      <td class="checkboxCell"><input type="checkbox" rowselector="yes"
	name="selectedAction" value="<%=descs[i].getTitle()%>"></input>
      </td>
      <td>
	<a href="SFActionArgs.jsp?title=<%=descs[i].getTitle()%>">
	  <%=descs[i].getTitle()%>
	</a>
      </td>
      <td> <%=descs[i].getAction()%></td>
    </tr>
<%
	}
%>
  </tbody>
</table>
<br/>
<div align="center" style="width: 95%;">
  <script>
      oneVoiceWritePageMenu("SFActions","footer",
	"Delete Selected Actions","javascript:submit('actionListFrm','SaveSFAction.jsp?pageAction=delAction')",
	"Add an Action","javascript:toggle('newActionDiv')"
      );
  </script>
</div>

</center>
</div> 
</form>


<div id='newActionDiv' style="display:none">
<center>
<form id='newActionFrm' name='newActionFrm' method='post' action="SaveSFAction.jsp?pageAction=addAction">
  <table id="newActionListTable" border="0" cellpadding="0" cellspacing="0" class="dataTable">
    <caption>New Action</caption>
    <tbody>
      <tr>
	<td class="medium" align="right">Title:</th>
	<td class="editaleFieldCell">
	  <input type="text" name="title" size="50">
	</td>
      </tr>
      <tr>
	<td class="medium" align="right">URL:</th>
	<td class="editableFieldcell">
	  <input type="text" name="url" size="50">
	</td>
      </tr>
      <tr>
	<td class="medium" align="right">Action:</th>
	<td class="editableFieldCell">
	  <select name="sfAction">
<%
	  SettingsType.Action []actionOpts = defSettings.getActionArray();
	  for( int i = 0;i<actionOpts.length;i++){
%>
	    <option><%=actionOpts[i].getName()%></option>
<%
	  }
%>	
	  </select>
	</td>
      </tr>
    </tbody>
  </table>
<br/>
<div align="center" style="width: 95%;">
<div class="buttonSet">
  <div class="bWrapperUp"><div><div>
    <input class="hpButton" type="submit" name="Save" value="Save Action"></input>
  </div></div></div>
</div>
<div class="clearFloats"></div>
</div>
</form>
</center>
<script language="JavaScript" type="text/javascript">
        reconcileEventHandlers();
</script>
</body>

</html>
