package org.smartfrog.test.unit.sfcore.languages.cdl;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smartfrog.services.xml.utils.ResourceLoader;
import org.smartfrog.sfcore.languages.cdl.CdlCatalog;
import org.smartfrog.sfcore.languages.cdl.CdlParser;
import org.smartfrog.sfcore.languages.cdl.ParseContext;
import org.smartfrog.sfcore.languages.cdl.dom.CdlDocument;
import org.smartfrog.sfcore.languages.cdl.dom.PropertyList;
import org.smartfrog.sfcore.languages.cdl.faults.CdlException;
import org.smartfrog.sfcore.languages.cdl.faults.CdlXmlParsingException;
import org.smartfrog.test.SmartFrogTestBase;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;

/**
 * this is the testbase for the tests that load files
 */
public abstract class XmlTestBase extends SmartFrogTestBase
        implements Filenames {

    protected Log log = LogFactory.getLog(this.getClass());

    /**
     * This is our list of valid documents. These must all parse
     */
    protected final static String[] VALID_CDL = {
        CDL_DOC_MINIMAL,
        CDL_DOC_EXTRA_ELEMENTS,
        CDL_DOC_WEBSERVER,
        CDL_DOC_WEBSERVER_NO_NAMESPACE,
        CDL_DOC_WEBSERVER_DEFAULT_NAMESPACE,
        CDL_DOC_DOCUMENTED,
        CDL_DOC_REFERENCES_1,
        CDL_DOC_REFERENCES_2,
        CDL_DOC_REFERENCES_3,
        CDL_DOC_EXPRESSION_1,
        CDL_DOC_TYPE_1,
        CDL_DOC_LAZY_1,
        CDL_DOC_LAZY_2,
        CDL_DOC_PARAMETERIZATION_1,
        CDL_DOC_FULL_EXAMPLE_1,
        CDL_DOC_FULL_EXAMPLE_2,
        CDL_DOC_FULL_EXAMPLE_3,
    };


    protected CdlParser parser;

    boolean PARSER_MUST_VALIDATE = true;
    //public static final boolean PARSER_MUST_VALIDATE = false;

    /**
     * Constructs a test case with the given name.
     */
    protected XmlTestBase(String name) {
        super(name);
    }

    /**
     * create a new catalog, using the local classloader for resolution
     *
     * @return
     */
    protected CdlCatalog createCatalog() {
        ResourceLoader loader;
        loader = new ResourceLoader(this.getClass());
        return new CdlCatalog(loader);
    }

    private void loading(String filename) {
        log(filename);
    }

    protected void log(String message) {
        log.info(message);
    }

    protected CdlDocument load(String filename) throws IOException,
            ParsingException, CdlException {
        CdlDocument doc;
        loading(filename);
        doc = parser.parseResource(filename);
        return doc;
    }

    /**
     * configure the parser
     *
     * @throws SAXException
     */
    protected void initParser() throws SAXException {
        ResourceLoader loader = new ResourceLoader(this.getClass());
        parser = new CdlParser(loader, PARSER_MUST_VALIDATE);
    }

    /**
     * Sets up the fixture by initialising the parser
     */
    protected void setUp() throws Exception {
        super.setUp();
        initParser();
    }

    /**
     * assert that a resource loads as invalid CDL
     *
     * @param resource resource to load
     * @param text     text to look for in the exception (ParsingException or
     *                 CdlParsingException only)
     * @throws Exception for any other type of exception thrown during
     *                   load/parse
     */
    protected void assertInvalidCDL(String resource, String text)
            throws Exception {
        ParseContext context = new ParseContext();
        assertInvalidCDL(context, resource, text);
    }

    /**
     * assert that a resource loads as invalid CDL
     *
     * @param resource resource to load
     * @param text     text to look for in the exception (ParsingException or
     *                 CdlParsingException only)
     * @throws Exception for any other type of exception thrown during
     *                   load/parse
     */
    protected void assertInvalidCDL(ParseContext context,
            String resource,
            String text) throws Exception {
        try {
            if (text == null) {
                text = "";
            }
            CdlDocument doc = load(resource);
            doc.parse(context);
            fail("expected a validity failure with " + text);
        } catch (Exception e) {
            if (e.getMessage().indexOf(text) < 0) {
                log("expected [" + text + "] but got " + e.toString());
                throw e;
            }
        }
    }

    /**
     * load a file; assert that it is valid. This parses the doc as well as
     * loading it.
     *
     * @param resource
     * @throws IOException
     * @throws ParsingException
     * @throws CdlXmlParsingException
     */
    protected void assertValidCDL(String resource) throws IOException,
            ParsingException, CdlException {
        ParseContext context = new ParseContext();
        parseValidCDL(context, resource);
    }

    /**
     * load a file; assert that it is valid by initiating parsing
     *
     * @param context
     * @param resource
     * @return a parsed document
     * @throws IOException
     * @throws ParsingException
     * @throws CdlException
     */
    protected CdlDocument parseValidCDL(ParseContext context, String resource)
            throws IOException,
            ParsingException, CdlException {
        CdlDocument cdlDocument = loadValidCDL(resource);
        cdlDocument.parse(context);
        return cdlDocument;
    }

    /**
     * load a file; assert that it is valid by initiating parsing
     *
     * @param resource
     * @return a parsed document
     * @throws IOException
     * @throws ParsingException
     * @throws CdlException
     */
    protected CdlDocument parseValidCDL(String resource)
            throws IOException,
            ParsingException, CdlException {
        ParseContext context = new ParseContext();
        CdlDocument cdlDocument = loadValidCDL(resource);
        cdlDocument.parse(context);
        return cdlDocument;
    }


    /**
     * Load a valid CDL document
     *
     * @param resource
     * @return
     */
    protected CdlDocument loadValidCDL(String resource) throws IOException,
            ParsingException, CdlException {
        CdlDocument doc = load(resource);
        return doc;
    }

    /**
     * Load a valid CDL document and build the DOM, but do not apply any other
     * phases
     *
     * @param resource
     * @return the document, built up but without imports, expands or other
     *         operations
     */
    protected CdlDocument loadCDLToDOM(String resource) throws IOException,
            ParsingException, CdlException {
        CdlDocument doc = load(resource);
        ParseContext context = new ParseContext();
        doc.setParseContext(context);
        doc.parsePhaseBuildDom();
        return doc;
    }

    /**
     * Assert that a template has an attribute
     *
     * @param template
     * @param local
     */
    protected void assertHasAttribute(PropertyList template, String local) {
        assertHasAttribute(template, "", local);
    }

    /**
     * Assert that a template has an attribute
     *
     * @param template
     * @param local
     */
    protected void assertHasAttribute(PropertyList template,
            String namespace,
            String local) {
        assertTrue("Template " + template + " lacks the attribute " + local,
                template.hasAttribute(namespace, local));
    }

    /**
     * Assert that a template has an attribute of a given value
     *
     * @param template
     * @param namespace
     * @param local
     * @param expected
     */
    protected void assertAttributeValueEquals(PropertyList template,
            String namespace,
            String local,
            String expected) {
        Attribute attribute = template.getAttribute(local, namespace);
        assertNotNull("Template " + template + " lacks the attribute " + local,
                attribute);
        assertEquals(expected, attribute.getValue());
    }

    /**
     * Assert that a template has an attribute of a given value
     *
     * @param template
     * @param local
     * @param expected
     */
    protected void assertAttributeValueEquals(PropertyList template,
            String local,
            String expected) {
        assertAttributeValueEquals(template, "", local, expected);
    }

    public void assertElementValueEquals(Element e, String value) {
        assertNotNull("Null element", e);
        String actual = e.getValue();
        assertEquals("Element " +
                e +
                " has value [" +
                actual +
                "] and not the expected value [" + value + "]",
                value, actual);
    }

    protected void assertElementTextContains(Element element,
            String search) {
        String value = element.getValue();
        assertTrue("Not found: [" + search + "] in [" + value + "]",
                value.indexOf(search) >= 0);
    }

    /**
     * looku up a component; thro
     *
     * @param doc
     * @param localname
     */
    protected PropertyList lookup(CdlDocument doc, String localname) {
        PropertyList template = doc.lookup(new QName(localname));
        assertNotNull("Lookup failed for element name " + localname, template);
        return template;
    }
}
