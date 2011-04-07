package org.springframework.beans.factory.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * <p>
 * A jndi-aware extension of the Spring {@link PropertyPlaceholderConfigurer} (v3.0.5). By default this resolves properties from
 * jndi first, then any referenced property files and then falls back to System properties and the System Environment.
 * </p>
 * <p>
 * It also performs property expansion on values passed in as Locations in case the resource locations themselves have
 * placeHolder values that need to be resolved from jndi or system properties
 * </p>
 * <p>
 * The behavior of this class is controlled, in addition to the standard {@link PropertyPlaceholderConfigurer} configuration,
 * by the {@link #searchJndiEnvironment} flag and the {@link #jndiSystemOrder} property.  The {@link #searchJndiEnvironment}
 * flag turns off searching JNDI values, although why you would want to use this class without that is beyond me.  The {@link #jndiSystemOrder}
 * property specifies whether JNDI or SYSTEM properties are read first, and thus take priority.  These work in <em>conjunction</em> with
 * the standard systemPropertiesMode property which specifies whether JNDI/SYSTEM properties override anything specified in the properties
 * files or whether they are only used as a fallback.
 * </p>
 * <p>
 * An example usage in which we have two property files referenced via a configDirectory parameter injected as a jndi or
 * system property
 * </p>
 * 
 * <pre>
 *  &lt;!--
 *   Expose jndi, system and config properties to bean definitions. This expects a jndi or system
 *   property configDirectory to our directory of configuration files
 *  --&gt;
 *  &lt;bean
 *    id=&quot;propertyPlaceholderConfigurer&quot;
 *    class=&quot;org.springframework.beans.factory.config.JndiAwarePropertyPlaceholderConfigurer&quot;
 *    init-method=&quot;initialize&quot;&gt;
 *    &lt;property
 *      name=&quot;locations&quot;&gt;
 *      &lt;list&gt;
 *        &lt;value&gt;file:${configDirectory}/../common.properties
 *        &lt;/value&gt;
 *        &lt;value&gt;file:${configDirectory}/application.properties
 *        &lt;/value&gt;
 *      &lt;/list&gt;
 *    &lt;/property&gt;
 *  &lt;/bean&gt;
 * </pre>
 */
public class JndiAwarePropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean
{
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Indicating whether JNDI or System properties are used first.
     *
     * @author Ross M. Lodge
     */
    public static enum JndiSystemOrder {
        /**
         * When this is the value, JNDI values will be checked first
         * and only if a JNDI value is not found will System properties be used.
         */
        JNDI_FIRST,
        /**
         * When this is the value, System values will be checked first
         * and only if a System value is not found will JNDI be used.  This is the default.
         */
        SYSTEM_FIRST;
    }
    
    /**
     * if searchJndiEnvironment is set to true then we will search the jndi environment for properties
     */
    private boolean searchJndiEnvironment = true;

    private Resource[] tempLocations;
    
    private JndiSystemOrder jndiSystemOrder = JndiSystemOrder.SYSTEM_FIRST;

    /**
     * Takes an array or Resources and for any of type UrlResource, resolves any properties in the URL. Note that as we
     * haven't loaded the property fiels at this stage we are resolving properties against the system and jndi sets (if
     * any)
     * 
     * @param locations
     */
    @SuppressWarnings("deprecation")
    private void processLocationValues(Resource[] locations)
    {
        if (locations != null)
        {
            Properties props = new Properties();
            HashSet visitedPlaceholders = new HashSet();
            for (int i = 0; i < locations.length; i++)
            {
                if (locations[i] instanceof UrlResource)
                {
                    UrlResource file = (UrlResource)locations[i];
                    String path;
                    try
                    {
                        path = file.getURL().toString();
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                    String value = parseStringValue(path, props, visitedPlaceholders);
                    if (!StringUtils.equals(path, value))
                    {
                        UrlResource newFile;
                        try
                        {
                            newFile = new UrlResource(value);
                        }
                        catch (MalformedURLException e)
                        {
                            throw new RuntimeException(e);
                        }
                        locations[i] = newFile;
                    }
                }
            }
        }
    }

    /**
     * Resolve the given placeholder using the given properties. Default implementation simply checks for an environment
     * entry for a corresponding property key.
     * <p>
     * Subclasses can override this for customized placeholder-to-key mappings or custom resolution strategies, possibly
     * just using the given lookup as a fallback.
     * 
     * @param placeholder the placeholder to resolve
     * @return the resolved value, of <code>null</code> if none
     */
    protected String resolveJndiProperty(String placeholder)
    {
        InitialContext initialContext = null;
        try
        {
            initialContext = new InitialContext();
            try
            {
                return (String)initialContext.lookup("java:comp/env/" + placeholder);
            }
            catch (NameNotFoundException e)
            {
                return null;
            }
            catch (NamingException e)
            {
                return null;
            }
        }
        catch (NamingException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (initialContext != null)
            {
                try
                {
                    initialContext.close();
                }
                catch (NamingException e)
                {
                    //Do nothing
                }
            }
        }
    }

    /**
     * Override of PropertyPlaceholderConfigurer.resolvePlaceholder to handle jndi property lookup.
     * <p>
     * Warning: note that we are directly accessing the instance variable jndiPropertiesMode rather than accepting it as
     * an input parameter (simply to avoid having to rewrite the calling method)
     * </p>
     * <p>
     * The override/fallback mode of jndi properties relative to property-file values can be controlled by setting the
     * systemPropertyMode property; precedence of JNDI and System properties is controlled by {@link #jndiSystemOrder};
     */
    @Override
    protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode)
    {
        String propVal = null;
        if (systemPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE)
        {
            propVal = getJndiAndSystemValues(placeholder);
        }
        if (propVal == null)
        {
            propVal = resolvePlaceholder(placeholder, props);
        }
        if (propVal == null && systemPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE)
        {
            propVal = getJndiAndSystemValues(placeholder);
        }
        return propVal;
    }
    
    /**
     * Gets the JNDI and System values according to the specified order.
     * 
     * @return
     */
    private String getJndiAndSystemValues(String placeholder)
    {
        String propValue = null;
        if (jndiSystemOrder == JndiSystemOrder.JNDI_FIRST)
        {
            if (searchJndiEnvironment)
            {
                propValue = resolveJndiProperty(placeholder);
                if (propValue != null && log.isDebugEnabled())
                {
                    log.debug("Retrieved JNDI property {}={}", placeholder, propValue);
                }
            }
            if (propValue == null)
            {
                propValue = resolveSystemProperty(placeholder);
                if (propValue != null && log.isDebugEnabled())
                {
                    log.debug("Retrieved System property {}={}", placeholder, propValue);
                }
            }
        }
        else if (jndiSystemOrder == JndiSystemOrder.SYSTEM_FIRST)
        {
            propValue = resolveSystemProperty(placeholder);
            if (propValue != null && log.isDebugEnabled())
            {
                log.debug("Retrieved System property {}={}", placeholder, propValue);
            }
            if (propValue == null && searchJndiEnvironment)
            {
                propValue = resolveJndiProperty(placeholder);
                if (propValue != null && log.isDebugEnabled())
                {
                    log.debug("Retrieved JNDI property {}={}", placeholder, propValue);
                }
            }
        }
        return propValue;
    }

    /**
     * Set a location of a properties file to be loaded.
     * <p>
     * Can point to a classic properties file or to an XML file that follows JDK 1.5's properties XML format.
     */
    @Override
    public void setLocation(Resource location)
    {
        this.tempLocations = new Resource[] { location };
    }

    /**
     * Set locations of properties files to be loaded.
     * <p>
     * Can point to classic properties files or to XML files that follow JDK 1.5's properties XML format.
     * <p>
     * Note: Properties defined in later files will override properties defined earlier files, in case of overlapping
     * keys. Hence, make sure that the most specific files are the last ones in the given list of locations.
     */
    @Override
    public void setLocations(Resource[] locations)
    {
        tempLocations = locations;
    }

    /**
     * @param searchJndiEnvironment
     */
    public void setSearchJndiEnvironment(boolean searchJndiEnvironment)
    {
        this.searchJndiEnvironment = searchJndiEnvironment;
    }

    /**
     * @return the jndiSystemOrder
     */
    public JndiSystemOrder getJndiSystemOrder()
    {
        return jndiSystemOrder;
    }

    /**
     * @param jndiSystemOrder the jndiSystemOrder to set
     */
    public void setJndiSystemOrder(JndiSystemOrder jndiSystemOrder)
    {
        this.jndiSystemOrder = jndiSystemOrder;
    }
    
    /**
     * resolve locations and let our superclass know about them
     */
    private void initialize()
    {
        processLocationValues(this.tempLocations);
        super.setLocations(tempLocations);
    }

    /** 
     * {@inheritDoc}
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        initialize();
    }

}
