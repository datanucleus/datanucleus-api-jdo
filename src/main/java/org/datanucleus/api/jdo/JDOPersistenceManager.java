/**********************************************************************
Copyright (c) 2003 David Jencks and others. All rights reserved.
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
2003 Erik Bengtson - removed exist() operation
2003 Andy Jefferson - commented
2003 Andy Jefferson - added check on makePersistent that class is persistence capable
2003 Andy Jefferson - introduction of localiser
2003 Erik Bengtson - provided initial caching mechanism
2003 Erik Bengtson - added getObjectbyAID
2003 Erik Bengtson - removed unused variables
2004 Erik Bengtson - implemented evictAll    
2004 Andy Jefferson - converted to use Logger
2004 Andy Jefferson - added LifecycleListener
2004 Andy Jefferson - rewritten Cache
2005 Andy Jefferson - moved ClassLoaderResolver handling to separate class
2005 Marco Schulze - implemented copying the lifecycle listeners in j2ee environment
2007 Andy Jefferson - copyOnAttach
     ...
 **********************************************************************/
package org.datanucleus.api.jdo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDONullIdentityException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.Query;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.identity.SingleFieldIdentity;
import javax.jdo.listener.InstanceLifecycleListener;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ExecutionContext;
import org.datanucleus.FetchGroup;
import org.datanucleus.Configuration;
import org.datanucleus.PropertyNames;
import org.datanucleus.TransactionEventListener;
import org.datanucleus.api.jdo.exceptions.TransactionNotActiveException;
import org.datanucleus.api.jdo.exceptions.TransactionNotWritableException;
import org.datanucleus.api.jdo.query.JDOTypesafeQuery;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.exceptions.ClassNotResolvedException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusOptimisticException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.exceptions.TransactionActiveOnCloseException;
import org.datanucleus.identity.SCOID;
import org.datanucleus.identity.SingleFieldId;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.ExtensionMetaData;
import org.datanucleus.metadata.FetchGroupMetaData;
import org.datanucleus.metadata.FetchPlanMetaData;
import org.datanucleus.metadata.IdentityType;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.metadata.QueryLanguage;
import org.datanucleus.metadata.QueryMetaData;
import org.datanucleus.metadata.SequenceMetaData;
import org.datanucleus.query.typesafe.TypesafeQuery;
import org.datanucleus.state.CallbackHandler;
import org.datanucleus.state.DetachState;
import org.datanucleus.state.FetchPlanState;
import org.datanucleus.store.NucleusConnection;
import org.datanucleus.store.NucleusSequence;
import org.datanucleus.store.StoreManager;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.StringUtils;

/**
 * Provide the basics of a JDO PersistenceManager using an underlying ExecutionContext to perform the actual persistence.
 */
public class JDOPersistenceManager implements javax.jdo.PersistenceManager
{
    /** Logger for JDO. */
    public static final NucleusLogger LOGGER = NucleusLogger.getLoggerInstance("DataNucleus.JDO");

    private boolean closed = false;

    /** Map of user objects attached to this PM. */
    private Map userObjectMap = null;

    /** User object attached to the PM. */
    private Object userObject = null;

    /** Backing ExecutionContext for this PersistenceManager. */
    protected ExecutionContext ec;

    protected javax.jdo.Transaction jdotx;

    /** Owning PersistenceManagerFactory. */
    protected JDOPersistenceManagerFactory pmf;

    /** JDO Fetch Plan. */
    protected JDOFetchPlan fetchPlan = null;

    /** JDO Fetch Groups. */
    private Set<JDOFetchGroup> jdoFetchGroups = null;

    /**
     * Constructor.
     * @param pmf Persistence Manager Factory
     * @param userName Username for the datastore. Note that this is currently ignored
     * @param password Password for the datastore. Note that this is currently ignored
     */
    public JDOPersistenceManager(JDOPersistenceManagerFactory pmf, String userName, String password)
    {
        Map<String, Object> options = new HashMap();
        options.put(ExecutionContext.OPTION_USERNAME, userName);
        options.put(ExecutionContext.OPTION_PASSWORD, password);
        this.ec = pmf.getNucleusContext().getExecutionContext(this, options);
        this.pmf = pmf;
        this.fetchPlan = new JDOFetchPlan(ec.getFetchPlan());
        this.jdotx = new JDOTransaction(this, ec.getTransaction());

        CallbackHandler beanValidator = pmf.getNucleusContext().getValidationHandler(ec);
        if (beanValidator != null)
        {
            ec.getCallbackHandler().setValidationListener(beanValidator);
        }
    }

    /**
     * The internal method called by the PMF to cleanup this PM
     */
    protected void internalClose()
    {
        if (closed)
        {
            return;
        }

        try
        {
            // Close the ExecutionContext
            ec.close();
        }
        catch (TransactionActiveOnCloseException tae)
        {
            throw new JDOUserException(tae.getMessage(), this);
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }

        userObject = null;
        userObjectMap = null;
        closed = true;
    }

    /**
     * Convenience accessor for the ExecutionContext performing the actual persistence.
     * @return The ExecutionContext
     */
    public ExecutionContext getExecutionContext()
    {
        return ec;
    }

    /**
     * Accessor for the PersistenceManager Factory.
     * @return The PersistenceManagerFactory
     */
    public JDOPersistenceManagerFactory getPersistenceManagerFactory()
    {
        return pmf;
    }

    /**
     * Accessor for whether to detach all objects on commit of the transaction.
     * @return Whether to detach all on commit.
     */
    public boolean getDetachAllOnCommit()
    {
        assertIsOpen();
        return ec.getBooleanProperty(PropertyNames.PROPERTY_DETACH_ALL_ON_COMMIT);
    }

    /**
     * Accessor for whether to copy objects on attaching.
     * @return Whether to copy objects on attaching.
     */
    public boolean getCopyOnAttach()
    {
        assertIsOpen();
        return ec.getBooleanProperty(PropertyNames.PROPERTY_COPY_ON_ATTACH);
    }

    /**
     * Acessor for the current FetchPlan
     * @return The FetchPlan
     */
    public FetchPlan getFetchPlan()
    {
        return fetchPlan;
    }

    /**
     * Accessor for whether to ignore the cache.
     * @return Whether to ignore the cache.
     */
    public boolean getIgnoreCache()
    {
        assertIsOpen();
        return ec.getBooleanProperty(PropertyNames.PROPERTY_IGNORE_CACHE);
    }

    /**
     * Accessor for whether the Persistence Manager is multithreaded.
     * @return Whether to run multithreaded.
     */
    public boolean getMultithreaded()
    {
        assertIsOpen();
        return ec.getBooleanProperty(PropertyNames.PROPERTY_MULTITHREADED);
    }

    /**
     * Mutator for whether to detach all objects on commit of the transaction.
     * @param flag Whether to detach all on commit.
     */
    public void setDetachAllOnCommit(boolean flag)
    {
        assertIsOpen();
        ec.setProperty(PropertyNames.PROPERTY_DETACH_ALL_ON_COMMIT, flag);
    }

    /**
     * Mutator for whether to copy objects on attach.
     * @param flag Whether to copy on attaching
     */
    public void setCopyOnAttach(boolean flag)
    {
        assertIsOpen();
        ec.setProperty(PropertyNames.PROPERTY_COPY_ON_ATTACH, flag);
    }

    /**
     * Mutator for whether to ignore the cache.
     * @param flag Whether to ignore the cache.
     */
    public void setIgnoreCache(boolean flag)
    {
        assertIsOpen();
        ec.setProperty(PropertyNames.PROPERTY_IGNORE_CACHE, flag);
    }

    /**
     * Mutator for whether the Persistence Manager is multithreaded.
     * @param flag Whether to run multithreaded.
     */
    public void setMultithreaded(boolean flag)
    {
        assertIsOpen();
        ec.setProperty(PropertyNames.PROPERTY_MULTITHREADED, flag);
    }

