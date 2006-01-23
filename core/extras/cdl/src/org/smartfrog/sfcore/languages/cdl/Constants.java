/** (C) Copyright 2004-2005 Hewlett-Packard Development Company, LP

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


package org.smartfrog.sfcore.languages.cdl;

import org.ggf.cddlm.generated.api.CddlmConstants;

import javax.xml.namespace.QName;

/**
 * Date: 15-Jul-2004 Time: 22:26:34
 */
public class Constants {

    private Constants() {
    }


    public static final String XML_NAMESPACE_URI="http://www.w3.org/2000/xmlns/";

    public static final String DEPLOY_API_SCHEMA_FILENAME =
            CddlmConstants.CDL_FILENAME_DEPLOYMENT_API;

    public static final String XML_CDL_NAMESPACE = CddlmConstants.XML_CDL_NAMESPACE;

    public static final String DEPLOY_API_TYPES_NAMESPACE = CddlmConstants.CDL_API_TYPES_NAMESPACE;

    public static final String CMP_NAMESPACE = CddlmConstants.CDL_CMP_TYPES_NAMESPACE;

    public static final String CDL_ELT_CDL = "cdl";

    public static final String XPATH_URI = "http://www.w3.org/TR/1999/REC-xpath-19991116";

    public static final String SMARTFROG_NAMESPACE = CddlmConstants.SMARTFROG_NAMESPACE;


    public static final String OPTION_VALIDATE_ONLY = CddlmConstants.OPTION_VALIDATE_ONLY;

    public static final String OPTION_PROPERTIES = CddlmConstants.OPTION_PROPERTIES;

    public static final String SMARTFROG_ELEMENT_NAME = "smartfrog";

    public static final String SMARTFROG_ELEMENT_VERSION_ATTR = "version";

    public static final QName QNAME_CDL_REF = new QName(CddlmConstants.XML_CDL_NAMESPACE,
            CddlmConstants.ATTRIBUTE_REF, "cdl");

    public static final QName QNAME_CDL_REFROOT = new QName(CddlmConstants.XML_CDL_NAMESPACE,
            CddlmConstants.ATTRIBUTE_REFROOT,"cdl");

    public static final QName QNAME_CDL_LAZY = new QName(CddlmConstants.XML_CDL_NAMESPACE,
            CddlmConstants.ATTRIBUTE_LAZY, "cdl");

    /**
     * Completely arbitrary limit on number of steps, used to catch recursion errors
     * {@value}
     */
    public static final int RESOLUTION_PATH_LIMIT = 10000;

    /**
     * Completely arbitrary limit on the depth of nested references, used to catch recursion errors
     * {@value}
     */
    public static final int RESOLUTION_DEPTH_LIMIT = 100;

    /**
     * Completely arbitrary limit on the number of times we iterate through, the graph trying to
     * resolve references. After a while you have to give up.
     * {@value}
     */
    public static final int RESOLUTION_SPIN_LIMIT = 100;

    /**
     * QName used for fault detail containing the current phase
     * {@value}
     */
    public static final QName QNAME_DETAIL_PHASE = new QName(SMARTFROG_NAMESPACE,"phase","sf");

    /**
     * QName used for fault detail containing the current phase
     * {@value}
     */
    public static final QName QNAME_DETAIL_DOCUMENT = new QName(SMARTFROG_NAMESPACE, "phase", "sf");


    /**
     * This is one of those places where bits of the spec can be tuned.
     * Here we set the policy about how to handle nested text/comments/PI in a cdl:ref node,
     * {@value}
     */
    public static final boolean POLICY_NESTED_NODES_FORBIDDEN_IN_REFERENCES=true;

    /**
     * Policy logic: do we extends configs as well as systems. {@value}
     */
    public static final boolean POLICY_ALWAYS_EXTEND_CONFIGURATION = true;

    /**
     * Are we a debug release, in which case there are extra tests in the phases
     * that every step worked
     */
    public static final boolean POLICY_DEBUG_RELEASE = true;
}
