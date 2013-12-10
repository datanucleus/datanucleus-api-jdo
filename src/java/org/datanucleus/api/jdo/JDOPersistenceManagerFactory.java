/**********************************************************************
Copyright (c) 2007 Andy Jefferson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
2008 Craig Russell - AccessController register of JDOStateManagerImpl
2010 Peter Dettman - fixes to synchronising of fetch groups
 	...
 **********************************************************************/
package org.datanucleus.api.jdo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jdo.Constants;
import javax.jdo.FetchGroup;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.DataStoreCache;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.spi.JDOImplHelper;
import javax.jdo.spi.JDOPermission;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ExecutionContext;
import org.datanucleus.NucleusContext;
import org.datanucleus.PersistenceConfiguration;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.metadata.ClassMetadataImpl;
import org.datanucleus.api.jdo.metadata.InterfaceMetadataImpl;
import org.datanucleus.api.jdo.metadata.JDOMetadataImpl;
import org.datanucleus.exceptions.ClassNotResolvedException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.TransactionActiveOnCloseException;
import org.datanucleus.exceptions.TransactionIsolationNotSupportedException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.ClassMetaData;
import org.datanucleus.metadata.FileMetaData;
import org.datanucleus.metadata.InterfaceMetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.metadata.PackageMetaData;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.datanucleus.metadata.TransactionType;
import org.datanucleus.properties.CorePropertyValidator;
import org.datanucleus.query.cache.QueryCompilationCache;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.ConnectionFactory;
import org.datanucleus.store.connection.ConnectionResourceType;
import org.datanucleus.store.query.cache.QueryDatastoreCompilationCache;
import org.datanucleus.store.query.cache.QueryResultsCache;
import org.datanucleus.util.ClassUtils;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * Implementation of a JDO PersistenceManagerFactory, used to obtain PersistenceManager instances.
 * The factory is configurable up to a point when it is frozen. Thereafter nothing can be changed.
 * <p>
 * <b>Serialisation</b><br/>
 * When a PMF is serialised its persistence properties are serialised with it, but dynamic state
 * (fetch groups, listeners, L2 cache, etc) are not serialised with it.
 * </p>
 */
public class JDOPersistenceManagerFactory implements PersistenceManagerFactory, ObjectFactory, Referenceable, Serializable
{
    /** Localisation utility for output messages from jdo. */
    protected static final Localiser LOCALISER = Localiser.getInstance(
        "org.datanucleus.api.jdo.Localisation", JDOPersistenceManagerFactory.class.getClassLoader());

    /** Cache of PMF keyed by the name. Only used when having single-PMF property enabled. */
    private static ConcurrentHashMap<String, JDOPersistenceManagerFactory> pmfByName;

    /** The context that this factory uses. TODO Ought to be serializable, or able to recreate. */
    protected transient NucleusContext nucleusContext;

    /** The cache of PM's in use. */
    private transient Set<JDOPersistenceManager> pmCache = new HashSet<JDOPersistenceManager>();

    /** Lifecycle Listeners. */
    protected transient Map<InstanceLifecycleListener, LifecycleListenerForClass> lifecycleListeners;

    /** Map of user-defined sequences keyed by the factory class name. */
    private transient Map<String, Sequence> sequenceByFactoryClass;

    /** Level 2 Cache. */
    private transient DataStoreCache datastoreCache = null;

    /** Query Results Cache. */
    private transient JDOQueryCache queryCache = null;

    /** JDO Fetch Groups. */
    private transient Set<JDOFetchGroup> jdoFetchGroups = null;

    /** Whether the PersistenceManagerFactory is closed */
    private boolean closed;

    /** Flag for whether this object is still configurable. */
    private boolean configurable = true;

    /**
     * Thread-specific state information (instances of {@link OperationInfo}) for the PM proxy.
     */
    private transient ThreadLocal<PersistenceManager> pmProxyThreadLocal = new InheritableThreadLocal<PersistenceManager>()
    {
        protected PersistenceManager initialValue()
        {
            return null;
        }
    };

    /**
     * Return a new PersistenceManagerFactory with options set according to the given Properties.
     * This method exists for JDO1 compliance whereas in JDO2 the method takes a Map.
     * @param overridingProps The Properties to initialize the PersistenceManagerFactory with.
     * @return A PersistenceManagerFactory with options set according to the given Properties.
     * @see javax.jdo.JDOHelper#getPersistenceManagerFactory(java.util.Map)
     */
    public synchronized static PersistenceManagerFactory getPersistenceManagerFactory(Properties overridingProps)
    {
        // Extract the properties into a Map allowing for a Properties object being used
        Map overridingMap = new HashMap();
        for (Enumeration e = overridingProps.propertyNames() ; e.hasMoreElements() ;)
        {
            // Make sure we handle default properties too (SUN Properties class oddness)
            String param = (String)e.nextElement();
            overridingMap.put(param, overridingProps.getProperty(param));
        }

        // Create the PMF and freeze it (JDO spec $11.7)
        final JDOPersistenceManagerFactory pmf = new JDOPersistenceManagerFactory(overridingMap);
        pmf.freezeConfiguration();

        return pmf;
    }

    /**
     * Return a new PersistenceManagerFactory with options set according to the given Properties.
     * @param overridingProps The Map of properties to initialize the PersistenceManagerFactory with.
     * @return A PersistenceManagerFactory with options set according to the given Properties.
     * @see javax.jdo.JDOHelper#getPersistenceManagerFactory(java.util.Map)
     */
    public synchronized static PersistenceManagerFactory getPersistenceManagerFactory(Map overridingProps)
    {
        // Extract the properties into a Map allowing for a Properties object being used
        Map overridingMap = null;
        if (overridingProps instanceof Properties)
        {
            // Make sure we handle default properties too (SUN Properties class oddness)
            overridingMap = new HashMap();
            for (Enumeration e = ((Properties)overridingProps).propertyNames() ; e.hasMoreElements() ;)
            {
                String param = (String)e.nextElement();
                overridingMap.put(param, ((Properties)overridingProps).getProperty(param));
            }
        }
        else
        {
            overridingMap = overridingProps;
        }

        return createPersistenceManagerFactory(overridingMap);
    }

    /**
     * Return a new PersistenceManagerFactory with options set according to the given properties and
     * given overrides.
     * @param overrides Map of properties to override the supplied props (if any)
     * @param props Map of properties to initialise the PMF with
     * @return A PersistenceManagerFactory with options set according to the given Properties
     */
    public synchronized static PersistenceManagerFactory getPersistenceManagerFactory(Map overrides, Map props)
    {
        // Extract the props into a Map allowing for a Properties object being used
        Map propsMap = null;
        if (props instanceof Properties)
        {
            // Make sure we handle default properties too (SUN Properties class oddness)
            propsMap = new HashMap();
            for (Enumeration e = ((Properties)props).propertyNames() ; e.hasMoreElements() ;)
            {
                String param = (String)e.nextElement();
                propsMap.put(param, ((Properties)props).getProperty(param));
            }
        }
        else
        {
            propsMap = props;
        }

        // Extract the overrides into a Map allowing for a Properties object being used
        Map overridesMap = null;
        if (overrides instanceof Properties)
        {
            // Make sure we handle default properties too (SUN Properties class oddness)
            overridesMap = new HashMap();
            for (Enumeration e = ((Properties)overrides).propertyNames() ; e.hasMoreElements() ;)
            {
                String param = (String)e.nextElement();
                overridesMap.put(param, ((Properties)overrides).getProperty(param));
            }
        }
        else
        {
            overridesMap = overrides;
        }

        // Set the properties of the PMF, taking propsMap+overridesMap
        Map overallMap = null;
        if (propsMap != null)
        {
            overallMap = new HashMap(propsMap);
        }
        else
        {
            overallMap = new HashMap();
        }
        if (overridesMap != null)
        {
            overallMap.putAll(overridesMap);
        }

        return createPersistenceManagerFactory(overallMap);
    }

