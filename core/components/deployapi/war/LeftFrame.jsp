<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<%
/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*
*/

/**
 * Author : Deepal Jayasinghe
 * Date: May 26, 2005
 * Time: 7:14:26 PM
 */
%>
<head>
<title>Untitled Document</title>
<style type="text/css">
</style></head>

<body>
<table width="100%">

 <tr> <tr>
     <td colspan="2" >
      <b> System Components</b>
     </td>
  </tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td >
      <a href="listService" target="mainFrame">Available Services</a>
    </td>
 </tr>
 <tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td >
      <a href="listModules" target="mainFrame">Available Modules</a>
    </td>
 </tr>
 <tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td>
      <a href="globalModules" target="mainFrame">Globally Engaged Modules</a>
    </td>
 </tr>
 <tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td >
      <a href="listPhases" target="mainFrame">Available Phases</a>
    </td>
 </tr>
 <tr>
    <td colspan="2">
     <br>
    </td>
 </tr>
  <tr>
     <td colspan="2" >
       <b>Execution Chains</b>
     </td>
  </tr>
   <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="viewGlobalHandlers" target="mainFrame">Global Chains</a>
       </td>
    </tr>
    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="selectService" target="mainFrame">Operation's Chains</a>
       </td>
    </tr>
    <tr>
       <td colspan="2">
        <br>
      </td>
   </tr>
    <tr>
     <td colspan="2" >
       <b>Engage Module</b>
     </td>
  </tr>
   <tr>
       <td>
        &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="engagingglobally" target="mainFrame">Gloabally</a>
       </td>
    </tr>

    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="engageToService" target="mainFrame">To a Service</a>
       </td>
    </tr>

     <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="listoperation" target="mainFrame">To an Operation</a>
       </td>
    </tr>
    <tr>
      <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
        &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
    </tr>

    <tr>
     <td colspan="2" >
       <b>Edit Service</b>
     </td>
  </tr>
    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="removeService" target="mainFrame">Turn off service</a>
       </td>
    </tr>
    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="selectServiceParaEdit" target="mainFrame">Edit Service Parameters</a>
       </td>
    </tr>

     <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
        <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
    </tr>
    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="index.jsp" target="_parent" >Back</a>
       </td>
    </tr>
</table>
</body>
</html>
