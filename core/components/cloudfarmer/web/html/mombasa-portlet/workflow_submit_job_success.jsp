<%!
  /**
   Report successful job submission
   */
%>

<%@ include file="/html/mombasa-portlet/init.jsp" %>
<jsp:include page="/html/mombasa-portlet/header.jsp"/>
<h2>Workflow Queued</h2>


<table border="2">
  <tr>
    <td>Name</td>
    <td><bean:write name="workflowInstance" property="name"/></td>
  </tr>
  <tr>
    <td>Description</td>
    <td><bean:write name="workflowInstance" property="description"/></td>
  </tr>

  <tr>
    <td>Classname</td>
    <td><bean:write name="workflowInstance" property="classname"/></td>
  </tr>
</table>


<jsp:include page="/html/mombasa-portlet/footer.jsp"/>


