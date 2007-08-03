/** (C) Copyright 1998-2007 Hewlett-Packard Development Company, LP

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 For more information: www.smartfrog.org */

// Returns object or false
function getXMLHttpRequestObject() {
    try {
        // If Mozilla, Firefox, Safari
        if (window.XMLHttpRequest && !window.ActiveXObject) {
            var XMLHttpRequestObject = false;
            XMLHttpRequestObject = new XMLHttpRequest();
            return XMLHttpRequestObject;
            // Else if Microsoft Browser
        } else if (window.ActiveXObject) {
            try {
                return (new ActiveXObject("MSXML2.XMLHTTP.3.0"));
            } catch (e) {
                try {
                    return (new ActiveXObject("Msxml2.XMLHTTP"));
                } catch (e) {
                    try {
                        return (new ActiveXObject("Microsoft.XMLHTTP"));
                    } catch (e) {
                        return false;
                    }
                }
            }
        }
    } catch (e) {
        return false;
    }
}

// Sends given XMLHttpRequest, returns true on success.
function sendRequest(XMLHttpRequest) {
    try {
        // If Mozilla, Firefox, Safari
        if (window.XMLHttpRequest) {
            XMLHttpRequest.send(null);
            // Else if Microsoft Browser
        } else if (window.ActiveXObject) {
            XMLHttpRequest.send("");
        }
        return true;
    } catch (e) {
        return false;
    }
}

// Fills a given DIV box with a given text
function fillDivBox(idOfTargetDiv, text) {
    var targetDiv = false;
    targetDiv = document.getElementById(idOfTargetDiv);

    if (targetDiv) {
        // Update only if neccessary
        if (targetDiv.innerHTML != text) {
            targetDiv.innerHTML = text;
        }
    }
}