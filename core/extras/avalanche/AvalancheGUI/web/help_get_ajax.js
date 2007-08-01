/**
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

var help_response = false;
var help_xml = getXMLHttpRequestObject();

function getHelp(pageId) {
    if (help_xml) {
        help_xml.open("GET", "help_get.jsp?id=" + pageId + "&now=" + (new Date).getMilliseconds());
        help_xml.onreadystatechange = function()
        {
            try {
            // If everything went alright
            if (help_xml.status == 200) {
                if (help_xml.readyState == 4) {
                    var xmlDocument = help_xml.responseXML;
                    help_response = "<h2>Help</h2><p>" + xmlDocument.getElementsByTagName("message")[0].childNodes[0].nodeValue + "</p>";
                } else {
                    help_response = "<center><img style=\"vertical-align:middle\" src=\"images/help_loader.gif\" alt=\"Loading\" />Loading...</center>";
                }
            } else {
                help_response = "<p>Help is currently unavailable.</p>";
            }
            } catch (e) {
                help_response = "<p>Server is currently unavailable. </p>";
            }
            fillDivBox("helptext", help_response);
        }
        sendRequest(help_xml)
    }
}