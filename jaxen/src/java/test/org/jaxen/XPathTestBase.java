
package org.jaxen;

import org.jaxen.function.StringFunction;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

public abstract class XPathTestBase extends TestCase
{
    protected static String TESTS_XML = "xml/test/tests.xml";

    protected static boolean            verbose         = true;
    private          SAXReader          xmlReader       = new SAXReader();
    private          ContextSupport     contextSupport;

    private Stack executionContext = new Stack();

    public XPathTestBase(String name)
    {
        super( name );
    }

    public void setUp()
    {
        this.contextSupport = null;
    }

    public void tearDown()
    {

    }

    public void log(String text)
    {
        log( this.verbose,
             text );
    }

    public void log(boolean actualVerbose,
                    String text)
    {
        if ( ! actualVerbose )
        {
            return;
        }
            
        System.err.println( text );
    }

    public void testXPaths() throws Exception
    {
        log( "Loading test file: " + TESTS_XML );
        
        Document doc = xmlReader.read( TESTS_XML );

        Element root = doc.getRootElement();

        Iterator iter = root.elementIterator( "document" );

        Element eachDocElem = null;

        while ( iter.hasNext() )
        {
            eachDocElem = (Element) iter.next();

            testDocument( eachDocElem );
        }
    }

    protected void testDocument(Element docElem) throws Exception
    {
        String url = docElem.attributeValue( "url" );

        Object testDoc = getDocument( url );

        log( "-----------------------------" );
        log( "Document [" + url + "]" );

        this.executionContext.push( url );

        Iterator iter    = docElem.elementIterator( "context" );
        Element  context = null;

        while ( iter.hasNext() )
        {
            context = (Element) iter.next();

            testContext( testDoc,
                         context );
        }

        this.executionContext.pop();

        log( "-----------------------------" );
    }

    protected void testContext(Object testDoc,
                               Element contextElem) throws Exception
    {
        String xpathStr = contextElem.attributeValue( "select" );

        log( "Initial Context :: " + xpathStr );

        this.executionContext.push( xpathStr );

        JaXPath xpath = new JaXPath( xpathStr );

        List list = xpath.jaSelectNodes( getContext( testDoc ) );

        Iterator iter = list.iterator();
        Object   contextNode = null;

        while ( iter.hasNext() )
        {
            contextNode = iter.next();

            runTests( testDoc,
                      contextElem,
                      contextNode );
        }

        this.executionContext.pop();
    }

    protected void runTests(Object testDoc,
                            Element contextElem,
                            Object context) throws Exception
    {
        Iterator iter = contextElem.elementIterator( "test" );

        Element test = null;

        while ( iter.hasNext() )
        {
            test = (Element) iter.next();

            runTest( testDoc,
                     context,
                     test );
        }

        iter = contextElem.elementIterator( "valueOf" );

        Element valueOf = null;

        while ( iter.hasNext() )
        {
            valueOf = (Element) iter.next();

            testValueOf( testDoc,
                         context,
                         valueOf );
        }
    }

