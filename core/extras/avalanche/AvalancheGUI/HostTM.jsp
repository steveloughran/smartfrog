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
<%@ page language="java" %>

<%@	page import="org.smartfrog.avalanche.settings.xdefault.*"%>
<%@	page import="org.smartfrog.avalanche.core.module.*"%>
<%@	page import="org.smartfrog.avalanche.server.*"%>
<%@	page import="org.smartfrog.avalanche.core.host.*"%>
  	
<%@ include file="InitBeans.jsp" %>  	
<%
    String errMsg = null; 
    HostManager manager = factory.getHostManager();
    
    if( null == manager ){
	errMsg = "Error connecting to hosts database" ;
	throw new Exception ( "Error connecting to hosts database" );
    }
    
    SettingsManager settingsMgr = factory.getSettingsManager();
    SettingsType defSettings = settingsMgr.getDefaultSettings();  
    
    SettingsType.DataTransferMode sysTransferModes[] =
		 defSettings.getDataTransferModeArray();
    
    String hostId = request.getParameter("hostId");
    String os = null; 
    String plaf = null ;
    String arch = null ;
    
    HostType host = manager.getHost(hostId);
    if( null != hostId ){
	if( null != host ){
	    PlatformSelectorType ps = host.getPlatformSelector();
	    if( null != ps ){
		os   = ps.getOs();
		plaf = ps.getPlatform();
		arch = ps.getArch();
	    }
	}
    }

    String modeStr = "" ;
    for( int i=0; i < sysTransferModes.length; i++){
	if ( i != sysTransferModes.length-1 )
	    modeStr += "\""+sysTransferModes[i].getName() + "\""+",";
	else
	    modeStr += "\"" +sysTransferModes[i].getName() +"\"";
    }
	
%>

<!DOCTYPE HTML PUBLIC "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
<%@ include file="common.jsp" %>
</head>

<script language="javascript">

function submit(target){
    document.addHostFrm.action = target ;
    var hostId = <%=(hostId!=null)?("\""+hostId+"\""):null%> ;
    if( hostId != null )
	document.addHostFrm.action = target + "&&hostId=" + hostId ;

    document.addHostFrm.submit();
}

function addRowInTable(table)
{
    var modes = new Array (<%=modeStr%>);
    var len = table.rows.length ;

    var newRow = document.createElement("tr");
    var idx = "" + (len + 1) ;

    var col1 = document.createElement("td");
    col1.setAttribute('class', 'medium');
    col1.setAttribute('align', 'center');

    var d2 = document.createElement("select");
    d2.setAttribute('name', 'mode.type.' + idx );

    for( var i=0; i < modes.length; i++){
	var opt = document.createElement("option");
	opt.appendChild(document.createTextNode(modes[i]) );
	d2.appendChild(opt)
    }
    col1.appendChild(d2);

    var col2 = document.createElement("td");
    col2.setAttribute('class', 'medium');
    var d3 = document.createElement("input");
    d3.setAttribute('type', 'text');
    d3.setAttribute('name', 'mode.userName.' + idx);
    col2.appendChild(d3);

    var col3 = document.createElement("td");
    col3.setAttribute('class', 'medium');
    var d4 = document.createElement("input");
    d4.setAttribute('type', 'password');
    d4.setAttribute('name', 'mode.password.' + idx);
    col3.appendChild(d4);

    var col4 = document.createElement("td");
    col4.setAttribute('class', 'medium');
    col4.setAttribute('align', 'center');
    var d5 = document.createElement("input");
    d5.setAttribute('type', 'radio');
    d5.setAttribute('name', 'defaultAccessMode');
    col4.appendChild(d5);

    newRow.appendChild(col1);
    newRow.appendChild(col2);  
    newRow.appendChild(col3);  
    newRow.appendChild(col4);  
    table.getElementsByTagName("tbody")[0].appendChild(newRow);
}

</script>

<body>
<script>
setNextSubtitle("Host Transfer Modes Page");
</script>

<form id='addHostFrm' name='addHostFrm' method='post' action='SaveHost1.jsp?action=tm&&next=props&&hostId=<%=hostId %>'>

<!-- This is the page menu -->
<br>
<div align="center" style="width: 95%;">
  <script>
    oneVoiceWritePageMenu("HostTM","header",
      "Host Properties",
	    "javascript:submit('SaveHost1.jsp?action=tm&&next=props')",
      "Transfer Modes",
	    "",
      "Access Modes",
	    "javascript:submit('SaveHost1.jsp?action=tm&&next=am')",
      "Basic Settings",
	    "javascript:submit('SaveHost1.jsp?action=tm&&next=bs')"
    );
  </script>
</div>

<!-- Actual Body starts here -->
<br/>
<center>

<table id="transferModeTable" class="dataTable" 
	style="width: 500px; border-collapse: collapse">
    <caption>Transfer modes for host <%=hostId %></caption>
<tbody>
    <tr>
	<td class="medium"> Transfer Mode </td>
	<td class="medium"> User Name </td>
	<td class="medium"> Password </td>
	<td class="medium"> Default </td>
    </tr>	
<%
    HostType.TransferModes transferModes = host.getTransferModes();
		
    if (null != transferModes){
	DataTransferModeType modes[] = transferModes.getModeArray();
	for( int i=0;i<modes.length;i++){
	    String modeName = modes[i].getType();
	    String userName = modes[i].getUser();
	    String password = modes[i].getPassword();
%>

    <tr>
	<td class="medium" align="center">
	    <select name="<%=("mode.type." + i)%>">
<%
	    for( int j=0;j<sysTransferModes.length;j++){
%>	
		<option<%=(sysTransferModes[j].getName().equals(modeName)) ?
		    " selected" : ""%>><%=sysTransferModes[j].getName()%>
		</option>
<%
	    }
%>
	    </select>
	</td>
	<td class="medium">
	    <input name="<%=("mode.userName." + i)%>" type="text" value="<%=userName%>">
	</td>
	<td class="medium">
	    <input name="<%=("mode.password." + i)%>" type="password" value="<%=password%>">
	</td>
	<td class="medium" align="center">
<%
	    if( modes[i].getIsDefault() ){
%>		
	    <p><input type="radio" value="<%=modeName%>" name="defaultTransferMode" checked></p>
<%
	    } else {
%>
	    <input type="radio" value="<%=modeName%>" name="defaultTransferMode">
<%
	    }
%>
	</td>
    </tr>

<%
        }
    }
%>
</tbody>
</table>

<br/>
<input type='button' value='Add a Transfer Mode' class="btn" 
	onclick="javascript:addRowInTable(getElementById('transferModeTable'))">
<input type='submit' name='save' value='Save Changes' class="btn">
</center>
</form>

<script language="JavaScript" type="text/javascript">
        reconcileEventHandlers();
</script>
</body>

</html>
