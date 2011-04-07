/*
 * Created on Dec 9, 2010
 */
package com.example.tutorial.jaxb;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A converter to make XSD date/time datatypes friendlier for java.
 * 
 * @author Ross M. Lodge
 */
public class DateConverter
{
    
    private static final Logger log = LoggerFactory.getLogger(DateConverter.class);

    /**
     * Parses the xsd:date object
     * 
     * @param xmlRepresentation
     * @return
     */
    public static Date parseDate(String xmlRepresentation)
    {
        if (StringUtils.isBlank(xmlRepresentation))
        {
            return null;
        }
        return DatatypeConverter.parseDate(xmlRepresentation).getTime();
    }

    /**
     * Prints the xsd:date object
     * 
     * @param date
     * @return
     */
    public static String printDate(Date date)
    {
        if (date == null)
        {
            return null;
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        return DatatypeConverter.printDate(cal);
    }

    /**
     * Parses the xsd:date object
     * 
     * @param xmlRepresentation
     * @return
     */
    public static Calendar parseDateCalendar(String xmlRepresentation)
    {
        if (StringUtils.isBlank(xmlRepresentation))
        {
            return null;
        }
        return DatatypeConverter.parseDate(xmlRepresentation);
    }

    /**
     * Prints the xsd:date object
     * 
     * @param cal
     * @return
     */
    public static String printDateCalendar(Calendar cal)
    {
        if (cal == null)
        {
            return null;
        }
        return DatatypeConverter.printDate(cal);
    }

    /**
     * Parses the xsd:dateTime object
     * 
     * @param xmlRepresentation
     * @return
     */
    public static Date parseDateTime(String xmlRepresentation)
    {
        if (StringUtils.isBlank(xmlRepresentation))
        {
            return null;
        }
        return DatatypeConverter.parseDateTime(xmlRepresentation).getTime();
    }

    /**
     * Prints the xsd:dateTime object
     * 
     * @param date
     * @return
     */
    public static String printDateTime(Date date)
    {
        if (date == null)
        {
            return null;
        }
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        return DatatypeConverter.printDateTime(cal);
    }

    /**
     * Parses the xsd:dateTime object
     * 
     * @param xmlRepresentation
     * @return
     */
    public static Calendar parseDateTimeCalendar(String xmlRepresentation)
    {
        if (StringUtils.isBlank(xmlRepresentation))
        {
            return null;
        }
        return DatatypeConverter.parseDateTime(xmlRepresentation);
    }

    /**
     * Prints the xsd:dateTime object
     * 
     * @param cal
     * @return
     */
    public static String printDateTimeCalendar(Calendar cal)
    {
        if (cal == null)
        {
            return null;
        }
        return DatatypeConverter.printDateTime(cal);
    }

    /**
     * <p>Parses the xsd:duration object.
     * 
     * <p><strong>Note:</strong> This may be inaccurate, particularly for large durations, since
     * it uses the the current date as the base for the duration.  This may be in accurate because
     * lengths of months, etc. vary.  To that end we recommend you do not convert durations
     * automatically but do them manually with the {@link #parseDuration(String, Date)} method.  On
     * the other hand this is not likely to have consequences in this application.
     * 
     * @param xmlRepresentation
     * @return
     */
    public static Long parseDuration(String xmlRepresentation)
    {
        if (StringUtils.isBlank(xmlRepresentation))
        {
            return null;
        }
        Date startInstant = new Date();
        return parseDuration(xmlRepresentation, startInstant);
    }

    /**
     * <p>Parses the xsd:duration object.
     * 
     * @param xmlRepresentation
     * @param startInstant The time the duration is supposed to start.  This matters because
     * months, years, etc. have different lengths.
     * @return
     */
    public static Long parseDuration(String xmlRepresentation, Date startInstant)
    {
        if (StringUtils.isBlank(xmlRepresentation))
        {
            return null;
        }
        return getDatatypeFactory().newDuration(xmlRepresentation).getTimeInMillis(startInstant);
    }

    /**
     * Prints the xsd:duration object
     * 
     * @param milliseconds
     * @return
     */
    public static String printDuration(Long milliseconds)
    {
        if (milliseconds == null)
        {
            return null;
        }
        Duration duration = getDatatypeFactory().newDuration(milliseconds);
        return duration.toString();
    }

    /**
     * @return
     * @throws DatatypeConfigurationException
     */
    private static DatatypeFactory getDatatypeFactory()
    {
        try
        {
            return DatatypeFactory.newInstance();
        }
        catch (DatatypeConfigurationException e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }

}