    protected void runTest(Object testDoc,
                           Object context,
                           Element test) throws Exception
    {
        String xpathStr = test.attributeValue( "select" );

        String debugStr  = test.attributeValue( "debug" );

        boolean debug = this.verbose;

        if ( debugStr != null )
        {
            if ( "true".equals( debugStr )
                 ||
                 "on".equals( debugStr ) )
            {
                debug = true;
            }
        }

        log( debug,
             "  Select :: " + xpathStr );

        this.executionContext.push( xpathStr );

        String count = test.attributeValue( "count" );

        JaXPath xpath = new JaXPath( xpathStr );

        if ( count != null )
        {
            int expectedSize = Integer.parseInt( count );

            try
            {
                List results = xpath.jaSelectNodes( getContext( context ) );
                
                log ( debug,
                      "    Expected Size :: " + expectedSize );
                log ( debug,
                      "    Result Size   :: " + results.size() );
                
                if ( expectedSize != results.size() )
                {
                    log ( debug,
                          "      ## FAILED" );
                    log ( debug,
                          "      ## xpath: " + xpath );
                    
                    Iterator resultIter = results.iterator();
                    
                    while ( resultIter.hasNext() )
                    {
                        log ( debug,
                              "      --> " + resultIter.next() );
                    }
                }
                assertEquals( this.executionContext.toString(),
                              expectedSize,
                              results.size() );
            }
            catch (UnsupportedAxisException e)
            {
                log ( debug,
                      "      ## SKIPPED -- Unsupported Axis" );
            }
        }

        Element valueOf = test.element( "valueOf" );

        if ( valueOf != null )
        {
            debugStr = valueOf.attributeValue( "debug" );
            
            if ( debugStr != null )
            {
                if ( "true".equals( debugStr )
                     ||
                     "on".equals( debugStr ) )
                {
                    debug = true;
                }
            }
            
            try
            {
                Object newContext = xpath.jaSelectSingleNode( getContext( context ) );
                
                log ( debug,
                      "    New Context :: " + abbreviate( newContext ) );
                
                
                String valueOfXPathStr = valueOf.attributeValue( "select" );
                
                log( debug,
                     "  Select :: " + valueOfXPathStr );

                this.executionContext.push( valueOfXPathStr );
                
                JaXPath valueOfXPath = new JaXPath( valueOfXPathStr );
                
                Object node = valueOfXPath.jaSelectSingleNode( getContext( newContext ) );
                
                String expected = valueOf.getText();
                String result =   StringFunction.evaluate( node,
                                                           getNavigator() );

                log ( debug,
                      "    Expected :: " + expected );
                
                log ( debug,
                      "    Result   :: " + result );

                if ( ! expected.equals( result ) )
                {
                    log ( debug,
                          "      ## FAILED" );
                }
                
                assertEquals( this.executionContext.toString(),
                              expected,
                              result );
            }
            catch (UnsupportedAxisException e)
            {
                log ( debug,
                      "      ## SKIPPED -- Unsupported Axis" );
                
            }

            this.executionContext.pop();
        }
        
        this.executionContext.pop();
    }

    protected void testValueOf(Object testDoc,
                               Object context,
                               Element valueOf) throws Exception
    {
        String xpathStr = valueOf.attributeValue( "select" );

        String debugStr  = valueOf.attributeValue( "debug" );

        boolean debug = this.verbose;

        if ( debugStr != null )
        {
            if ( "true".equals( debugStr )
                 ||
                 "on".equals( debugStr ) )
            {
                debug = true;
            }
        }

        JaXPath xpath = new JaXPath( xpathStr );

        this.executionContext.push( xpathStr );

        try
        {
            Object node = xpath.jaSelectSingleNode( getContext( context ) );
            
            String expected = valueOf.getText();
            String result = StringFunction.evaluate( node,
                                                     getNavigator() );
            
            log ( debug,
                  "  Select :: " + xpathStr );
            log ( debug,
                  "    Expected :: " + expected );
            log ( debug,
                  "    Result   :: " + result );
            
            if ( ! expected.equals( result ) )
            {
                log ( debug,
                      "      ## FAILED" );
                log ( debug,
                      "      ## xpath: " + xpath );
            }
            
            assertEquals( this.executionContext.toString(),
                          expected,
                          result );
        }
        catch (UnsupportedAxisException e)
        {
            log ( debug,
                  "      ## SKIPPED -- Unsupported Axis " );

        }

        this.executionContext.pop();
    }
                               
    protected Context getContext(Object contextNode)
    {
        Context context = new Context( getContextSupport() );

        context.setNodeSet( Collections.singletonList( contextNode ) );

        return context;
    }

    public ContextSupport getContextSupport()
    {
        if ( this.contextSupport == null )
        {
            Navigator nav = getNavigator();

            this.contextSupport = new ContextSupport( new SimpleNamespaceContext(),
                                                      XPathFunctionContext.getInstance(),
                                                      new SimpleVariableContext(),
                                                      getNavigator() );
        }

        return this.contextSupport;
    }

    public abstract Navigator getNavigator();

    public abstract Object getDocument(String url) throws Exception;

    private String abbreviate(Object obj)
    {
        String str = obj.toString();

        int nl = str.indexOf( "\n" );

        if ( nl >= 0
             &&
             nl < 80 )
        {
            return str.substring( 0, nl );
        }

        if ( str.length() < 80 )
        {
            return str;
        }

        return str.substring(0, 80) + "...";
    }
        
}