    /**
     * Mutator for the timeout to use for datastore reads.
     * @param timeout Datastore read timeout interval (millisecs)
     */
    public void setDatastoreReadTimeoutMillis(Integer timeout)
    {
        assertIsOpen();
        if (!ec.getStoreManager().getSupportedOptions().contains(StoreManager.OPTION_DATASTORE_TIMEOUT))
        {
            throw new JDOUnsupportedOptionException("This datastore doesn't support read timeouts");
        }
        ec.setProperty(PropertyNames.PROPERTY_DATASTORE_READ_TIMEOUT, timeout);
    }

    /**
     * Accessor for the datastore read timeout (milliseconds).
     * @return datastore read timeout
     */
    public Integer getDatastoreReadTimeoutMillis()
    {
        assertIsOpen();
        return ec.getIntProperty(PropertyNames.PROPERTY_DATASTORE_READ_TIMEOUT);
    }

    /**
     * Mutator for the timeout to use for datastore writes.
     * @param timeout Datastore write timeout interval (millisecs)
     */
    public void setDatastoreWriteTimeoutMillis(Integer timeout)
    {
        assertIsOpen();
        if (!ec.getStoreManager().getSupportedOptions().contains(StoreManager.OPTION_DATASTORE_TIMEOUT))
        {
            throw new JDOUnsupportedOptionException("This datastore doesn't support write timeouts");
        }
        ec.setProperty(PropertyNames.PROPERTY_DATASTORE_WRITE_TIMEOUT, timeout);
    }

    /**
     * Accessor for the datastore write timeout (milliseconds).
     * @return datastore write timeout
     */
    public Integer getDatastoreWriteTimeoutMillis()
    {
        assertIsOpen();
        return ec.getIntProperty(PropertyNames.PROPERTY_DATASTORE_WRITE_TIMEOUT);
    }