    /**
     * Convenience method to create the PMF, check whether we should hand out a singleton, and if all
     * ok then freeze it for use.
     * @param props The properties
     * @return The PMF to use
     */
    protected static JDOPersistenceManagerFactory createPersistenceManagerFactory(Map props)
    {
        // Cater for overriding the PMF
        Class pmfClass = null;
        if (props != null && props.containsKey("javax.jdo.PersistenceManagerFactoryClass"))
        {
            String pmfClassName = (String) props.get("javax.jdo.PersistenceManagerFactoryClass");
            if (!pmfClassName.equals(JDOPersistenceManagerFactory.class.getName()))
            {
                try
                {
                    // TODO Use the primaryClassLoader if passes in?
                    pmfClass = Class.forName(pmfClassName);
                }
                catch (ClassNotFoundException e)
                {
                    // Class not found
                }
            }
        }

        // Create the PMF and freeze it (JDO spec $11.7)
        final JDOPersistenceManagerFactory pmf;
        if (pmfClass != null)
        {
            pmf = (JDOPersistenceManagerFactory) ClassUtils.newInstance(pmfClass, new Class[]{Map.class}, new Object[] {props});
        }
        else
        {
            pmf = new JDOPersistenceManagerFactory(props);
        }

        Boolean singleton =
            pmf.getConfiguration().getBooleanObjectProperty("datanucleus.singletonPMFForName");
        if (singleton != null && singleton)
        {
            // Check on singleton pattern. Would be nice to know the name of the PMF before creation
            // but not possible without restructuring parse code, so leave as is for now
            if (pmfByName == null)
            {
                pmfByName = new ConcurrentHashMap<String, JDOPersistenceManagerFactory>();
            }
            String name = pmf.getName();
            if (name == null)
            {
                name = pmf.getPersistenceUnitName();
            }
            if (name != null)
            {
                if (pmfByName.containsKey(name))
                {
                    pmf.close();
                    NucleusLogger.PERSISTENCE.warn("Requested PMF of name \"" + name + 
                        "\" but already exists and using singleton pattern, so returning existing PMF");
                    return pmfByName.get(name);
                }
            }
            pmfByName.putIfAbsent(name, pmf);
        }

        // Freeze the PMF for use (establishes connection to datastore etc)
        pmf.freezeConfiguration();

        return pmf;
    }

    /**
     * Constructs a new JDOPersistenceManagerFactory.
     */
    public JDOPersistenceManagerFactory()
    {
        this(null);
    }

    /**
     * Constructor for a PMF for the specified persistence-unit with optional overriding properties.
     * @param pumd The persistence unit
     * @param overrideProps Properties overriding/supplementing those in the persistence-unit
     */
    public JDOPersistenceManagerFactory(PersistenceUnitMetaData pumd, Map overrideProps)
    {
        // Build up map of all properties to apply (from persistence-unit + overridden + defaulted)
    	Map props = new HashMap();
    	if (pumd != null && pumd.getProperties() != null)
    	{
    	    props.putAll(pumd.getProperties());
    	}
    	if (overrideProps != null)
    	{
    		props.putAll(overrideProps);
    	}
        if (!props.containsKey(PropertyNames.PROPERTY_TRANSACTION_TYPE) && 
            !props.containsKey("javax.jdo.option.TransactionType"))
	    {
    		// Default to RESOURCE_LOCAL txns
    		props.put(PropertyNames.PROPERTY_TRANSACTION_TYPE, TransactionType.RESOURCE_LOCAL.toString());
    	}
    	else
        {
            // let TransactionType.JTA imply ResourceType.JTA
            String transactionType = props.get(PropertyNames.PROPERTY_TRANSACTION_TYPE) != null ? 
                    (String) props.get(PropertyNames.PROPERTY_TRANSACTION_TYPE) : 
                    (String) props.get("javax.jdo.option.TransactionType");
            if (TransactionType.JTA.toString().equalsIgnoreCase(transactionType))
            {
                props.put(ConnectionFactory.DATANUCLEUS_CONNECTION_RESOURCE_TYPE, ConnectionResourceType.JTA.toString());
                props.put(ConnectionFactory.DATANUCLEUS_CONNECTION2_RESOURCE_TYPE, ConnectionResourceType.JTA.toString());
            }
        }

    	// Initialise the context with all properties
        nucleusContext = new NucleusContext("JDO", props);

        initialiseMetaData(pumd);

        // Enable any listeners that are specified via persistence properties
        processLifecycleListenersFromProperties(props);
    }

    /**
     * Constructs a new JDOPersistenceManagerFactory for the specified persistence properties.
     * @param props Persistence properties
     */
    public JDOPersistenceManagerFactory(Map props)
    {
        // Extract any properties that affect NucleusContext startup
        Map startupProps = null;
        if (props != null)
        {
            // Possible properties to check for
            for (String startupPropName : NucleusContext.STARTUP_PROPERTIES)
            {
                if (props.containsKey(startupPropName))
                {
                    if (startupProps == null)
                    {
                        startupProps = new HashMap();
                    }
                    startupProps.put(startupPropName, props.get(startupPropName));
                }
            }
        }

        // Initialise the context for JDO (need nucleusContext to load persistence-unit)
        nucleusContext = new NucleusContext("JDO", startupProps);

        // Generate the properties to apply to the PMF
        Map pmfProps = new HashMap();

        PersistenceUnitMetaData pumd = null;
        if (props != null)
        {
            String persistenceUnitName = (String)props.get(PropertyNames.PROPERTY_PERSISTENCE_UNIT_NAME);
            if (persistenceUnitName == null)
            {
                persistenceUnitName = (String)props.get(Constants.PROPERTY_PERSISTENCE_UNIT_NAME);
            }
            if (persistenceUnitName != null)
            {
                // PMF for a "persistence-unit", so add property so the persistence mechanism knows this
                getConfiguration().setProperty(PropertyNames.PROPERTY_PERSISTENCE_UNIT_NAME, persistenceUnitName);

                try
                {
                    // Obtain any props defined for the persistence-unit
                    pumd = nucleusContext.getMetaDataManager().getMetaDataForPersistenceUnit(persistenceUnitName);
                    if (pumd != null)
                    {
                        // Add the properties for the unit
                        if (pumd.getProperties() != null)
                        {
                            pmfProps.putAll(pumd.getProperties());
                        }
                    }
                    else
                    {
                        throw new JDOUserException(LOCALISER.msg("012004", persistenceUnitName));
                    }
                }
                catch (NucleusException ne)
                {
                    throw new JDOUserException(LOCALISER.msg("012005", persistenceUnitName), ne);
                }
            }
        }

        // Append on any user properties
        if (props != null)
        {
            pmfProps.putAll(props);
            if (!pmfProps.containsKey(PropertyNames.PROPERTY_TRANSACTION_TYPE) &&
                !pmfProps.containsKey("javax.jdo.option.TransactionType"))
            {
                // Default to RESOURCE_LOCAL txns
                pmfProps.put(PropertyNames.PROPERTY_TRANSACTION_TYPE, TransactionType.RESOURCE_LOCAL.toString());
            }
            else
            {
                // let TransactionType.JTA imply ResourceType.JTA
                String transactionType = pmfProps.get(PropertyNames.PROPERTY_TRANSACTION_TYPE) != null ? 
                        (String)pmfProps.get(PropertyNames.PROPERTY_TRANSACTION_TYPE) : 
                        (String)pmfProps.get("javax.jdo.option.TransactionType");
                if (TransactionType.JTA.toString().equalsIgnoreCase(transactionType))
                {
                    pmfProps.put(ConnectionFactory.DATANUCLEUS_CONNECTION_RESOURCE_TYPE,
                        ConnectionResourceType.JTA.toString());
                    pmfProps.put(ConnectionFactory.DATANUCLEUS_CONNECTION2_RESOURCE_TYPE,
                        ConnectionResourceType.JTA.toString());
                }
            }
        }
        else
        {
            pmfProps.put(PropertyNames.PROPERTY_TRANSACTION_TYPE, TransactionType.RESOURCE_LOCAL.toString());
        }

        // Apply the properties to the PMF
        try
        {
            String propsFileProp = PropertyNames.PROPERTY_PROPERTIES_FILE;
            if (pmfProps.containsKey(propsFileProp))
            {
                // Apply properties file first
                getConfiguration().setPropertiesUsingFile((String)pmfProps.get(propsFileProp));
                pmfProps.remove(propsFileProp);
            }
            getConfiguration().setPersistenceProperties(pmfProps);
        }
        catch (IllegalArgumentException iae)
        {
            throw new JDOFatalUserException("Exception thrown setting persistence properties", iae);
        }
        catch (NucleusException jpe)
        {
            // Only throw JDOException and subclasses
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }

        // Initialise any metadata that needs loading + settings
        initialiseMetaData(pumd);

        // Enable any listeners that are specified via persistence properties
        processLifecycleListenersFromProperties(props);
    }

