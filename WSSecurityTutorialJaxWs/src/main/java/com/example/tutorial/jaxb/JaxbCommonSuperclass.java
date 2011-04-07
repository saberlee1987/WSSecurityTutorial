/*
 * Created on Dec 15, 2010
 */
package com.example.tutorial.jaxb;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * A common superclass (we're only allowed to specify one, for all generated classes!). Provides toString functionality.
 * 
 * @author Ross M. Lodge
 */
public class JaxbCommonSuperclass
{

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        try
        {
            return ReflectionToStringBuilder.toString(this);
        }
        catch (Exception e)
        {
            //Suppress
        }
        return super.toString();
    }

}