    /**
     * Accessor for the date on the datastore.
     * @return Date on the datastore
     */
    public Date getServerDate()
    {
        assertIsOpen();
        try
        {
            return ec.getStoreManager().getDatastoreDate();
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Method to close the Persistence Manager.
     */
    public void close()
    {
        pmf.releasePersistenceManager(this);
    }

    /**
     * Accessor for whether this ExecutionContext is closed.
     * @return Whether this manager is closed.
     */
    public boolean isClosed()
    {
        return closed;
    }

    /**
     * Accessor for the current transaction.
     * @return The transaction
     */
    public javax.jdo.Transaction currentTransaction()
    {
        assertIsOpen();
        return jdotx;
    }

    // ----------------------------- Eviction --------------------------------------------

    /**
     * JDO Convenience method to wrap any DataNucleus exceptions for the evict process.
     * @param obj The object to evict
     * @throws JDOUserException thrown if some instances could not be evicted
     */
    private void jdoEvict(Object obj)
    {
        try
        {
            ec.evictObject(obj);
        }
        catch (NucleusException ne)
        {
            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Method to evict an object from L1 cache.
     * @param obj The object
     * @throws JDOUserException thrown if some instances could not be evicted
     */
    public void evict(Object obj)
    {
        assertIsOpen();
        jdoEvict(obj);
    }

    /**
     * Method to evict all objects of the specified type (and optionaly its subclasses).
     * @param cls Type of persistable object
     * @param subclasses Whether to include subclasses
     */
    public void evictAll(boolean subclasses, Class cls)
    {
        assertIsOpen();
        try
        {
            ec.evictObjects(cls, subclasses);
        }
        catch (NucleusException ne)
        {
            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Method to evict an array of objects from L1 cache.
     * @param pcs The objects
     */
    public void evictAll(Object... pcs)
    {
        evictAll(Arrays.asList(pcs));
    }

    /**
     * Method to evict a collection of objects from L1 cache.
     * @param pcs The objects
     * @throws JDOUserException thrown if some instances could not be evicted
     */
    public void evictAll(Collection pcs)
    {
        assertIsOpen();
        ArrayList failures = new ArrayList();
        Iterator i = pcs.iterator();
        while (i.hasNext())
        {
            try
            {
                jdoEvict(i.next());
            }
            catch (JDOException e)
            {
                failures.add(e);
            }
        }
        if (!failures.isEmpty())
        {
            throw new JDOUserException(Localiser.msg("010036"), (Exception[]) failures.toArray(new Exception[failures.size()]));
        }
    }

    /**
     * Method to evict all current objects from L1 cache.
     */
    public void evictAll()
    {
        assertIsOpen();
        ec.evictAllObjects();
    }

    // --------------------------------- Refresh ----------------------------------------

    /**
     * JDO Convenience method to wrap any DataNucleus exceptions for the refresh process.
     * @param obj The object to refresh
     * @throws JDOUserException thrown if the object could not be refreshed
     */
    private void jdoRefresh(Object obj)
    {
        try
        {
            ec.refreshObject(obj);
        }
        catch (NucleusException ne)
        {
            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Method to do a refresh of an object.
     * @param obj The Object
     * @throws JDOUserException thrown if the object could not be refreshed
     */
    public void refresh(Object obj)
    {
        assertIsOpen();
        jdoRefresh(obj);
    }

    /**
     * Method to do a refresh of an array of objects.
     * @param pcs The Objects
     */
    public void refreshAll(Object... pcs)
    {
        refreshAll(Arrays.asList(pcs));
    }

    /**
     * Method to do a refresh of a collection of objects.
     * @param pcs The Objects
     * @throws JDOUserException thrown if instances could not be refreshed.
     */
    public void refreshAll(Collection pcs)
    {
        assertIsOpen();
        ArrayList failures = new ArrayList();
        Iterator iter = pcs.iterator();
        while (iter.hasNext())
        {
            try
            {
                jdoRefresh(iter.next());
            }
            catch (JDOException e)
            {
                failures.add(e);
            }
        }
        if (!failures.isEmpty())
        {
            throw new JDOUserException(Localiser.msg("010037"), (Exception[]) failures.toArray(new Exception[failures.size()]));
        }
    }

    /**
     * Method to do a refresh of all objects.
     * @throws JDOUserException thrown if instances could not be refreshed.
     */
    public void refreshAll()
    {
        assertIsOpen();
        try
        {
            ec.refreshAllObjects();
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Method to do a refresh of objects that failed verification in the
     * exception.
     * @param exc The JDO exception containing the objects that failed
     */
    public void refreshAll(JDOException exc)
    {
        Object obj = exc.getFailedObject();
        if (obj != null)
        {
            refresh(obj);
        }

        Throwable[] nested_excs = exc.getNestedExceptions();
        if (nested_excs != null)
        {
            for (int i = 0; i < nested_excs.length; i++)
            {
                if (nested_excs[i] instanceof JDOException)
                {
                    refreshAll((JDOException) nested_excs[i]);
                }
            }
        }
    }

    // ------------------------------- Retrieve ------------------------------------------

    /**
     * JDO Convenience method to wrap any DataNucleus exceptions for the retrieve process.
     * @param obj The object to retrieve
     * @param useFetchPlan whether to retrieve only the current fetch plan fields
     * @throws JDOUserException thrown if the object could not be retrieved
     */
    private void jdoRetrieve(Object obj, boolean useFetchPlan)
    {
        try
        {
            ec.retrieveObject(obj, useFetchPlan);
        }
        catch (NucleusException ne)
        {
            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Method to retrieve the fields of an object.
     * @param pc The object
     * @param useFetchPlan whether to retrieve only the current fetch plan fields
     */
    public void retrieve(Object pc, boolean useFetchPlan)
    {
        assertIsOpen();
        jdoRetrieve(pc, useFetchPlan);
    }

    /**
     * Method to retrieve the fields of an object.
     * @param pc The object
     */
    public void retrieve(Object pc)
    {
        retrieve(pc, false);
    }

    /**
     * Method to retrieve an array of objects.
     * @param pcs The objects
     */
    public void retrieveAll(Object... pcs)
    {
        retrieveAll(Arrays.asList(pcs), false);
    }

    /**
     * Retrieve field values of instances from the store. This tells the
     * <code>PersistenceManager</code> that the application intends to use the
     * instances, and their field values should be retrieved. The fields in the
     * current fetch group must be retrieved, and the implementation might
     * retrieve more fields than the current fetch group.
     * <P>
     * The <code>PersistenceManager</code> might use policy information about
     * the class to retrieve associated instances.
     * @param pcs the instances
     * @param useFetchPlan whether to retrieve only the current fetch plan fields
     * @deprecated
     */
    public void retrieveAll(Object[] pcs, boolean useFetchPlan)
    {
        retrieveAll(Arrays.asList(pcs), useFetchPlan);
    }

    /**
     * Retrieve field values of instances from the store.
     * As the equivalent method but arguments reversed for JDK1.5+.
     * @param useFetchPlan whether to retrieve only the current fetch plan fields
     * @param pcs the instances
     */
    public void retrieveAll(boolean useFetchPlan, Object... pcs)
    {
        retrieveAll(Arrays.asList(pcs), useFetchPlan);
    }

    /**
     * Retrieve field values of instances from the store. This tells the
     * <code>PersistenceManager</code> that the application intends to use the
     * instances, and their field values should be retrieved. The fields in the
     * current fetch group must be retrieved, and the implementation might
     * retrieve more fields than the current fetch group.
     * <P>
     * The <code>PersistenceManager</code> might use policy information about
     * the class to retrieve associated instances.
     * @param pcs the instances
     * @param useFetchPlan whether to retrieve only the current fetch plan fields
     */
    public void retrieveAll(Collection pcs, boolean useFetchPlan)
    {
        assertIsOpen();
        ArrayList failures = new ArrayList();
        Iterator i = pcs.iterator();
        while (i.hasNext())
        {
            try
            {
                jdoRetrieve(i.next(), useFetchPlan);
            }
            catch (RuntimeException e)
            {
                failures.add(e);
            }
        }
        if (!failures.isEmpty())
        {
            throw new JDOUserException(Localiser.msg("010038"), (Exception[]) failures.toArray(new Exception[failures.size()]));
        }
    }

    /**
     * Method to retrieve a collection of objects. Throws a JDOUserException if
     * instances could not be retrieved.
     * @param pcs The objects
     */
    public void retrieveAll(Collection pcs)
    {
        retrieveAll(pcs, false);
    }

    // ---------------------------------- Make Persistent ---------------------------------------

    /**
     * JDO Convenience method to wrap any DataNucleus exceptions for the makePersistent process.
     * @param obj The object to persist
     * @throws JDOUserException thrown if the object could not be persisted
     */
    private <T> T jdoMakePersistent(T obj)
    {
        try
        {
            return ec.persistObject(obj, false);
        }
        catch (NucleusException ne)
        {
            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * JDO method to persist an object.
     * Will also attach a previously detached object.
     * @param obj The object
     * @return The persisted object
     */
    public <T> T makePersistent(T obj)
    {
        assertIsOpen();
        assertWritable();
        if (obj == null)
        {
            return null;
        }

        // Persist the object
        return jdoMakePersistent(obj);
    }

    /**
     * JDO method to make persistent an array of objects.
     * @param pcs The objects to persist
     */
    public <T> T[] makePersistentAll(T... pcs)
    {
        return (T[]) makePersistentAll(Arrays.asList(pcs)).toArray();
    }

    /**
     * JDO method to make persistent a collection of objects.
     * Throws a JDOUserException if objects could not be made persistent.
     * @param pcs The objects to persist
     */
    public <T> Collection<T> makePersistentAll(Collection<T> pcs)
    {
        assertIsOpen();
        assertWritable();

        try
        {
            Object[] persistedPcs = ec.persistObjects(pcs.toArray());
            Collection persisted = new ArrayList();
            for (int i=0;i<persistedPcs.length;i++)
            {
                persisted.add(persistedPcs[i]);
            }
            return persisted;
        }
        catch (NucleusUserException nue)
        {
            Throwable[] failures = nue.getNestedExceptions();
            throw new JDOUserException(Localiser.msg("010039"), failures);
        }
    }

    // ------------------------------- Delete Persistent ------------------------------------------

    /**
     * JDO Convenience method to wrap any DataNucleus exceptions for the deletePersistent process.
     * @param obj The object to delete
     * @throws JDOUserException thrown if the object could not be deleted
     */
    private void jdoDeletePersistent(Object obj)
    {
        try
        {
            ec.deleteObject(obj);
        }
        catch (NucleusException ne)
        {
            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * JDO method to delete an object from the datastore.
     * @param obj The object
     */
    public void deletePersistent(Object obj)
    {
        assertIsOpen();
        assertWritable();

        jdoDeletePersistent(obj);
    }

    /**
     * JDO method to delete an array of objects from the datastore.
     * @param pcs The objects
     * @throws JDOUserException Thrown if one (or more) object cannot be deleted
     */
    public void deletePersistentAll(Object... pcs)
    {
        deletePersistentAll(Arrays.asList(pcs));
    }

    /**
     * JDO method to delete a collection of objects from the datastore. 
     * Throws a JDOUserException if objects could not be deleted.
     * @param pcs The objects
     * @throws JDOUserException Thrown if one (or more) object cannot be deleted
     */
    public void deletePersistentAll(Collection pcs)
    {
        assertIsOpen();
        assertWritable();

        try
        {
            ec.deleteObjects(pcs.toArray());
        }
        catch (NucleusUserException nue)
        {
            Throwable[] failures = nue.getNestedExceptions();
            throw new JDOUserException(Localiser.msg("010040"), failures);
        }
    }

    // -------------------------------- Make Transient -----------------------------------------

    /**
     * JDO Convenience method to wrap any DataNucleus exceptions for the makeTransient process.
     * @param pc The object to make transient
     * @param state FetchPlanState
     * @throws JDOUserException thrown if the object could not be made transient
     */
    private void jdoMakeTransient(Object pc, FetchPlanState state)
    {
        try
        {
            ec.makeObjectTransient(pc, state);
        }
        catch (NucleusException ne)
        {
            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Method to make transient an object allowing fetching using the fetch plan.
     * @param pc The object
     * @param useFetchPlan Whether to make transient all objects in the fetch plan
     */
    public void makeTransient(Object pc, boolean useFetchPlan)
    {
        assertIsOpen();

        FetchPlanState state = null;
        if (useFetchPlan)
        {
            // Create a state object to carry the processing state info
            state = new FetchPlanState();
        }
        jdoMakeTransient(pc, state);
    }

    /**
     * Method to make transient an object.
     * This doesn't use the fetch plan and just makes the specified object transient.
     * @param pc The object
     */
    public void makeTransient(Object pc)
    {
        makeTransient(pc, false);
    }

    /**
     * Method to make transient an array of objects.
     * @param pcs The objects
     */
    public void makeTransientAll(Object... pcs)
    {
        makeTransientAll(Arrays.asList(pcs));
    }

    /**
     * Method to make transient an array of objects.
     * @param pcs The objects
     * @param includeFetchPlan Whether to make transient all objects in the fetch plan
     */
    public void makeTransientAll(Object[] pcs, boolean includeFetchPlan)
    {
        makeTransientAll(Arrays.asList(pcs), includeFetchPlan);
    }

    /**
     * Method to make transient an array of objects.
     * @param includeFetchPlan Whether to make transient all objects in the fetch plan
     * @param pcs The objects
     */
    public void makeTransientAll(boolean includeFetchPlan, Object... pcs)
    {
        makeTransientAll(Arrays.asList(pcs), includeFetchPlan);
    }

    /**
     * Method to make transient a collection of objects.
     * @param pcs The objects
     * @param useFetchPlan Whether to use the fetch plan when making transient
     * @throws JDOUserException thrown if objects could not be made transient.
     */
    public void makeTransientAll(Collection pcs, boolean useFetchPlan)
    {
        assertIsOpen();
        ArrayList failures = new ArrayList();
        Iterator i = pcs.iterator();
        FetchPlanState state = null;
        if (useFetchPlan)
        {
            // Create a state object to carry the processing state info
            state = new FetchPlanState();
        }
        while (i.hasNext())
        {
            try
            {
                jdoMakeTransient(i.next(), state);
            }
            catch (RuntimeException e)
            {
                failures.add(e);
            }
        }
        if (!failures.isEmpty())
        {
            throw new JDOUserException(Localiser.msg("010041"), (Exception[]) failures.toArray(new Exception[failures.size()]));
        }
    }

    /**
     * Method to make transient a collection of objects.
     * @param pcs The objects
     * @throws JDOUserException thrown if objects could not be made transient.
     */
    public void makeTransientAll(Collection pcs)
    {
        makeTransientAll(pcs, false);
    }

    // ----------------------------------- Make Transactional --------------------------------------

    /**
     * JDO Convenience method to wrap any DataNucleus exceptions for the makeTransactional process.
     * @param pc The object to make transactional
     * @throws JDOUserException thrown if the object could not be made transactional
     */
    private void jdoMakeTransactional(Object pc)
    {
        try
        {
            ec.makeObjectTransactional(pc);
        }
        catch (NucleusException ne)
        {
            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Method to make transactional an object.
     * @param pc The object
     */
    public void makeTransactional(Object pc)
    {
        assertIsOpen();

        jdoMakeTransactional(pc);
    }

    /**
     * Method to make transactional an array of objects.
     * @param pcs The objects
     */
    public void makeTransactionalAll(Object... pcs)
    {
        makeTransactionalAll(Arrays.asList(pcs));
    }

    /**
     * Method to make transactional a collection of objects.
     * @param pcs The objects
     * @throws JDOUserException thrown if objects could not be made transactional
     */
    public void makeTransactionalAll(Collection pcs)
    {
        assertIsOpen();
        assertActiveTransaction();

        ArrayList failures = new ArrayList();
        Iterator i = pcs.iterator();
        while (i.hasNext())
        {
            try
            {
                jdoMakeTransactional(i.next());
            }
            catch (RuntimeException e)
            {
                failures.add(e);
            }
        }
        if (!failures.isEmpty())
        {
            throw new JDOUserException(Localiser.msg("010042"), (Exception[]) failures.toArray(new Exception[failures.size()]));
        }
    }

    // ------------------------------ Make NonTransactional -------------------------------------------

    /**
     * JDO Convenience method to wrap any DataNucleus exceptions for the makeNontransactional process.
     * @param obj The object to make nontransactional
     * @throws JDOUserException thrown if the object could not be made nontransactional
     */
    private void jdoMakeNontransactional(Object obj)
    {
        try
        {
            ec.makeObjectNontransactional(obj);
        }
        catch (NucleusException ne)
        {
            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Method to make nontransactional an object.
     * @param pc The object
     */
    public void makeNontransactional(Object pc)
    {
        assertIsOpen();
        if (pc == null)
        {
            return;
        }

        // if !transactional and !persistent
        if (!((Persistable) pc).dnIsTransactional() && !((Persistable) pc).dnIsPersistent())
        {
            throw new JDOUserException(Localiser.msg("011004"));
        }
        // if !transactional and persistent, do nothing
        if (!((Persistable) pc).dnIsTransactional() && ((Persistable) pc).dnIsPersistent())
        {
            return;
        }
        jdoMakeNontransactional(pc);
    }

    /**
     * Method to make nontransactional an array of objects.
     * @param pcs The objects.
     */
    public void makeNontransactionalAll(Object... pcs)
    {
        makeNontransactionalAll(Arrays.asList(pcs));
    }

    /**
     * Method to make nontransactional a collection of objects.
     * @param pcs The objects.
     * @throws JDOUserException thrown if objects could not be made nontransactional
     */
    public void makeNontransactionalAll(Collection pcs)
    {
        assertIsOpen();
        assertActiveTransaction();

        ArrayList failures = new ArrayList();
        Iterator i = pcs.iterator();
        while (i.hasNext())
        {
            try
            {
                jdoMakeNontransactional(i.next());
            }
            catch (RuntimeException e)
            {
                failures.add(e);
            }
        }
        if (!failures.isEmpty())
        {
            throw new JDOUserException(Localiser.msg("010043"), (Exception[]) failures.toArray(new Exception[failures.size()]));
        }
    }

    // ------------------------- Attach/Detach instances -----------------------

    /**
     * JDO Convenience method to wrap any DataNucleus exceptions for the detachCopy process.
     * @param obj The object to detach a copy of
     * @param state DetachState
     * @throws JDOUserException thrown if the object could not be detached
     */
    private <T> T jdoDetachCopy(T obj, FetchPlanState state)
    {
        ec.assertClassPersistable(obj.getClass());
        try
        {
            return ec.detachObjectCopy(obj, state);
        }
        catch (NucleusException ne)
        {
            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * JDO method to detach a persistent object.
     * If the object is of class that is not detachable a transient copy will be returned.
     * If the object is not persistent it will be persisted first before detaching a copy.
     * @param pc The object
     * @return The detached object
     */
    public <T> T detachCopy(T pc)
    {
        assertIsOpen();
        if (pc == null)
        {
            return null;
        }

        try
        {
            ec.assertClassPersistable(pc.getClass());
            assertReadable("detachCopy");

            return jdoDetachCopy(pc, new DetachState(ec.getApiAdapter()));
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Detach the specified objects from the <code>PersistenceManager</code>. 
     * The objects returned can be manipulated and re-attached with 
     * {@link #makePersistentAll(Object[])}.
     * The detached instances will be unmanaged copies of the specified parameters, 
     * and are suitable for serialization and manipulation outside of a JDO environment.
     * When detaching instances, only fields in the current {@link FetchPlan} will be 
     * traversed. Thus, to detach a graph of objects, relations to other persistent 
     * instances must either be in the <code>default-fetch-group</code>, or in the
     * current custom {@link FetchPlan}.
     * @param pcs the instances to detach
     * @return the detached instances
     */
    public <T> T[] detachCopyAll(T... pcs)
    {
        return (T[]) detachCopyAll(Arrays.asList(pcs)).toArray();
    }

    /**
     * Detach the specified objects from the <code>PersistenceManager</code>.
     * @param pcs the instances to detach
     * @return the detached instances
     * @see #detachCopyAll(Object[])
     */
    public <T> Collection<T> detachCopyAll(Collection<T> pcs)
    {
        assertIsOpen();
        assertReadable("detachCopyAll");

        // Detach the objects
        FetchPlanState state = new DetachState(ec.getApiAdapter());
        List detacheds = new ArrayList();
        for (Iterator it = pcs.iterator(); it.hasNext();)
        {
            Object obj = it.next();
            if (obj == null)
            {
                detacheds.add(null);
            }
            else
            {
                detacheds.add(jdoDetachCopy(obj, state));
            }
        }

        return detacheds;
    }

    // --------------------------------- Queries ----------------------------------------

    /**
     * Construct an empty query instance.
     * @return The query
     */
    public Query newQuery()
    {
        return newQuery("javax.jdo.query.JDOQL", null);
    }

    /**
     * Construct a query instance from another query. The parameter might be a
     * serialized/restored Query instance from the same JDO vendor but a
     * different execution environment, or the parameter might be currently
     * bound to a PersistenceManager from the same JDO vendor. Any of the
     * elements Class, Filter, IgnoreCache flag, Import declarations, Variable
     * declarations, Parameter declarations, and Ordering from the parameter
     * Query are copied to the new Query instance, but a candidate Collection or
     * Extent element is discarded.
     * @param obj The object to use in the query
     * @return The query
     */
    public Query newQuery(Object obj)
    {
        if (obj != null && obj instanceof JDOQuery)
        {
            String language = ((JDOQuery)obj).getLanguage();
            return newQuery(language, obj);
        }

        // TODO What situation is this ?
        return newQuery(null, obj);
    }

    /**
     * Construct a query instance using the specified Single-String query.
     * @param query The single-string query
     * @return The Query
     */
    public Query newQuery(String query)
    {
        return newQuery("javax.jdo.query.JDOQL", query);
    }

    /**
     * Construct a query instance using the specified language and the specified
     * query. The query instance will be of a class defined by the query language.
     * @param language The language parameter for the JDO Query language. This is by default 
     * "javax.jdo.query.JDOQL", but in JDO 2.0 can also be "javax.jdo.query.SQL", or vendor provided languages.
     * @param query The query object
     * @return The query
     */
    public Query newQuery(String language, Object query)
    {
        assertIsOpen();

        String queryLanguage = language;
        if (queryLanguage == null)
        {
            queryLanguage = "JDOQL";
        }
        else if (queryLanguage.equals("javax.jdo.query.JDOQL"))
        {
            queryLanguage = "JDOQL";
        }
        else if (queryLanguage.equals("javax.jdo.query.SQL"))
        {
            queryLanguage = "SQL";
        }
        else if (queryLanguage.equals("javax.jdo.query.JPQL"))
        {
            queryLanguage = "JPQL";
        }

        // Check that our store supports the language
        if (!ec.getStoreManager().supportsQueryLanguage(queryLanguage))
        {
            throw new JDOUserException(Localiser.msg("011006", queryLanguage));
        }

        org.datanucleus.store.query.Query internalQuery = null;
        try
        {
            if (query != null && query instanceof JDOQuery)
            {
                // Extract the internal query for generating the next query
                internalQuery = ec.getStoreManager().getQueryManager().newQuery(queryLanguage, ec,
                        ((JDOQuery)query).getInternalQuery());
            }
            else if (query instanceof String && StringUtils.isWhitespace((String)query))
            {
                internalQuery = ec.getStoreManager().getQueryManager().newQuery(queryLanguage, ec, null);
            }
            else
            {
                internalQuery = ec.getStoreManager().getQueryManager().newQuery(queryLanguage, ec, query);
            }
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
        return new JDOQuery(this, internalQuery, queryLanguage);
    }

    /**
     * Construct a query instance with the candidate class specified.
     * @param cls The class to query
     * @return The query
     */
    public Query newQuery(Class cls)
    {
        Query query = newQuery();
        query.setClass(cls);
        return query;
    }

    /**
     * Construct a query instance with the candidate Extent specified; the
     * candidate class is taken from the Extent.
     * @param cln The extent to query
     * @return The query
     */
    public Query newQuery(Extent cln)
    {
        Query query = newQuery();
        query.setClass(cln.getCandidateClass());
        query.setCandidates(cln);
        return query;
    }

    /**
     * Construct a query instance with the candidate class and candidate
     * Collection specified.
     * @param cls The class to query
     * @param cln The collection
     * @return The query
     */
    public Query newQuery(Class cls, Collection cln)
    {
        Query query = newQuery();
        query.setClass(cls);
        query.setCandidates(cln);
        return query;
    }

    /**
     * Construct a query instance with the candidate class and filter specified.
     * @param cls The class to query
     * @param filter A filter to apply
     * @return The query
     */
    public Query newQuery(Class cls, String filter)
    {
        Query query = newQuery();
        query.setClass(cls);
        query.setFilter(filter);
        return query;
    }

    /**
     * Construct a query instance with the candidate class, the candidate
     * Collection, and filter specified.
     * @param cls The class to query
     * @param cln A collection
     * @param filter A filter to apply
     * @return The query
     */
    public Query newQuery(Class cls, Collection cln, String filter)
    {
        Query query = newQuery();
        query.setClass(cls);
        query.setCandidates(cln);
        query.setFilter(filter);
        return query;
    }

    /**
     * Construct a query instance with the candidate Extent and filter
     * specified. The candidate class is taken from the Extent.
     * @param cln The extent to query
     * @param filter A filter to apply
     * @return The query
     */
    public Query newQuery(Extent cln, String filter)
    {
        Query query = newQuery();
        query.setClass(cln.getCandidateClass());
        query.setCandidates(cln);
        query.setFilter(filter);
        return query;
    }

    /**
     * Construct a query instance with the candidate class and the query name.
     * @param cls The class to query
     * @param queryName Name of the query.
     * @return The query
     */
    public Query newNamedQuery(Class cls, String queryName)
    {
        assertIsOpen();

        // Throw exception on incomplete input
        if (queryName == null)
        {
            throw new JDOUserException(Localiser.msg("011005", null, cls));
        }

        // Find the Query for the specified class
        ClassLoaderResolver clr = ec.getClassLoaderResolver();
        QueryMetaData qmd = ec.getMetaDataManager().getMetaDataForQuery(cls, clr, queryName);
        if (qmd == null)
        {
            throw new JDOUserException(Localiser.msg("011005", queryName, cls));
        }

        // Create the Query
        Query query = newQuery(qmd.getLanguage(), qmd.getQuery());
        if (cls != null)
        {
            query.setClass(cls);
            if (!ec.getStoreManager().managesClass(cls.getName()))
            {
                // Load the candidate class since not yet managed
                ec.getStoreManager().manageClasses(clr, cls.getName());
            }
        }

        // Optional args that should only be used with SQL
        if (qmd.getLanguage().equals(QueryLanguage.JDOQL.toString()) && 
            (qmd.isUnique() || qmd.getResultClass() != null))
        {
            throw new JDOUserException(Localiser.msg("011007", queryName));
        }
        if (qmd.isUnique())
        {
            query.setUnique(true);
        }
        if (qmd.getResultClass() != null)
        {
            // Set the result class, allowing for it being in the same package as the candidate
            Class resultCls = null;
            try
            {
                resultCls = clr.classForName(qmd.getResultClass());
            }
            catch (ClassNotResolvedException cnre)
            {
                if (cls != null)
                {
                    try
                    {
                        String resultClassName = cls.getPackage().getName() + "." + qmd.getResultClass();
                        resultCls = clr.classForName(resultClassName);
                    }
                    catch (ClassNotResolvedException cnre2)
                    {
                        throw new JDOUserException(Localiser.msg("011008", queryName, qmd.getResultClass()));
                    }
                }
            }
            query.setResultClass(resultCls);
        }

        // Add any extensions
        ExtensionMetaData[] extmds = qmd.getExtensions();
        if (extmds != null)
        {
            for (int i=0;i<extmds.length;i++)
            {
                if (extmds[i].getVendorName().equals(MetaData.VENDOR_NAME))
                {
                    query.addExtension(extmds[i].getKey(), extmds[i].getValue());
                }
            }
        }
        if (qmd.isUnmodifiable())
        {
            query.setUnmodifiable();
        }
        if (qmd.getFetchPlanName() != null)
        {
            // Apply any named FetchPlan to the query
            FetchPlanMetaData fpmd = ec.getMetaDataManager().getMetaDataForFetchPlan(qmd.getFetchPlanName());
            if (fpmd != null)
            {
                org.datanucleus.FetchPlan fp = new org.datanucleus.FetchPlan(ec, clr);
                fp.removeGroup(org.datanucleus.FetchPlan.DEFAULT);
                FetchGroupMetaData[] fgmds = fpmd.getFetchGroupMetaData();
                for (int i=0;i<fgmds.length;i++)
                {
                    fp.addGroup(fgmds[i].getName());
                }
                fp.setMaxFetchDepth(fpmd.getMaxFetchDepth());
                fp.setFetchSize(fpmd.getFetchSize());
                JDOQuery jdoquery = (JDOQuery)query;
                jdoquery.getInternalQuery().setFetchPlan(fp);
            }
        }

        return query;
    }

    /**
     * Method to return a "typesafe" query object, for the specified query type.
     * @param cls candidate class
     * @return The typesafe query object
     * @param <T> candidate type
     */
    public <T> TypesafeQuery<T> newTypesafeQuery(Class<T> cls)
    {
        return new JDOTypesafeQuery<T>(this, cls);
    }

    // ------------------------------- Extents ------------------------------------------

    /**
     * Extents are collections of datastore objects managed by the datastore,
     * not by explicit user operations on collections. Extent capability is a
     * boolean property of classes that are persistence capable. If an instance
     * of a class that has a managed extent is made persistent via reachability,
     * the instance is put into the extent implicitly.
     * @param pcClass The class to query
     * @param subclasses Whether to include subclasses in the query.
     * @return returns an Extent that contains all of the instances in the
     * parameter class, and if the subclasses flag is true, all of the instances
     * of the parameter class and its subclasses.
     * @param <T> candidate type
     */
    public <T> Extent<T> getExtent(Class<T> pcClass, boolean subclasses)
    {
        assertIsOpen();
        try
        {
            return new JDOExtent(this,ec.getExtent(pcClass, subclasses));
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }
    
    /**
     * Extents are collections of datastore objects managed by the datastore,
     * not by explicit user operations on collections. Extent capability is a
     * boolean property of classes that are persistence capable. If an instance
     * of a class that has a managed extent is made persistent via reachability,
     * the instance is put into the extent implicitly.
     * @param pcClass The class to query
     * @return returns an Extent that contains all of the instances in the
     * parameter class, and all of the instances of the parameter class and its
     * subclasses.
     */
    public <T> Extent<T> getExtent(Class<T> pcClass)
    {
        return getExtent(pcClass, true);
    }

    // ----------------------------- New Instances ----------------------------------

    /**
     * Method to generate an instance of an interface, abstract class, or concrete PC class.
     * @param pc The class of the interface or abstract class, or concrete class defined in MetaData
     * @return The instance of this type
     */
    public <T> T newInstance(Class<T> pc)
    {
        assertIsOpen();
        try
        {
            return ec.newInstance(pc);
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * This method returns an object id instance corresponding to the pcClass and key arguments.
     * It has 2 modes of operation. Where SingleFieldIdentity is being used the key is the
     * value of the key field. For all other cases the key is the String form of the object id instance.
     * @param pcClass Class of the persistable to create the OID for.
     * @param key Value of the key for SingleFieldIdentity, or toString() for other cases
     * @return The new object-id instance
     */
    public Object newObjectIdInstance(Class pcClass, Object key)
    {
        assertIsOpen();
        try
        {
            return ec.newObjectId(pcClass, key);
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    // ----------------------------- Object Retrieval by Id ----------------------------------

    /**
     * Accessor for the objects currently managed by this PM in the current transaction.
     * @return The managed objects
     */
    public Set getManagedObjects()
    {
        return ec.getManagedObjects();
    }

    /**
     * Accessor for the objects currently managed by this PM in the current transaction.
     * @param classes Classes that we want objects for
     * @return The managed objects
     */
    public Set getManagedObjects(Class... classes)
    {
        return ec.getManagedObjects(classes);
    }

    /**
     * Accessor for the objects currently managed by this PM in the current transaction.
     * @param states States that we want objects for
     * @return The managed objects
     */
    public Set getManagedObjects(EnumSet states)
    {
        if (states == null)
        {
            return null;
        }

        String[] stateNames = new String[states.size()];
        Iterator iter = states.iterator();
        int i = 0;
        while (iter.hasNext())
        {
            // Convert to strings to avoid using JDO class in Core
            ObjectState state = (ObjectState)iter.next();
            stateNames[i++] = state.toString();
        }
        return ec.getManagedObjects(stateNames);
    }

    /**
     * Accessor for the objects currently managed by this PM in the current transaction.
     * @param states States that we want objects for
     * @param classes Classes that we want objects for
     * @return The managed objects
     */
    public Set getManagedObjects(EnumSet states, Class... classes)
    {
        if (states == null)
        {
            return null;
        }

        String[] stateNames = new String[states.size()];
        Iterator iter = states.iterator();
        int i = 0;
        while (iter.hasNext())
        {
            // Convert to strings to avoid using JDO class in Core
            ObjectState state = (ObjectState)iter.next();
            stateNames[i++] = state.toString();
        }
        return ec.getManagedObjects(stateNames, classes);
    }

    /**
     * Accessor for an object given the object id.
     * @param id Id of the object.
     * @return The Object
     */
    public Object getObjectById(Object id)
    {
        return getObjectById(id, true);
    }

    /**
     * Accessor for an object given the object id.
     * @param id Id of the object.
     * @param validate Whether to validate the object state
     * @return The Object
     */
    public Object getObjectById(Object id, boolean validate)
    {
        assertIsOpen();
        if (id == null)
        {
            throw new JDONullIdentityException(Localiser.msg("010044"));
        }

        try
        {
            Object theId = id;
            if (id instanceof javax.jdo.identity.SingleFieldIdentity)
            {
                // Convert to DN own internal types
                theId = NucleusJDOHelper.getDataNucleusIdentityForSingleFieldIdentity((SingleFieldIdentity)id);
            }
            return ec.findObject(theId, validate, validate, null);
        }
        catch (NucleusException ne)
        {
            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Convenience method that exactly matches the behavior of calling 
     * pm.getObjectById (pm.newObjectIdInstance (cls, key), true).
     * @param cls Class of the persistable
     * @param key Value of the key field for SingleFieldIdentity, or the string value of the key otherwise
     * @return The object for this id.
     */
    public <T> T getObjectById(Class<T> cls, Object key)
    {
        return (T) getObjectById(newObjectIdInstance (cls, key), true);
    }

    /**
     * Accessor for the objects given the object ids.
     * @param validate Whether to validate the object state
     * @param oids Ids of the objects.
     * @return The Objects with these ids (in the same order)
     */
    public Object[] getObjectsById(boolean validate, Object... oids)
    {
        return getObjectsById(oids, validate);
    }

    /**
     * Accessor for the objects given the object ids.
     * @param oids Ids of the objects.
     * @param validate Whether to validate the object state
     * @return The Objects with these ids (in the same order)
     */
    public Object[] getObjectsById(Object[] oids, boolean validate)
    {
        assertIsOpen();
        if (oids == null)
        {
            throw new JDOUserException(Localiser.msg("011002"));
        }

        // Convert any explicit JDO single field ids to DN single field id
        Object[] theOids = new Object[oids.length];
        for (int i=0;i<oids.length;i++)
        {
            if (oids[i] != null)
            {
                if (oids[i] instanceof javax.jdo.identity.SingleFieldIdentity)
                {
                    // Convert to DN own internal types
                    theOids[i] = NucleusJDOHelper.getDataNucleusIdentityForSingleFieldIdentity((SingleFieldIdentity)oids[i]);
                }
                else
                {
                    theOids[i] = oids[i];
                }
            }
            else
            {
                theOids[i] = null;
            }
        }

        return ec.findObjects(theOids, validate);
    }

    /**
     * Accessor for the objects given the object ids, validating the objects.
     * @param oids Ids of the objects.
     * @return The Objects with these ids (in the same order)
     */
    public Object[] getObjectsById(Object... oids)
    {
        return getObjectsById(oids, true);
    }

    /**
     * Accessor for the objects given the object ids, validating the objects.
     * @param oids Ids of the objects.
     * @return The Objects with these ids (in the same order)
     */
    public Collection getObjectsById(Collection oids)
    {
        return getObjectsById(oids, true);
    }

    /**
     * Accessor for the objects given the object ids.
     * @param oids Ids of the objects.
     * @param validate Whether to validate the object state
     * @return The Objects with these ids (in the same order)
     */
    public Collection getObjectsById(Collection oids, boolean validate)
    {
        assertIsOpen();
        if (oids == null)
        {
            throw new JDOUserException(Localiser.msg("011002"));
        }
        else if (oids.size() == 0)
        {
            return Collections.EMPTY_LIST;
        }

        // Convert any explicit JDO single field ids to DN single field id
        Object[] oidArray = new Object[oids.size()];
        int j = 0;
        for (Object oid : oids)
        {
            Object id = oid;
            if (id != null)
            {
                if (id instanceof javax.jdo.identity.SingleFieldIdentity)
                {
                    // Convert to DN own internal types
                    id = NucleusJDOHelper.getDataNucleusIdentityForSingleFieldIdentity((SingleFieldIdentity)id);
                }
            }
            oidArray[j++] = id;
        }

        Object[] objs = ec.findObjects(oidArray, validate);

        Collection objects = new ArrayList(oids.size());
        for (int i=0;i<objs.length;i++)
        {
            objects.add(objs[i]);
        }
        return objects;
    }

    /**
     * Accessor for an object id given the object.
     * @param pc The object
     * @return The Object id
     */
    public Object getObjectId(Object pc)
    {
        assertIsOpen();
        if (pc != null && pc instanceof Persistable)
        {
            Persistable p = (Persistable) pc;
            if (p.dnIsPersistent() || p.dnIsDetached())
            {
                Object id = p.dnGetObjectId();
                if (id != null && id instanceof SingleFieldId)
                {
                    // Convert to javax.jdo.identity.*
                    id = NucleusJDOHelper.getSingleFieldIdentityForDataNucleusIdentity((SingleFieldId)id, pc.getClass());
                }
                return id;
            }
        }
        return null;
    }

    /**
     * Accessor for the object id of a transactional object given the object.
     * @param pc The object
     * @return The Object id
     */
    public Object getTransactionalObjectId(Object pc)
    {
        assertIsOpen();
        return ((Persistable) pc).dnGetTransactionalObjectId();
    }

    /**
     * Accessor for the class of the object id given the class of object.
     * @param cls The class name of the object
     * @return The class name of the object id
     */
    public Class getObjectIdClass(Class cls)
    {
        assertIsOpen();
        if (!ec.getNucleusContext().getApiAdapter().isPersistable(cls) || !hasPersistenceInformationForClass(cls))
        {
            return null;
        }

        ClassLoaderResolver clr = ec.getClassLoaderResolver();
        AbstractClassMetaData cmd = ec.getMetaDataManager().getMetaDataForClass(cls, clr);
        if (cmd.getIdentityType() == IdentityType.DATASTORE)
        {
            return ec.getNucleusContext().getIdentityManager().getDatastoreIdClass();
        }
        else if (cmd.getIdentityType() == IdentityType.APPLICATION)
        {
            try
            {
                return this.ec.getClassLoaderResolver().classForName(
                    ec.getMetaDataManager().getMetaDataForClass(cls, clr).getObjectidClass(), null);
            }
            catch (ClassNotResolvedException e)
            {
                String msg = Localiser.msg("011009", cls.getName());
                LOGGER.error(msg);
                throw new JDOException(msg);
            }
        }
        else
        {
            if (cmd.isRequiresExtent())
            {
                return ec.getNucleusContext().getIdentityManager().getDatastoreIdClass();
            }
            return SCOID.class;
        }
    }

    // ------------------------------------ User Objects -----------------------------------

    /**
     * Method to put a user object into the PersistenceManager. This is so that
     * multiple users can each have a user object for example. <I>The parameter
     * is not inspected or used in any way by the JDO implementation. </I>
     * @param key The key to store the user object under
     * @param value The object to store
     * @return The previous value for this key
     */
    public Object putUserObject(Object key, Object value)
    {
        assertIsOpen();
        if (key == null)
        {
            return null;
        }
        if (userObjectMap == null)
        {
            userObjectMap = new HashMap();
        }
        if (value == null)
        {
            // Remove the object
            return userObjectMap.remove(key);
        }
        // Put the object
        return userObjectMap.put(key, value);
    }

    /**
     * Method to get a user object from the PersistenceManager. This is for user
     * objects which are stored under a key. <I>The parameter is not inspected
     * or used in any way by the JDO implementation. </I>
     * @param key The key to store the user object under
     * @return The user object for that key
     */
    public Object getUserObject(Object key)
    {
        assertIsOpen();
        if (key == null)
        {
            return null;
        }
        if (userObjectMap == null)
        {
            return null;
        }
        return userObjectMap.get(key);
    }

    /**
     * Method to remove a user object from the PersistenceManager. This is for
     * user objects which are stored under a key. <I>The parameter is not
     * inspected or used in any way by the JDO implementation. </I>
     * @param key The key whose uder object is to be removed.
     * @return The user object that was removed
     */
    public Object removeUserObject(Object key)
    {
        assertIsOpen();
        if (key == null)
        {
            return null;
        }
        if (userObjectMap == null)
        {
            return null;
        }
        return userObjectMap.remove(key);
    }

    /**
     * The application might manage PersistenceManager instances by using an
     * associated object for bookkeeping purposes. These methods allow the user
     * to manage the associated object. <I>The parameter is not inspected or
     * used in any way by the JDO implementation. </I>
     * @param userObject The object
     */
    public void setUserObject(Object userObject)
    {
        assertIsOpen();
        this.userObject = userObject;
    }

    /**
     * The application might manage PersistenceManager instances by using an
     * associated object for bookkeeping purposes. These methods allow the user
     * to manage the associated object. <I>The parameter is not inspected or
     * used in any way by the JDO implementation. </I>
     * @return The user object
     */
    public Object getUserObject()
    {
        assertIsOpen();
        return userObject;
    }

    /**
     * This method flushes all dirty, new, and deleted instances to the
     * datastore. It has no effect if a transaction is not active. If a
     * datastore transaction is active, this method synchronizes the cache with
     * the datastore and reports any exceptions. If an optimistic transaction is
     * active, this method obtains a datastore connection and synchronizes the
     * cache with the datastore using this connection. The connection obtained
     * by this method is held until the end of the transaction.
     */
    public void flush()
    {
        assertIsOpen();
        try
        {
            // Flush all changes to the datastore without letting the store manager hold back
            ec.flush();
        }
        catch (NucleusException ne)
        {
            if (ne instanceof NucleusOptimisticException)
            {
                // Optimistic exceptions so convert all nested into JDOOptimisticVerificationException and return as single
                Throwable[] nested = ne.getNestedExceptions();
                JDOOptimisticVerificationException[] jdoNested = new JDOOptimisticVerificationException[nested.length];
                for (int i=0;i<nested.length;i++)
                {
                    jdoNested[i] = 
                        (JDOOptimisticVerificationException)NucleusJDOHelper.getJDOExceptionForNucleusException((NucleusException)nested[i]);
                }
                throw new JDOOptimisticVerificationException(ne.getMessage(), jdoNested);
            }

            // Convert any DataNucleus exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * This method validates the cache with the datastore. It has no effect if a transaction 
     * is not active. If a datastore transaction is active, this method verifies the consistency 
     * of instances in the cache against the datastore. An implementation might flush instances 
     * as if flush() were called, but it is not required to do so. 
     * If an optimistic transaction is active, this method obtains a datastore connection 
     * and verifies the consistency of the instances in the cache against the datastore. If any 
     * inconsistencies are detected, a JDOOptimisticVerificationException is thrown. This 
     * exception contains a nested JDOOptimisticVerificationException for each object 
     * that failed the consistency check. No datastore resources acquired during the execution 
     * of this method are held beyond the scope of this method.
     */
    public void checkConsistency()
    {
        assertIsOpen();

        // If transaction is not active do nothing
        if (!ec.getTransaction().isActive())
        {
            return;
        }

        if (ec.getTransaction().getOptimistic())
        {
            // TODO Implement checkConsistency() for optimistic transactions
            throw new JDOUserException("checkConsistency() not yet implemented for optimistic transactions");
        }

        flush();
    }

    // ------------------------------------- Sequence Management --------------------------------------

    /**
     * Method to retrieve a sequence by name. As per JDO2 spec section 12.14.
     * If the named sequence is not known, throws a JDOUserException.
     * @param sequenceName Fully qualified name of the sequence
     * @return The sequence
     */
    public Sequence getSequence(String sequenceName)
    {
        assertIsOpen();

        SequenceMetaData seqmd = ec.getMetaDataManager().getMetaDataForSequence(ec.getClassLoaderResolver(),sequenceName);
        if (seqmd == null)
        {
            throw new JDOUserException(Localiser.msg("017000", sequenceName));
        }

        Sequence seq = null;
        if (seqmd.getFactoryClass() != null)
        {
            // User has specified a factory class
            seq = pmf.getSequenceForFactoryClass(seqmd.getFactoryClass());
            if (seq == null)
            {
                // Create a new instance of the factory class and obtain the Sequence
                Class factory = ec.getClassLoaderResolver().classForName(seqmd.getFactoryClass());
                if (factory == null)
                {
                    throw new JDOUserException(Localiser.msg("017001", sequenceName, seqmd.getFactoryClass()));
                }

                Class[] argTypes = null;
                Object[] arguments = null;
                if (seqmd.getStrategy() != null)
                {
                    argTypes = new Class[2];
                    argTypes[0] = String.class;
                    argTypes[1] = String.class;
                    arguments = new Object[2];
                    arguments[0] = seqmd.getName();
                    arguments[1] = seqmd.getStrategy().toString();
                }
                else
                {
                    argTypes = new Class[1];
                    argTypes[0] = String.class;
                    arguments = new Object[1];
                    arguments[0] = seqmd.getName();
                }

                Method newInstanceMethod;
                try 
                {
                    // Obtain the sequence from the static "newInstance(...)" method
                    newInstanceMethod = factory.getMethod("newInstance", argTypes);
                    seq = (Sequence) newInstanceMethod.invoke(null, arguments);
                }
                catch (Exception e)
                {
                    throw new JDOUserException(Localiser.msg("017002", seqmd.getFactoryClass(), e.getMessage()));
                }

                // Register the sequence with the PMF
                pmf.addSequenceForFactoryClass(seqmd.getFactoryClass(), seq);
            }
        }
        else
        {
            NucleusSequence nucSeq = ec.getStoreManager().getNucleusSequence(ec, seqmd);
            seq = new JDOSequence(nucSeq);
        }
        return seq;
    }

    // ------------------------------------- Lifecycle Listeners --------------------------------------

    /**
     * Method to register a lifecycle listener as per JDO 2.0 spec 12.15.
     * @param listener The instance lifecycle listener to sends events to
     * @param classes The classes that it is interested in
     */
    public void addInstanceLifecycleListener(InstanceLifecycleListener listener, Class... classes)
    {
        assertIsOpen();
        if (listener == null)
        {
            return;
        }

        classes = LifecycleListenerForClass.canonicaliseClasses(classes);
        if (classes != null && classes.length == 0)
        {
            return;
        }

        ec.getCallbackHandler().addListener(listener, classes);
    }

    /**
     * Method to remove a currently registered lifecycle listener, as per JDO 2.0 spec 12.15.
     * @param listener The instance lifecycle listener to remove.
     */
    public void removeInstanceLifecycleListener(InstanceLifecycleListener listener)
    {
        assertIsOpen();
        ec.getCallbackHandler().removeListener(listener);
    }

    // -------------------------------------- Utility methods ----------------------------------------

    /**
     * Method to assert if this Persistence Manager is open.
     * @throws JDOFatalUserException if the PM is closed.
     */
    protected void assertIsOpen()
    {
        if (isClosed())
        {
            throw new JDOFatalUserException(Localiser.msg("011000"));
        }
    }

    /**
     * Method to assert if the current transaction is active.
     * Throws a TransactionNotActiveException if not active.
     */
    protected void assertActiveTransaction()
    {
        if (!ec.getTransaction().isActive())
        {
            throw new TransactionNotActiveException();
        }
    }

    /**
     * Method to assert if the current transaction is active or non transactional writes are allowed.
     * Throws a TransactionNotWritableException if not active and non transactional writes are disabled
     */
    protected void assertWritable()
    {
        if (!ec.getTransaction().isActive() && !ec.getTransaction().getNontransactionalWrite())
        {
            throw new TransactionNotWritableException();
        }
    }

    /**
     * Method to assert if no active transaction and nontransactionalRead is not set.
     * Throws JDOUserException if the tx is not active and no non-transactional read is available
     * @param operation The operation
     */
    protected void assertReadable(String operation)
    {
        if (!ec.getTransaction().isActive() && !ec.getTransaction().getNontransactionalRead())
        {
            throw new JDOUserException(Localiser.msg("011001", operation));
        }
    }

    /**
     * Utility method to check if the specified class has reachable metadata/annotations.
     * @param cls The class to check
     * @return Whether the class has reachable metadata/annotations.
     */
    protected boolean hasPersistenceInformationForClass(Class cls)
    {
        return ec.hasPersistenceInformationForClass(cls);
    }

    /**
     * Accessor for a connection on the datastore. See JDO 2.0 spec section 12.16
     * @return The JDO connection to the datastore
     * @see javax.jdo.PersistenceManager#getDataStoreConnection()
     */
    public JDOConnection getDataStoreConnection()
    {
        try
        {
            NucleusConnection nconn = ec.getStoreManager().getNucleusConnection(ec);
            if (ec.getStoreManager().isJdbcStore())
            {
                // JDO spec, if the datastore supports JDBC then the JDOConnection must implement Connection
                return new JDOConnectionJDBCImpl(nconn);
            }

            // Wrap the NucleusConnection with a JDOConnectionImpl
            return new JDOConnectionImpl(nconn);
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    // -------------------------------------- FetchGroup methods ----------------------------------------

    /**
     * Method to return a fetch group for the specified class, with the specified name.
     * @param cls The class
     * @param name The name of the fetch group
     * @return FetchGroup
     */
    public javax.jdo.FetchGroup getFetchGroup(Class cls, String name)
    {
        if (jdoFetchGroups == null)
        {
            jdoFetchGroups = new HashSet();
        }

        // Check our local groups
        Iterator<JDOFetchGroup> iter = jdoFetchGroups.iterator();
        while (iter.hasNext())
        {
            JDOFetchGroup jdoGrp = iter.next();
            if (jdoGrp.getName().equals(name) && jdoGrp.getType() == cls && !jdoGrp.isUnmodifiable())
            {
                // Modifiable group for this name and class already exists for this PM scope
                return jdoGrp;
            }
        }

        // Check the PMF now
        JDOFetchGroup jdoGrp = (JDOFetchGroup)getPersistenceManagerFactory().getFetchGroup(cls, name);
        if (jdoGrp != null)
        {
            // PMF returned us a group (maybe newly created) so put a copy in scope here
            // We actually copy the internal group to the OM, and have our own JDOFetchGroup here
            FetchGroup internalGrp = (jdoGrp).getInternalFetchGroup();
            FetchGroup internalCopy = new FetchGroup(internalGrp);
            jdoGrp = new JDOFetchGroup(internalCopy);
            ec.addInternalFetchGroup(internalCopy);
            jdoFetchGroups.add(jdoGrp);
            return jdoGrp;
        }

        // Create new FetchGroup - should never happen since PMF always creates one if not existent
        try
        {
            org.datanucleus.FetchGroup internalGrp = ec.getInternalFetchGroup(cls, name);
            jdoGrp = new JDOFetchGroup(internalGrp);
            jdoFetchGroups.add(jdoGrp);
            return jdoGrp;
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Set a persistence manager property. This can be a standard property
     * or a vendor-extension property. If a vendor-extension property is
     * not recognized, it is silently ignored.
     * @param propertyName name of property
     * @param value The value
     * @throws JDOUserException if the value is not supported for the property
     * @since JDO3.1
     */
    public void setProperty(String propertyName, Object value)
    {
        assertIsOpen();
        try
        {
            ec.setProperty(propertyName, value);
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Get the properties and associated values currently in effect for the
     * persistence manager. Changing entries in the map will not have affect
     * the configuration of the persistence manager.
     * @return map of properties in effect
     * @since JDO3.1
     */
    public Map<String, Object> getProperties()
    {
        assertIsOpen();
        Map<String, Object> pmProps = new HashMap<String, Object>();

        Map<String, Object> ecProps = ec.getProperties();
        Iterator<Map.Entry<String, Object>> propertiesIter = ecProps.entrySet().iterator();
        Configuration conf = ec.getNucleusContext().getConfiguration();
        while (propertiesIter.hasNext())
        {
            Map.Entry<String, Object> entry = propertiesIter.next();
            String ecPropName = entry.getKey();
            // Return with javax.jdo name if this is an internal property name
            String pmPropName = conf.getPropertyNameWithInternalPropertyName(ecPropName, "javax.jdo");
            pmProps.put(pmPropName!=null ? pmPropName : ecPropName, entry.getValue());
        }
        return pmProps;
    }

    /**
     * Get the names of the properties that are supported for use with the
     * persistence manager. These can be standard JDO properties, or can be
     * vendor-extension properties.
     * @return property names Names of the properties accepted
     * @since JDO3.1
     */
    public Set<String> getSupportedProperties()
    {
        assertIsOpen();
        return ec.getSupportedProperties();
    }

    /**
     * Convenience method to add a listener for transaction events (begin, commit, rollback).
     * @param listener The listener.
     */
    public void addTransactionEventListener(TransactionEventListener listener)
    {
        assertIsOpen();
        ec.getTransaction().bindTransactionEventListener(listener);
    }

    /**
     * Convenience method to remove the supplied listener from transaction event notification.
     * @param listener The listener
     */
    public void removeTransactionEventListener(TransactionEventListener listener)
    {
        assertIsOpen();
        ec.getTransaction().removeTransactionEventListener(listener);
    }
}