    /**
     * Close this PersistenceManagerFactory. Check for JDOPermission("closePersistenceManagerFactory") 
     * and if not authorized, throw SecurityException.
     * <P>If the authorization check succeeds, check to see that all PersistenceManager instances obtained 
     * from this PersistenceManagerFactory have no active transactions. If any PersistenceManager instances
     * have an active transaction, throw a JDOUserException, with one nested JDOUserException for each 
     * PersistenceManager with an active Transaction.
     * <P>If there are no active transactions, then close all PersistenceManager instances obtained from 
     * this PersistenceManagerFactory, mark this PersistenceManagerFactory as closed, disallow 
     * getPersistenceManager methods, and allow all other get methods. If a set method or getPersistenceManager
     * method is called after close, then JDOUserException is thrown.
     * @see javax.jdo.PersistenceManagerFactory#close()
     */
    public synchronized void close()
    {
        checkJDOPermission(JDOPermission.CLOSE_PERSISTENCE_MANAGER_FACTORY);

        if (isClosed())
        {
            return;
        }

        setIsNotConfigurable();

        synchronized (pmCache)
        {
            // Check there are no active transactions before closing any PM
            Set<JDOUserException> exceptions = new HashSet<JDOUserException>();
            for (JDOPersistenceManager pm : pmCache)
            {
                ExecutionContext om = pm.getExecutionContext();
                if (om.getTransaction().isActive())
                {
                    // Note: we replicate the exception that would have come from pm.close() when tx active
                    TransactionActiveOnCloseException tae = new TransactionActiveOnCloseException(om);
                    exceptions.add(new JDOUserException(tae.getMessage(), pm));
                }
            }
            if (!exceptions.isEmpty())
            {
                throw new JDOUserException(LOCALISER.msg("012002"), exceptions.toArray(new Throwable[exceptions.size()]));
            }

            // Close all PMs
            for (JDOPersistenceManager pm : pmCache)
            {
                pm.internalClose();
            }
            pmCache.clear();
        }

        if (pmfByName != null)
        {
            // Closing so clean out from singleton pattern handler
            synchronized (pmfByName)
            {
                Iterator<Map.Entry<String, JDOPersistenceManagerFactory>> pmfIter = pmfByName.entrySet().iterator();
                while (pmfIter.hasNext())
                {
                    Map.Entry<String, JDOPersistenceManagerFactory> entry = pmfIter.next();
                    if (entry.getValue() == this)
                    {
                        pmfIter.remove();
                        break;
                    }
                }
            }
        }

        if (sequenceByFactoryClass != null)
        {
            sequenceByFactoryClass.clear();
            sequenceByFactoryClass = null;
        }
        if (lifecycleListeners != null)
        {
            lifecycleListeners.clear();
            lifecycleListeners = null;
        }
        if (datastoreCache != null)
        {
            datastoreCache.evictAll();
            datastoreCache = null;
        }
        if (queryCache != null)
        {
            queryCache.evictAll();
            queryCache = null;
        }
        if (jdoFetchGroups != null)
        {
            jdoFetchGroups.clear();
            jdoFetchGroups = null;
        }
        nucleusContext.close();

        closed = true;
    }

    /**
     * Utility to return whether the factory is closed or not.
     * @return Whether it is closed.
     */
    public synchronized boolean isClosed()
    {
        return closed;
    }

    /**
     * Convenience method to extract lifecycle listeners that are specified by way of persistence properties.
     * @param props Persistence props.
     */
    protected void processLifecycleListenersFromProperties(Map props)
    {
        if (props != null)
        {
            // Process any lifecycle listeners defined in persistent properties
            Iterator<Map.Entry> propsIter = props.entrySet().iterator();
            while (propsIter.hasNext())
            {
                Map.Entry entry = propsIter.next();
                String key = (String)entry.getKey();
                if (key.startsWith(Constants.PROPERTY_INSTANCE_LIFECYCLE_LISTENER))
                {
                    String listenerClsName = key.substring(45);
                    String listenerClasses = (String)entry.getValue();
                    ClassLoaderResolver clr = nucleusContext.getClassLoaderResolver(null);
                    Class listenerCls = null;
                    try
                    {
                        listenerCls = clr.classForName(listenerClsName);
                    }
                    catch (ClassNotResolvedException cnre)
                    {
                        throw new JDOUserException(LOCALISER.msg("012022", listenerClsName));
                    }

                    InstanceLifecycleListener listener = null;

                    // Find method getInstance()
                    Method method = ClassUtils.getMethodForClass(listenerCls, "getInstance", null);
                    if (method != null)
                    {
                        // Create instance via getInstance()
                        try
                        {
                            listener = (InstanceLifecycleListener)method.invoke(null);
                        }
                        catch (Exception e)
                        {
                            throw new JDOUserException(LOCALISER.msg("012021", listenerClsName), e);
                        }
                    }
                    else
                    {
                        // Try default constructor
                        try
                        {
                            listener = (InstanceLifecycleListener)listenerCls.newInstance();
                        }
                        catch (Exception e)
                        {
                            throw new JDOUserException(LOCALISER.msg("012020", listenerClsName), e);
                        }
                    }

                    Class[] classes = null;
                    if (!StringUtils.isWhitespace(listenerClasses))
                    {
                        String[] classNames = StringUtils.split(listenerClasses, ",");
                        classes = new Class[classNames.length];
                        for (int i=0;i<classNames.length;i++)
                        {
                            classes[i] = clr.classForName(classNames[i]);
                        }
                    }

                    addInstanceLifecycleListener(listener, classes);
                }
            }
        }
    }

