/*
 * Created on Dec 10, 2010
 */
package com.example.tutorial.jaxb;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A JAXB Converter for URI values.
 *
 * @author Ross M. Lodge
 */
public class UriConverter
{
    
    /**
     * Parses the URI object
     * 
     * @param xmlRepresentation
     * @return
     */
    public static URI parseUri(String xmlRepresentation)
    {
        try
        {
            return xmlRepresentation != null && !"".equals(xmlRepresentation) ? new URI(xmlRepresentation) : null;
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException("Cannot convert URI as specified: " + xmlRepresentation, e);
        }
    }

    /**
     * Prints the URI object
     * 
     * @param uri
     * @return
     */
    public static String printUri(URI uri)
    {
        return uri != null ? uri.toString() : null;
    }

}
