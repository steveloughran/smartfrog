<%--
/** (C) Copyright 2009 Hewlett-Packard Development Company, LP

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

For more information: www.smartfrog.org

*/
--%>

<%@ include file="/html/mombasa-portlet/cluster/init.jsp" %>


<p>Add Hadooop hosts </p>

<html:form action="/mombasa-portlet/cluster/add_dynamic/process" method="post" focus="minWorkers">


  <table border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td>
        Minimum number of workers
      </td>
      <td style="padding-left: 10px;"></td>
      <td>
        <html:text name="clusterAddDynamicForm" property="minWorkers" size="23"/>
      </td>
    </tr>

    <tr>
      <td>
        Maximum of workers
      </td>
      <td style="padding-left: 10px;"></td>
      <td>
        <html:text name="clusterAddDynamicForm" property="maxWorkers" size="23"/>
      </td>
    </tr>
    
 <%--   <tr>
      <td>
        Large size
      </td>
      <td style="padding-left: 10px;"></td>
      <td>
        <html:checkbox name="clusterAddDynamicForm" property="large" />
      </td>
    </tr>--%>


    <tr>
      <td>
        <html:submit>
          Add
        </html:submit>
      </td>
      <td style="padding-left: 10px;"></td>
      <td>
        <html:reset>
          Reset
        </html:reset>
      </td>
    </tr>

  </table>


</html:form>