    protected void initialiseMetaData(PersistenceUnitMetaData pumd)
    {
        // Prepare Metadata manager for use
        nucleusContext.getMetaDataManager().setAllowXML(
            getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_METADATA_ALLOW_XML));
        nucleusContext.getMetaDataManager().setAllowAnnotations(
            getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_METADATA_ALLOW_ANNOTATIONS));
        nucleusContext.getMetaDataManager().setValidate(
            getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_METADATA_XML_VALIDATE));

        if (pumd != null)
        {
            // Initialise the MetaDataManager with all files/classes for this persistence-unit
            // This is done now that all PMF properties are set (including the persistence-unit props)
            try
            {
                nucleusContext.getMetaDataManager().loadPersistenceUnit(pumd, null);

                // Set validation mode if set on persistence-unit
                if (pumd.getValidationMode() != null)
                {
                    getConfiguration().setProperty(PropertyNames.PROPERTY_VALIDATION_MODE, pumd.getValidationMode());
                }
            }
            catch (NucleusException jpe)
            {
                throw new JDOException(jpe.getMessage(),jpe);
            }
        }

        // Turn off loading of metadata from here if required
        boolean allowMetadataLoad =
            nucleusContext.getPersistenceConfiguration().getBooleanProperty(PropertyNames.PROPERTY_METADATA_ALLOW_LOAD_AT_RUNTIME);
        if (!allowMetadataLoad)
        {
            nucleusContext.getMetaDataManager().setAllowMetaDataLoad(false);
        }
    }

    /**
     * Freezes the current configuration.
     * @throws JDOException if the configuration was invalid or inconsistent in some way
     */
    protected void freezeConfiguration()
    {
        if (isConfigurable())
        {
            // Check for invalid javax.jdo properties by calling JDOImplHelper method (new in JDO3.1+)
            Method m = null;
            try
            {
                m = JDOImplHelper.class.getDeclaredMethod("assertOnlyKnownStandardProperties", new Class[] {Map.class});
                m.invoke(null, nucleusContext.getPersistenceConfiguration().getPersistenceProperties());
            }
            catch (InvocationTargetException ite)
            {
                if (ite.getCause() instanceof JDOException)
                {
                    throw (JDOException)ite.getCause();
                }
            }
            catch (JDOException jdoe)
            {
                throw jdoe;
            }
            catch (Exception e)
            {
                // Method not present so continue
            }

            synchronized (this)
            {
                try
                {
                    // Initialise the NucleusContext
                    nucleusContext.initialise();

                    // Set up the Level 2 Cache
                    datastoreCache = new JDODataStoreCache(nucleusContext.getLevel2Cache());

                    setIsNotConfigurable();
                }
                catch (TransactionIsolationNotSupportedException inse)
                {
                    throw new JDOUnsupportedOptionException(inse.getMessage());
                }
                catch (NucleusException jpe)
                {
                    throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
                }
            }
        }
    }

    /**
     * Get an instance of <tt>PersistenceManager</tt> from this factory. The instance has default values for options.
     * <p>After the first use of getPersistenceManager, no "set" methods will succeed.</p>
     * @return a <tt>PersistenceManager</tt> instance with default options.
     */
    public PersistenceManager getPersistenceManager()
    {
        // Just relay to other getPersistenceManager() method
        return getPersistenceManager(getConnectionUserName(), getConnectionPassword());
    }

    /**
     * Get an instance of <tt>PersistenceManager</tt> from this factory.
     * The instance has default values for options. The parameters user-id/password are used when obtaining
     * datastore connections from the connection pool.
     * <p>After the first use of getPersistenceManager, no "set" methods will succeed.</p>
     * @param userName  the user name for the connection
     * @param password  the password for the connection
     * @return <tt>PersistenceManager</tt> instance with default options.
     */
    public PersistenceManager getPersistenceManager(String userName, String password)
    {
        assertIsOpen();

        // Freeze the PMF config now that we are handing out PM's
        freezeConfiguration();

        JDOPersistenceManager pm = newPM(this, userName, password);

        if (lifecycleListeners != null)
        {
            // Add PMF lifecycle listeners to the PM
            for (LifecycleListenerForClass listener : lifecycleListeners.values())
            {
                pm.addInstanceLifecycleListener(listener.getListener(), listener.getClasses());
            }
        }

        synchronized (pmCache)
        {
            pmCache.add(pm);
        }

        return pm;
    }

    /**
     * Construct a {@link JDOPersistenceManager}. Override if you want to construct a subclass instead.
     */
    protected JDOPersistenceManager newPM(JDOPersistenceManagerFactory jdoPmf, String userName, String password)
    {
        return new JDOPersistenceManager(jdoPmf, userName, password);
    }

    /**
     * Gets the context for this PMF
     * @return Returns the context.
     */
    public NucleusContext getNucleusContext()
    {
        return nucleusContext;
    }

    /**
     * Accessor for the persistence configuration.
     * @return Returns the configuration.
     */
    protected PersistenceConfiguration getConfiguration()
    {
        return nucleusContext.getPersistenceConfiguration();
    }

    /**
     * Equality operator.
     * @param obj Object to compare against
     * @return Whether the objects are the same.
     */
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (!(obj instanceof JDOPersistenceManagerFactory))
        {
            return false;
        }

        return super.equals(obj);
    }

    /**
     * Create a PMF using the (JNDI) location or reference information specified.
     * @param obj The object
     * @param name Name of the object relative to the context
     * @param ctx The context
     * @param env properties used for creating the object
     * @return The PMF instance
     * @throws Exception If an error occurs generating the referenced object
     */
    public Object getObjectInstance(Object obj, Name name, Context ctx, Hashtable env)
    throws Exception
    {
        JDOPersistenceManagerFactory pmf = null;
        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug("Creating PersistenceManagerFactory instance via JNDI with values "+
                                    "[object] " + (obj == null ? "" : obj.toString()) + " " +
                                    "[name] " + (name == null ? "" : name.toString()) + " " +
                                    "[context] " + (ctx == null ? "" : ctx.toString()) + " " +
                                    "[env] " + (env == null ? "" : env.toString()) + " ");
        }

        if (obj instanceof Reference)
        {
            Reference ref = (Reference) obj;
            if (ref.getClassName().equals(JDOClassNameConstants.JDOPersistenceManagerFactory) ||
                ref.getClassName().equals(JDOClassNameConstants.JAVAX_JDO_PersistenceManagerFactory))
            {
                // Extract the properties to use for PMF creation
                Properties p = new Properties();
                for (Enumeration e = ref.getAll(); e.hasMoreElements();)
                {
                    StringRefAddr sra = (StringRefAddr) e.nextElement();
                    p.setProperty(sra.getType(), (String) sra.getContent());
                }

                // Create the PMF
                pmf = new JDOPersistenceManagerFactory(p);

                // Freeze the PMF config now that we are handing out PM's : see JDO 1.0.1 [11.7]
                pmf.freezeConfiguration();

                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("012006", name.toString()));
                }
            }
            else
            {
                NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("012007", 
                    ref.getClassName(), JDOClassNameConstants.JDOPersistenceManagerFactory));
            }
        }
        else
        {
            NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("012008", (obj.getClass().getName())));
        }
        return pmf;
    }

    /**
     * Retrieves the (JNDI) reference of this PMF object.
     * @return The reference
     */
    public Reference getReference()
    {
        Reference rc = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            rc = new Reference(JDOClassNameConstants.JAVAX_JDO_PersistenceManagerFactory,
                JDOClassNameConstants.JDOPersistenceManagerFactory, null);
            
            Map p = getConfiguration().getPersistenceProperties();
            for (Iterator<Map.Entry> i = p.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry entry = i.next();
                String key = (String) entry.getKey();
                Object valueObj = entry.getValue();
                if (valueObj instanceof String)
                {
                    String value = (String) valueObj;
                    rc.add(new StringRefAddr(key, value));
                    if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                    {
                        NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("012009", key, value));
                    }
                }
                else if (valueObj instanceof Long)
                {
                    String value = "" + valueObj;
                    rc.add(new StringRefAddr(key, value));
                    if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                    {
                        NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("012009", key, value));
                    }
                }
                else if (valueObj instanceof Integer)
                {
                    String value = "" + valueObj;
                    rc.add(new StringRefAddr(key, value));
                    if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                    {
                        NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("012009", key, value));
                    }
                }
                else if (valueObj instanceof Boolean)
                {
                    String value = (((Boolean)valueObj).booleanValue() ? "true" : "false");
                    rc.add(new StringRefAddr(key, value));
                    if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                    {
                        NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("012009", key, value));
                    }
                }
                else
                {
                    NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("012010", key));
                }
            }
            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                if (p.isEmpty())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("012011"));
                }
            }
        }
        catch (IOException ex)
        {
            NucleusLogger.PERSISTENCE.error(ex.getMessage());
            throw new NucleusException(ex.getMessage(),ex);
        }
        return rc;
    }

    /**
     * Accessor for the PersistenceManager proxy object
     * @return The PMF proxy
     */
    public PersistenceManager getPersistenceManagerProxy()
    {
        return new JDOPersistenceManagerProxy(this);
    }

    /**
     * Convenience accessor for the thread-local delegate PM for this thread.
     * If no thread-local is set then creates a new PM.
     * @return The real PM to delegate to for this thread
     */
    PersistenceManager getPMProxyDelegate()
    {
        PersistenceManager pm = pmProxyThreadLocal.get();
        if (pm == null)
        {
            // No delegate for this thread so create one and store it
            pm = getPersistenceManager();
            pmProxyThreadLocal.set(pm);
        }
        return pm;
    }

    /**
     * Convenience method to clear the thread-local delegate PM that we refer to.
     * This is invoked when the proxy PM has close() invoked.
     */
    void clearPMProxyDelegate()
    {
        // TODO Is it safe to assume 'this' is actually the right PMF already?
        PersistenceManagerFactory pmf = getPMProxyDelegate().getPersistenceManagerFactory();
        String txnType = pmf.getTransactionType();
        if (TransactionType.RESOURCE_LOCAL.toString().equalsIgnoreCase(txnType))
        {
            // Close the PM and unset the thread-local
            getPMProxyDelegate().close();
            pmProxyThreadLocal.remove();
        }
        else if (TransactionType.JTA.toString().equalsIgnoreCase(txnType))
        {
            // Do nothing
        }
    }

    /**
     * Return non-configurable properties of this PersistenceManagerFactory.
     * Properties with keys VendorName and VersionNumber are required. Other keys are optional.
     * @return the non-configurable properties of this PersistenceManagerFactory.
     */
    public Properties getProperties()
    {
        Properties props = new Properties();
        props.setProperty("VendorName", "DataNucleus");
        props.setProperty("VersionNumber", nucleusContext.getPluginManager().getVersionForBundle("org.datanucleus.api.jdo"));

        // Add all properties from the persistence configuration
        props.putAll(nucleusContext.getPersistenceConfiguration().getPersistenceProperties());

        return props;
    }

    /**
     * The application can determine from the results of this method which
     * optional features, and which query languages are supported by the JDO
     * implementation. See section 11.6 of the JDO 2 specification.
     * @return A Collection of String representing the supported options.
     */
    public Collection<String> supportedOptions()
    {
        // Generate the list of supported options, taking the general options we support and removing
        // any that the particular StoreManager doesn't support
        Set options = new HashSet(Arrays.asList(OPTION_ARRAY));
        StoreManager storeMgr = nucleusContext.getStoreManager();
        if (storeMgr != null)
        {
            Collection storeMgrOptions = storeMgr.getSupportedOptions();
            if (!storeMgrOptions.contains("NonDurableIdentity"))
            {
                options.remove("javax.jdo.option.NonDurableIdentity");
            }
            if (!storeMgrOptions.contains("DatastoreIdentity"))
            {
                options.remove("javax.jdo.option.DatastoreIdentity");
            }
            if (!storeMgrOptions.contains("ApplicationIdentity"))
            {
                options.remove("javax.jdo.option.ApplicationIdentity");
            }

            if (!storeMgr.supportsQueryLanguage("JDOQL"))
            {
                options.remove("javax.jdo.query.JDOQL");
            }
            if (!storeMgr.supportsQueryLanguage("SQL"))
            {
                options.remove("javax.jdo.query.SQL");
            }

            if (storeMgrOptions.contains("TransactionIsolationLevel.read-committed"))
            {
                options.add("javax.jdo.option.TransactionIsolationLevel.read-committed");
            }
            if (storeMgrOptions.contains("TransactionIsolationLevel.read-uncommitted"))
            {
                options.add("javax.jdo.option.TransactionIsolationLevel.read-uncommitted");
            }
            if (storeMgrOptions.contains("TransactionIsolationLevel.repeatable-read"))
            {
                options.add("javax.jdo.option.TransactionIsolationLevel.repeatable-read");
            }
            if (storeMgrOptions.contains("TransactionIsolationLevel.serializable"))
            {
                options.add("javax.jdo.option.TransactionIsolationLevel.serializable");
            }
            if (storeMgrOptions.contains("TransactionIsolationLevel.snapshot"))
            {
                options.add("javax.jdo.option.TransactionIsolationLevel.snapshot");
            }
            if (storeMgrOptions.contains("Query.Cancel"))
            {
                options.add("javax.jdo.option.QueryCancel");
            }
            if (storeMgrOptions.contains("Datastore.Timeout"))
            {
                options.add("javax.jdo.option.DatastoreTimeout");
            }
        }

        return Collections.unmodifiableSet(options);
    }

    /**
     * The JDO spec optional features that DataNucleus supports.
     * See JDO 2.0 spec section 11.6 for the full list of possibilities.
     **/
    private static final String[] OPTION_ARRAY =
    {
        "javax.jdo.option.TransientTransactional",
        "javax.jdo.option.NontransactionalWrite",
        "javax.jdo.option.NontransactionalRead",
        "javax.jdo.option.RetainValues",
        "javax.jdo.option.Optimistic",
        "javax.jdo.option.ApplicationIdentity",
        "javax.jdo.option.DatastoreIdentity",
        "javax.jdo.option.NonDurableIdentity",
        "javax.jdo.option.BinaryCompatibility",
        "javax.jdo.option.GetDataStoreConnection",
        "javax.jdo.option.GetJDBCConnection",
        "javax.jdo.option.version.DateTime",
        "javax.jdo.option.PreDirtyEvent",
        // "javax.jdo.option.ChangeApplicationIdentity",

        // Types
        "javax.jdo.option.ArrayList",
        "javax.jdo.option.LinkedList",
        "javax.jdo.option.TreeSet",
        "javax.jdo.option.TreeMap",
        "javax.jdo.option.Vector",
        "javax.jdo.option.List",
        "javax.jdo.option.Stack", // Not a listed JDO2 feature
        "javax.jdo.option.Map", // Not a listed JDO2 feature
        "javax.jdo.option.HashMap", // Not a listed JDO2 feature
        "javax.jdo.option.Hashtable", // Not a listed JDO2 feature
        "javax.jdo.option.SortedSet", // Not a listed JDO2 feature
        "javax.jdo.option.SortedMap", // Not a listed JDO2 feature
        "javax.jdo.option.Array",
        "javax.jdo.option.NullCollection",

        // ORM
        "javax.jdo.option.mapping.HeterogeneousObjectType",
        "javax.jdo.option.mapping.HeterogeneousInterfaceType",
        "javax.jdo.option.mapping.JoinedTablePerClass",
        "javax.jdo.option.mapping.JoinedTablePerConcreteClass",
        "javax.jdo.option.mapping.NonJoinedTablePerConcreteClass",
        // "javax.jdo.option.mapping.RelationSubclassTable", // Not yet supported for multiple subclasses

        // Query Languages
        "javax.jdo.query.SQL",
        "javax.jdo.query.JDOQL", // Not a listed optional feature
        "javax.jdo.option.UnconstrainedQueryVariables"
    };

    /**
     * Remove a PersistenceManager from the cache
     * Only the PersistenceManager is allowed to call this method
     * @param pm  the PersistenceManager to be removed from cache
     */
    public void releasePersistenceManager(JDOPersistenceManager pm)
    {
        synchronized (pmCache)
        {
            if (pmCache.contains(pm))
            {
                pm.internalClose();
                pmCache.remove(pm);
            }
        }
    }

    /**
     * Asserts that the PMF is open.
     * @throws JDOUserException if it is already closed
     */
    protected void assertIsOpen()
    {
        if (isClosed())
        {
            // Comply with Section 11.4 of the JDO spec (throw JDOUserException if already closed)
            throw new JDOUserException(LOCALISER.msg("012025"));
        }
    }

    protected void finalize() throws Throwable 
    {
        close();
    }

    /**
     * Accessor for the DataStore (level 2) Cache
     * @return The datastore cache
     */
    public DataStoreCache getDataStoreCache()
    {
        freezeConfiguration();

        return datastoreCache;
    }

    /**
     * Accessor for the query results cache.
     * @return Query results cache
     */
    public JDOQueryCache getQueryCache()
    {
        if (queryCache != null)
        {
            return queryCache;
        }
        QueryResultsCache cache = nucleusContext.getStoreManager().getQueryManager().getQueryResultsCache();
        queryCache = new JDOQueryCache(cache);
        return queryCache;
    }

    /**
     * Accessor for the query generic compilation cache.
     * @return Query generic compilation cache
     */
    public QueryCompilationCache getQueryGenericCompilationCache()
    {
        return nucleusContext.getStoreManager().getQueryManager().getQueryCompilationCache();
    }

    /**
     * Accessor for the query datastore compilation cache.
     * @return Query datastore compilation cache
     */
    public QueryDatastoreCompilationCache getQueryDatastoreCompilationCache()
    {
        return nucleusContext.getStoreManager().getQueryManager().getQueryDatastoreCompilationCache();
    }

    /**
     * Set the user name for the data store connection.
     * @param userName the user name for the data store connection.
     */
    public void setConnectionUserName(String userName)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_CONNECTION_USER_NAME, userName);
    }

    /**
     * Set the password for the data store connection.
     * @param password the password for the data store connection.
     */
    public void setConnectionPassword(String password)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_CONNECTION_PASSWORD, password);
    }

    /**
     * Set the URL for the data store connection.
     * @param url the URL for the data store connection.
     */
    public void setConnectionURL(String url)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_CONNECTION_URL, url);
    }

    /**
     * Set the driver name for the data store connection.
     * @param driverName the driver name for the data store connection.
     */
    public void setConnectionDriverName(String driverName)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_CONNECTION_DRIVER_NAME, driverName);
    }

    /**
     * Set the name for the data store connection factory.
     * @param connectionFactoryName name of the data store connection factory.
     */
    public void setConnectionFactoryName(String connectionFactoryName)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_CONNECTION_FACTORY_NAME, connectionFactoryName);
    }

    /**
     * Set the data store connection factory. JDO implementations will support specific connection factories.  
     * The connection factory interfaces are not part of the JDO specification.
     * @param connectionFactory the data store connection factory.
     */
    public void setConnectionFactory(Object connectionFactory)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_CONNECTION_FACTORY, connectionFactory);
    }

    /**
     * Set the name for the second data store connection factory.  This is
     * needed for managed environments to get nontransactional connections for
     * optimistic transactions.
     * @param connectionFactoryName name of the data store connection factory.
     */
    public void setConnectionFactory2Name(String connectionFactoryName)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_CONNECTION_FACTORY2_NAME, connectionFactoryName);
    }

    /**
     * Set the second data store connection factory.  This is
     * needed for managed environments to get nontransactional connections for
     * optimistic transactions.  JDO implementations
     * will support specific connection factories.  The connection
     * factory interfaces are not part of the JDO specification.
     * @param connectionFactory the data store connection factory.
     */
    public void setConnectionFactory2(Object connectionFactory)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_CONNECTION_FACTORY2, connectionFactory);
    }

    /**
     * Set the default Multithreaded setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @param flag the default Multithreaded setting.
     */
    public void setMultithreaded(boolean flag)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_MULTITHREADED, flag ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the default Optimistic setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @param flag the default Optimistic setting.
     */
    public void setOptimistic(boolean flag)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_OPTIMISTIC, flag ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the default RetainValues setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @param flag the default RetainValues setting.
     */
    public void setRetainValues(boolean flag)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_RETAIN_VALUES, flag ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the default RestoreValues setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @param flag the default RestoreValues setting.
     */
    public void setRestoreValues(boolean flag)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_RESTORE_VALUES, flag ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the default NontransactionalRead setting for all
     * <tt>PersistenceManager</tt> instances obtained from this factory.
     * @param flag the default NontransactionalRead setting.
     */
    public void setNontransactionalRead(boolean flag)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_NONTX_READ, flag ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the default NontransactionalWrite setting for all
     * <tt>PersistenceManager</tt> instances obtained from this factory.
     * @param flag the default NontransactionalWrite setting.
     */
    public void setNontransactionalWrite(boolean flag)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_NONTX_WRITE, flag ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the default IgnoreCache setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @param flag the default IgnoreCache setting.
     */
    public void setIgnoreCache(boolean flag)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_IGNORE_CACHE, flag ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Mutator for the DetachAllOnCommit setting.
     * @param flag the default DetachAllOnCommit setting.
     */
    public void setDetachAllOnCommit(boolean flag)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_DETACH_ALL_ON_COMMIT, flag ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Mutator for the CopyOnAttach setting.
     * @param flag the default CopyOnAttach setting.
     */
    public void setCopyOnAttach(boolean flag)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_COPY_ON_ATTACH, flag ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the name for any mapping, used in searching for ORM/Query metadata files.
     * @param mapping the mapping name
     */
    public void setMapping(String mapping)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_MAPPING, mapping);
    }

    /**
     * Mutator for the catalog to use for this persistence factory.
     * @param catalog Name of the catalog
     */
    public void setCatalog(String catalog)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_MAPPING_CATALOG, catalog);
    }

    /**
     * Mutator for the schema to use for this persistence factory.
     * @param schema Name of the schema
     */
    public void setSchema(String schema)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_MAPPING_SCHEMA, schema);
    }

    /**
     * Mutator for the timeout to use for datastore reads.
     * @param timeout Datastore read interval (millisecs)
     */
    public void setDatastoreReadTimeoutMillis(Integer timeout)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_DATASTORE_READ_TIMEOUT, timeout);
    }

    /**
     * Mutator for the timeout to use for datastore writes.
     * @param timeout Datastore write interval (millisecs)
     */
    public void setDatastoreWriteTimeoutMillis(Integer timeout)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_DATASTORE_WRITE_TIMEOUT, timeout);
    }

    /**
     * Mutator for the transaction type to use for this persistence factory.
     * @param type Transaction type
     */
    public void setTransactionType(String type)
    {
        assertConfigurable();
        boolean validated = new CorePropertyValidator().validate(PropertyNames.PROPERTY_TRANSACTION_TYPE, type);
        if (validated)
        {
            getConfiguration().setProperty(PropertyNames.PROPERTY_TRANSACTION_TYPE, type);
        }
        else
        {
            throw new JDOUserException(LOCALISER.msg("012026", "javax.jdo.option.TransactionType", type));
        }
    }

    /**
     * Mutator for the name of the persistence unit.
     * @param name Name of the persistence unit
     */
    public void setPersistenceUnitName(String name)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_PERSISTENCE_UNIT_NAME, name);
    }

    /**
     * Mutator for the filename of the persistence.xml file.
     * This is for the case where an application has placed the persistence.xml somewhere else maybe
     * outside the CLASSPATH.
     * @param name Filename of the persistence unit
     */
    public void setPersistenceXmlFilename(String name)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_PERSISTENCE_XML_FILENAME, name);
    }

    /**
     * Mutator for the name of the persistence factory.
     * @param name Name of the persistence factory (if any)
     */
    public void setName(String name)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_PMF_NAME, name);
    }

    /**
     * Mutator for the timezone id of the datastore server.
     * If not set assumes that it is running in the same timezone as this JVM.
     * @param id Timezone Id to use
     */
    public void setServerTimeZoneID(String id)
    {
        assertConfigurable();
        boolean validated = new CorePropertyValidator().validate(PropertyNames.PROPERTY_SERVER_TIMEZONE_ID, id);
        if (validated)
        {
            getConfiguration().setProperty(PropertyNames.PROPERTY_SERVER_TIMEZONE_ID, id);
        }
        else
        {
            throw new JDOUserException("Invalid TimeZone ID specified");
        }
    }

    /**
     * Set the readOnly setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @param flag the default readOnly setting.
     */
    public void setReadOnly(boolean flag)
    {
        assertConfigurable();
        getConfiguration().setProperty(PropertyNames.PROPERTY_DATASTORE_READONLY, flag ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the default isolation level for transactions.
     * @param level Level
     */
    public void setTransactionIsolationLevel(String level)
    {
        assertConfigurable();

        if (nucleusContext.getStoreManager() != null &&
            !nucleusContext.getStoreManager().getSupportedOptions().contains("TransactionIsolationLevel." + level))
        {
            throw new JDOUnsupportedOptionException("Isolation level \"" + level + "\" is not supported for this datastore");
        }

        // Reset to "read-committed" if passed in as null
        getConfiguration().setProperty(PropertyNames.PROPERTY_TRANSACTION_ISOLATION, level != null ? level : "read-committed");
    }

    /**
     * Get the user name for the data store connection.
     * @return the user name for the data store connection.
     */
    public String getConnectionUserName()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_CONNECTION_USER_NAME);
    }

    /**
     * Get the password for the data store connection.
     * @return the password for the data store connection.
     */
    public String getConnectionPassword()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_CONNECTION_PASSWORD);
    }

    /**
     * Get the URL for the data store connection.
     * @return the URL for the data store connection.
     */
    public String getConnectionURL()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_CONNECTION_URL);
    }

    /**
     * Get the driver name for the data store connection.
     * @return the driver name for the data store connection.
     */
    public String getConnectionDriverName()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_CONNECTION_DRIVER_NAME);
    }

    /**
     * Get the name for the data store connection factory.
     * @return the name of the data store connection factory.
     */
    public String getConnectionFactoryName()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_CONNECTION_FACTORY_NAME);
    }

    /**
     * Get the name for the second data store connection factory.  This is
     * needed for managed environments to get nontransactional connections for
     * optimistic transactions.
     * @return the name of the data store connection factory.
     */
    public String getConnectionFactory2Name()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_CONNECTION_FACTORY2_NAME);
    }

    /**
     * Get the data store connection factory.
     * @return the data store connection factory.
     */
    public Object getConnectionFactory()
    {
        return getConfiguration().getProperty(PropertyNames.PROPERTY_CONNECTION_FACTORY);
    }

    /**
     * Get the second data store connection factory.  This is
     * needed for managed environments to get nontransactional connections for
     * optimistic transactions.
     * @return the data store connection factory.
     */
    public Object getConnectionFactory2()
    {
        return getConfiguration().getProperty(PropertyNames.PROPERTY_CONNECTION_FACTORY2);
    }

    /**
     * Get the default Multithreaded setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @return the default Multithreaded setting.
     */
    public boolean getMultithreaded()
    {
        return getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_MULTITHREADED);
    }

    /**
     * Get the default Optimistic setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @return the default Optimistic setting.
     */
    public boolean getOptimistic()
    {
        return getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_OPTIMISTIC);
    }

    /**
     * Get the default RetainValues setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @return the default RetainValues setting.
     */
    public boolean getRetainValues()
    {
        return getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_RETAIN_VALUES);
    }

    /**
     * Get the default RestoreValues setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @return the default RestoreValues setting.
     */
    public boolean getRestoreValues()
    {
        return getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_RESTORE_VALUES);
    }

    /**
     * Get the default NontransactionalRead setting for all
     * <tt>PersistenceManager</tt> instances obtained from this factory.
     * @return the default NontransactionalRead setting.
     */
    public boolean getNontransactionalRead()
    {
        return getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_NONTX_READ);
    }

    /**
     * Get the default NontransactionalWrite setting for all
     * <tt>PersistenceManager</tt> instances obtained from this factory.
     * @return the default NontransactionalWrite setting.
     */
    public boolean getNontransactionalWrite()
    {
        return getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_NONTX_WRITE);
    }

    /**
     * Get the default IgnoreCache setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @return the IgnoreCache setting.
     */
    public boolean getIgnoreCache()
    {
        return getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_IGNORE_CACHE);
    }

    /**
     * Accessor for the DetachAllOnCommit setting.
     * @return the DetachAllOnCommit setting.
     */
    public boolean getDetachAllOnCommit()
    {
        return getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_DETACH_ALL_ON_COMMIT);
    }

    /**
     * Accessor for the CopyOnAttach setting.
     * @return the CopyOnAttach setting.
     */
    public boolean getCopyOnAttach()
    {
        return getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_COPY_ON_ATTACH);
    }

    /**
     * Get the name for any mapping, used in retrieving metadata files for ORM/Query data.
     * @return the name for the mapping.
     */
    public String getMapping()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_MAPPING);
    }

    /**
     * Accessor for the catalog to use for this persistence factory.
     * @return the name of the catalog
     */
    public String getCatalog()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_MAPPING_CATALOG);
    }

    /**
     * Accessor for the schema to use for this persistence factory.
     * @return the name of the schema
     */
    public String getSchema()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_MAPPING_SCHEMA);
    }

    /**
     * Accessor for the name of the persistence factory (if any).
     * @return the name of the persistence factory
     */
    public String getName()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_PMF_NAME);
    }

    /**
     * Accessor for the name of the persistence unit
     * @return the name of the persistence unit
     */
    public String getPersistenceUnitName()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_PERSISTENCE_UNIT_NAME);
    }

    /**
     * Accessor for the filename of the persistence.xml file.
     * This is for the case where an application has placed the persistence.xml somewhere else maybe
     * outside the CLASSPATH.
     * @return the filename of the persistence unit
     */
    public String getPersistenceXmlFilename()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_PERSISTENCE_XML_FILENAME);
    }

    /**
     * Accessor for the datastore read timeout interval.
     * @return datastore read timeout
     */
    public Integer getDatastoreReadTimeoutMillis()
    {
        return getConfiguration().getIntProperty(PropertyNames.PROPERTY_DATASTORE_READ_TIMEOUT);
    }

    /**
     * Accessor for the datastore write timeout interval.
     * @return datastore write timeout
     */
    public Integer getDatastoreWriteTimeoutMillis()
    {
        return getConfiguration().getIntProperty(PropertyNames.PROPERTY_DATASTORE_WRITE_TIMEOUT);
    }

    /**
     * Accessor for the timezone "id" of the datastore server (if any).
     * If not set assumes the same as the JVM we are running in.
     * @return Server timezone id
     */
    public String getServerTimeZoneID()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_SERVER_TIMEZONE_ID);
    }

    /**
     * Get the default readOnly setting for all <tt>PersistenceManager</tt>
     * instances obtained from this factory.
     * @return the default readOnly setting.
     */
    public boolean getReadOnly()
    {
        return getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_DATASTORE_READONLY);
    }

    /**
     * Accessor for the transaction type to use with this persistence factory.
     * @return transaction type
     */
    public String getTransactionType()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_TRANSACTION_TYPE);
    }

    /**
     * Accessor for the transaction isolation level default.
     * @return Transaction isolation level
     */
    public String getTransactionIsolationLevel()
    {
        return getConfiguration().getStringProperty(PropertyNames.PROPERTY_TRANSACTION_ISOLATION);
    }

    /**
     * Mutator to set the primary class loader.
     * Setter provided since the input is an object and so cannot go through property input
     * @param loader Loader
     */
    public void setPrimaryClassLoader(ClassLoader loader)
    {
        getConfiguration().setProperty(PropertyNames.PROPERTY_CLASSLOADER_PRIMARY, loader);
    }

    /**
     * Accessor for the primary class loader
     * @return primary class loader
     */
    public ClassLoader getPrimaryClassLoader()
    {
        return (ClassLoader)getConfiguration().getProperty(PropertyNames.PROPERTY_CLASSLOADER_PRIMARY);
    }

    /**
     * Set the properties for this configuration.
     * Note : this has this name so it has a getter/setter pair for use by things like Spring.
     * @param props The persistence properties
     */
    public void setPersistenceProperties(Map<String, Object> props)
    {
        assertConfigurable();
        getConfiguration().setPersistenceProperties(props);
    }

    /**
     * Accessor for the persistence properties.
     * Note : this has this name so it has a getter/setter pair for use by things like Spring.
     * @return The persistence properties
     */
    public Map<String, Object> getPersistenceProperties()
    {
        return getConfiguration().getPersistenceProperties();
    }

    /**
     * Asserts that a change to a configuration property is allowed.
     * @throws JDOUserException if not configurable
     */
    protected void assertConfigurable()
    {
        if (!isConfigurable())
        {
            throw new JDOUserException(LOCALISER.msg("012023"));
        }
    }

    /**
     * Accessor for whether this is still configurable (can set more properties etc).
     * @return Whether it is configurable
     */
    protected boolean isConfigurable()
    {
        return configurable;
    }

    /**
     * Method to set that this is no longer configurable.
     * Can no longer become configurable.
     */
    protected void setIsNotConfigurable()
    {
        this.configurable = false;
    }

    // -------------------------------- Lifecycle Listeners -------------------------------

    /**
     * @return Returns either <tt>null</tt> or a <tt>List</tt> with instances of
     *      <tt>LifecycleListenerSpecification</tt>.
     * @deprecated
     */
    public List<LifecycleListenerForClass> getLifecycleListenerSpecifications()
    {
        if (lifecycleListeners == null)
        {
            return Collections.EMPTY_LIST;
        }
        return new ArrayList(lifecycleListeners.values());
    }

    /**
     * Method to add lifecycle listeners for particular classes.
     * Adds the listener to all PMs already created.
     * @param listener The listener
     * @param classes The classes that the listener relates to
     */
    public void addInstanceLifecycleListener(InstanceLifecycleListener listener, Class[] classes)
    {
        boolean allowListeners = getNucleusContext().getPersistenceConfiguration().getBooleanProperty(
            "datanucleus.allowListenerUpdateAfterInit", false);
        if (!allowListeners)
        {
            assertConfigurable();
        }

        if (listener == null)
        {
            return;
        }

        classes = LifecycleListenerForClass.canonicaliseClasses(classes);
        if (classes != null && classes.length == 0)
        {
            return;
        }

        if (lifecycleListeners == null)
        {
            lifecycleListeners = new ConcurrentHashMap<InstanceLifecycleListener, LifecycleListenerForClass>(1);
        }

        LifecycleListenerForClass entry;
        if (lifecycleListeners.containsKey(listener))
        {
            entry = lifecycleListeners.get(listener).mergeClasses(classes);
        }
        else
        {
            entry = new LifecycleListenerForClass(listener, classes);
        }

        lifecycleListeners.put(listener, entry);
    }

    /**
     * Method to remove a lifecycle listener. Removes the listener from all PM's as well.
     * @param listener The Listener
     */
    public void removeInstanceLifecycleListener(InstanceLifecycleListener listener)
    {
        boolean allowListeners = getNucleusContext().getPersistenceConfiguration().getBooleanProperty(
            "datanucleus.allowListenerUpdateAfterInit", false);
        if (!allowListeners)
        {
            assertConfigurable();
        }

        if (listener == null || lifecycleListeners == null)
        {
            return;
        }

        // Remove from the PMF
        lifecycleListeners.remove(listener);
    }

    // --------------------------- Sequences ----------------------------------

    /**
     * Method to register a sequence for a particular factory class.
     * @param factoryClassName Name of the factory class
     * @param sequence The sequence
     */
    public void addSequenceForFactoryClass(String factoryClassName, Sequence sequence)
    {
        if (sequenceByFactoryClass == null)
        {
            sequenceByFactoryClass = new HashMap();
        }

        sequenceByFactoryClass.put(factoryClassName, sequence);
    }

    /**
     * Accessor for the sequence for a factory class.
     * @param factoryClassName The name of the factory class
     * @return The sequence
     */
    public Sequence getSequenceForFactoryClass(String factoryClassName)
    {
        if (sequenceByFactoryClass == null)
        {
            return null;
        }

        return sequenceByFactoryClass.get(factoryClassName);
    }

    // --------------------------- Fetch Groups ----------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getFetchGroups()
     */
    public Set<FetchGroup> getFetchGroups()
    {
        Set<JDOFetchGroup> jdoGroups = getJDOFetchGroups(false);

        if (jdoGroups != null)
        {
            synchronized (jdoGroups)
            {
                if (!jdoGroups.isEmpty())
                {
                    // Return mutable copy of all currently active (in scope) fetch groups, as per JDO spec.
                    return new HashSet<FetchGroup>(jdoGroups);
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getFetchGroup(java.lang.Class, java.lang.String)
     */
    public FetchGroup getFetchGroup(Class cls, String name)
    {
        Set<JDOFetchGroup> jdoGroups = getJDOFetchGroups(false);

        if (jdoGroups != null)
        {
            synchronized (jdoGroups)
            {
                // TODO Change structures so existing group can be looked up directly without iterating
                for (JDOFetchGroup jdoFetchGroup : jdoGroups)
                {
                    if (jdoFetchGroup.getType() == cls && jdoFetchGroup.getName().equals(name))
                    {
                        return jdoFetchGroup;
                    }
                }
            }
        }

        // Create new FetchGroup, but don't add to set of groups yet - user should add via addFetchGroups()
        try
        {
            org.datanucleus.FetchGroup internalGrp = nucleusContext.getInternalFetchGroup(cls, name);
            if (!internalGrp.isUnmodifiable())
            {
                // Return existing internal group since still modifiable
                return new JDOFetchGroup(internalGrp);
            }
            else
            {
                // Create a new internal group (modifiable) and return a JDO group based on that
                internalGrp = nucleusContext.createInternalFetchGroup(cls, name);
                nucleusContext.addInternalFetchGroup(internalGrp);
                JDOFetchGroup jdoGrp = new JDOFetchGroup(internalGrp);
                return jdoGrp;
            }
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#addFetchGroups(javax.jdo.FetchGroup[])
     */
    public void addFetchGroups(FetchGroup... groups)
    {
        if (groups == null || groups.length == 0)
        {
            return;
        }

        Set<JDOFetchGroup> jdoGroups = getJDOFetchGroups(true);

        synchronized (jdoGroups)
        {
            for (int i=0;i<groups.length;i++)
            {
                JDOFetchGroup jdoFetchGroup = (JDOFetchGroup)groups[i];
                nucleusContext.addInternalFetchGroup(jdoFetchGroup.getInternalFetchGroup());
                jdoGroups.add(jdoFetchGroup);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#removeFetchGroups(javax.jdo.FetchGroup[])
     */
    public void removeFetchGroups(FetchGroup... groups)
    {
        if (groups == null || groups.length == 0)
        {
            return;
        }

        Set<JDOFetchGroup> jdoGroups = getJDOFetchGroups(false);

        if (jdoGroups != null)
        {
            synchronized (jdoGroups)
            {
                if (!jdoGroups.isEmpty())
                {
                    for (int i=0;i<groups.length;i++)
                    {
                        JDOFetchGroup jdoFetchGroup = (JDOFetchGroup)groups[i];
                        nucleusContext.removeInternalFetchGroup(jdoFetchGroup.getInternalFetchGroup());
                        jdoGroups.remove(jdoFetchGroup);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#removeAllFetchGroups()
     */
    public void removeAllFetchGroups()
    {
        Set<JDOFetchGroup> jdoGroups = getJDOFetchGroups(false);

        if (jdoGroups != null)
        {
            synchronized (jdoGroups)
            {
                for (JDOFetchGroup jdoGrp : jdoGroups)
                {
                    nucleusContext.removeInternalFetchGroup(jdoGrp.getInternalFetchGroup());
                }
                jdoGroups.clear();
            }
        }
    }

    /**
     * Method to return a new metadata object that can be subsequently modified
     * and registered with the persistence process using the method {@link #registerMetadata}.
     * @return Metadata object to start from
     */
    public javax.jdo.metadata.JDOMetadata newMetadata()
    {
        return new JDOMetadataImpl();
    }

    /**
     * Method to register the supplied metadata with the persistence process managed by this
     * <code>PersistenceManagerFactory</code>.
     * Metadata can be created using the method {@link #newMetadata}.
     * @param metadata The Metadata to register.
     */
    public void registerMetadata(javax.jdo.metadata.JDOMetadata metadata)
    {
        MetaDataManager mmgr = nucleusContext.getMetaDataManager();
        FileMetaData filemd = ((JDOMetadataImpl)metadata).getInternal();

        // Check if already defined
        for (int i=0;i<filemd.getNoOfPackages();i++)
        {
            PackageMetaData pmd = filemd.getPackage(i);
            for (int j=0;j<pmd.getNoOfClasses();j++)
            {
                ClassMetaData cmd = pmd.getClass(j);
                if (mmgr.hasMetaDataForClass(cmd.getFullClassName()))
                {
                    throw new JDOUserException("Cannot redefine metadata for " + cmd.getFullClassName());
                }
            }
            for (int j=0;j<pmd.getNoOfInterfaces();j++)
            {
                InterfaceMetaData imd = pmd.getInterface(j);
                if (mmgr.hasMetaDataForClass(imd.getFullClassName()))
                {
                    throw new JDOUserException("Cannot redefine metadata for " + imd.getFullClassName());
                }
            }
        }

        mmgr.loadUserMetaData(filemd, null);
    }

    /**
     * Method to return the (class) metadata object for the specified class, if there is
     * metadata defined for that class.
     * @return The metadata
     */
    public javax.jdo.metadata.TypeMetadata getMetadata(String className)
    {
        MetaDataManager mmgr = nucleusContext.getMetaDataManager();
        AbstractClassMetaData acmd = mmgr.getMetaDataForClass(className,
            nucleusContext.getClassLoaderResolver(null));
        if (acmd == null)
        {
            return null;
        }
        else
        {
            if (acmd instanceof ClassMetaData)
            {
                return new ClassMetadataImpl((ClassMetaData)acmd);
            }
            else
            {
                return new InterfaceMetadataImpl((InterfaceMetaData)acmd);
            }
        }
    }

    /**
     * Accessor for the classes that are managed (have metadata loaded).
     * @return Collection of classes
     */
    public Collection<Class> getManagedClasses()
    {
        checkJDOPermission(JDOPermission.GET_METADATA);

        MetaDataManager mmgr = nucleusContext.getMetaDataManager();
        Collection<String> classNames = mmgr.getClassesWithMetaData();
        Collection<Class> classes = new HashSet<Class>();
        if (classNames != null)
        {
            ClassLoaderResolver clr = nucleusContext.getClassLoaderResolver(null);
            Iterator<String> iter = classNames.iterator();
            while (iter.hasNext())
            {
                try
                {
                    Class cls = clr.classForName(iter.next());
                    classes.add(cls);
                }
                catch (ClassNotResolvedException cnre)
                {
                }
            }
        }
        return classes;
    }

    private void checkJDOPermission(JDOPermission jdoPermission)
    {
        SecurityManager secmgr = System.getSecurityManager();
        if (secmgr != null)
        {
            // checkPermission will throw SecurityException if not authorized
            secmgr.checkPermission(jdoPermission);
        }
    }

    private synchronized Set<JDOFetchGroup> getJDOFetchGroups(boolean createIfNull)
    {
        if (jdoFetchGroups == null && createIfNull)
        {
            jdoFetchGroups = new HashSet<JDOFetchGroup>();
        }
        return jdoFetchGroups;
    }

    /**
     * Check on serialisation of the EMF.
     * @param oos The output stream to serialise to
     * @throws IOException Exception thrown if error
     */
    private void writeObject(ObjectOutputStream oos) throws IOException 
    {
        oos.defaultWriteObject();
        oos.writeObject(nucleusContext.getPersistenceConfiguration().getPersistenceProperties());
    }

    private Map<String, Object> deserialisationProps = null;
    private void readObject(java.io.ObjectInputStream ois) throws IOException, ClassNotFoundException 
    {
        ois.defaultReadObject();
        deserialisationProps = (Map<String, Object>) ois.readObject();
    }

    /**
     * Control deserialisation of the PMF where we have a singleton (in pmfByName).
     * @return The PMF
     * @throws InvalidObjectException 
     */
    private Object readResolve() throws InvalidObjectException 
    {
        JDOPersistenceManagerFactory pmf = null;
        if (pmfByName != null)
        {
            String name = (String) deserialisationProps.get(PropertyNames.PROPERTY_PMF_NAME);
            if (name == null)
            {
                name = (String) deserialisationProps.get(PropertyNames.PROPERTY_PERSISTENCE_UNIT_NAME);
            }

            // Return singleton if present to save reinitialisation
            pmf = pmfByName.get(name);
            if (pmf != null)
            {
                return pmf;
            }
        }

        // Use deserialised object, so need to initialise it
        configurable = true;
        if (pmCache == null)
        {
            pmCache = new HashSet<JDOPersistenceManager>();
        }
        nucleusContext = new NucleusContext("JDO", deserialisationProps);
        PersistenceUnitMetaData pumd = null;
        if (getPersistenceUnitName() != null)
        {
            pumd = nucleusContext.getMetaDataManager().getMetaDataForPersistenceUnit(getPersistenceUnitName());
        }
        initialiseMetaData(pumd);
        processLifecycleListenersFromProperties(deserialisationProps);
        freezeConfiguration();
        deserialisationProps = null;

        return this;
    }
}