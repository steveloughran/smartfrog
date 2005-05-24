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


import org.smartfrog.services.xml.utils.ParserHelper;
import org.smartfrog.services.xml.utils.ResourceLoader;
import org.smartfrog.sfcore.languages.cdl.dom.CdlDocument;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.ParsingException;

/**
 * JDom based utility to parse CDL files. created Jul 1, 2004 1:49:31 PM
 */

public class CdlParser {

    /**
     * logic for resource loading
     */
    private ResourceLoader resourceLoader;


    /**
     * create a parser;
     *
     * @param loader   resource loader algorithm
     * @param validate validation logic.
     */
/*    public CdlParser(ResourceLoader loader, boolean validate)
            throws SAXException {

        assert loader!=null:"null ResourceLoader";
        resourceLoader = loader;
        //we mandate Xerces, as the others cannot handle schema so well
        //XMLReader xerces = ParserHelper.createXmlParser(validate,true,true);

        builder = new SAXBuilder();
        JDomHelper.configureSaxBuilder(builder, validate, true,true);
        if (validate) {
            CdlCatalog resolver = new CdlCatalog(loader);
            resolver.bind(builder);
        }
    }*/


    /**
     * builder class
     */
    private Builder builder;

    /**
     * create a parser;
     *
     * @param loader   resource loader algorithm
     * @param validate validation logic.
     */
    public CdlParser(ResourceLoader loader, boolean validate)
            throws SAXException {
        resourceLoader = loader;
        //we mandate Xerces, as the others cannot handle schema so well
        XMLReader xerces = ParserHelper.createXmlParser(validate,true,true);

        if (validate) {
            CdlCatalog resolver = new CdlCatalog(loader);
            try {
                resolver.bind(xerces);
            } catch (IOException e) {
                throw new SAXException(e.getMessage(),e);
            }
        }
        builder = new Builder(xerces, validate);
    }


    /**
     * parse the file, and throw an exception if we couldnt
     *
     * @param filename
     * @return
     * @throws IOException
     * @throws ParsingException
     */
    public CdlDocument parseFile(String filename) throws IOException,
            ParsingException {
        File f = new File(filename);
        return new CdlDocument(builder.build(f));
    }

    /**
     * parse a stream that we are provided
     *
     * @param instream
     * @return
     * @throws IOException
     * @throws ParsingException
     */
    public CdlDocument parseStream(InputStream instream) throws IOException,
            ParsingException {
        nu.xom.Document doc = builder.build(instream);
        return new CdlDocument(doc);
    }

    /**
     * load and parse a resoure through our current resource loader
     *
     * @param resource
     * @return
     * @throws IOException
     * @throws ParsingException
     */
    public CdlDocument parseResource(String resource) throws IOException,
            ParsingException {
        InputStream in = resourceLoader.loadResource(resource);
        return parseStream(in);
    }

    /**
     * Get our resource loader
     * @return
     */
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

}
