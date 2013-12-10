/**********************************************************************
Copyright (c) 2008 Andy Jefferson and others. All rights reserved.
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
     ...
 **********************************************************************/
package org.datanucleus.api.jdo;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.Extent;
import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;

/**
 * Implementation of a JDO persistence manager proxy.
 * JDO spec $11.3.1. Returning a thread-safe PM.
 * <p>
 * In JTA this delegates to an underlying PM, with the exception of close() which has no effect.
 * If there is a PM associated with the (JTA) transaction the proxy just delegates to whichever PM
 * is still associated. If there is no PM associated to the (JTA) transaction then a PM is created
 * and associated to the (JTA) txn. If there is no (JTA) txn currently associated then a new PM
 * is created
 * </p>
 * <p>
 * In RESOURCE_LOCAL, this delegates to the PM associated with an implementation defined thread-local
 * variable. The close() on the proxy clears the thread-local, so subsequent calls to 
 * getPersistenceManagerProxy() will create a new PM.
 * </p>
 */
public class JDOPersistenceManagerProxy implements PersistenceManager
{
    protected JDOPersistenceManagerFactory pmf;

    /**
     * Constructor for a PM proxy.
     */
    public JDOPersistenceManagerProxy(JDOPersistenceManagerFactory pmf)
    {
        this.pmf = pmf;
    }

    /**
     * Accessor for the delegate PM that we hand off to.
     * @return The delegate PM (from the PMF thread-local store)
     */
    protected PersistenceManager getPM()
    {
        return pmf.getPMProxyDelegate();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#close()
     */
    public void close()
    {
        pmf.clearPMProxyDelegate();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener,
     * java.lang.Class[])
     */
    public void addInstanceLifecycleListener(InstanceLifecycleListener listener, Class... classes)
    {
        getPM().addInstanceLifecycleListener(listener, classes);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#checkConsistency()
     */
    public void checkConsistency()
    {
        getPM().checkConsistency();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#currentTransaction()
     */
    public Transaction currentTransaction()
    {
        return getPM().currentTransaction();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistent(java.lang.Object)
     */
    public void deletePersistent(Object obj)
    {
        getPM().deletePersistent(obj);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.lang.Object[])
     */
    public void deletePersistentAll(Object... pcs)
    {
        getPM().deletePersistentAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.util.Collection)
     */
    public void deletePersistentAll(Collection pcs)
    {
        getPM().deletePersistentAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopy(java.lang.Object)
     */
    public <T> T detachCopy(T pc)
    {
        return getPM().detachCopy(pc);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.util.Collection)
     */
    public <T> Collection<T> detachCopyAll(Collection<T> pcs)
    {
        return getPM().detachCopyAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(T[])
     */
    public <T> T[] detachCopyAll(T... pcs)
    {
        return getPM().detachCopyAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evict(java.lang.Object)
     */
    public void evict(Object obj)
    {
        getPM().evict(obj);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll()
     */
    public void evictAll()
    {
        getPM().evictAll();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Object[])
     */
    public void evictAll(Object... pcs)
    {
        getPM().evictAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.util.Collection)
     */
    public void evictAll(Collection pcs)
    {
        getPM().evictAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(boolean, java.lang.Class)
     */
    public void evictAll(boolean subclasses, Class cls)
    {
        getPM().evictAll(subclasses, cls);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#flush()
     */
    public void flush()
    {
        getPM().flush();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getCopyOnAttach()
     */
    public boolean getCopyOnAttach()
    {
        return getPM().getCopyOnAttach();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDataStoreConnection()
     */
    public JDOConnection getDataStoreConnection()
    {
        return getPM().getDataStoreConnection();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDetachAllOnCommit()
     */
    public boolean getDetachAllOnCommit()
    {
        return getPM().getDetachAllOnCommit();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    public <T> Extent<T> getExtent(Class<T> pcClass)
    {
        return getPM().getExtent(pcClass);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
     */
    public <T> Extent<T> getExtent(Class<T> pcClass, boolean subclasses)
    {
        return getPM().getExtent(pcClass, subclasses);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchGroup(java.lang.Class, java.lang.String)
     */
    public FetchGroup getFetchGroup(Class arg0, String arg1)
    {
        return getPM().getFetchGroup(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchPlan()
     */
    public FetchPlan getFetchPlan()
    {
        return getPM().getFetchPlan();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getIgnoreCache()
     */
    public boolean getIgnoreCache()
    {
        return getPM().getIgnoreCache();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects()
     */
    public Set getManagedObjects()
    {
        return getPM().getManagedObjects();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet)
     */
    public Set getManagedObjects(EnumSet<ObjectState> states)
    {
        return getPM().getManagedObjects(states);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.lang.Class[])
     */
    public Set getManagedObjects(Class... classes)
    {
        return getPM().getManagedObjects(classes);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet, java.lang.Class[])
     */
    public Set getManagedObjects(EnumSet<ObjectState> states, Class... classes)
    {
        return getPM().getManagedObjects(states, classes);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getMultithreaded()
     */
    public boolean getMultithreaded()
    {
        return getPM().getMultithreaded();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object)
     */
    public Object getObjectById(Object id)
    {
        return getPM().getObjectById(id);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    public Object getObjectById(Object id, boolean validate)
    {
        return getPM().getObjectById(id, validate);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class, java.lang.Object)
     */
    public <T> T getObjectById(Class<T> cls, Object key)
    {
        return getPM().getObjectById(cls, key);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectId(java.lang.Object)
     */
    public Object getObjectId(Object pc)
    {
        return getPM().getObjectId(pc);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectIdClass(java.lang.Class)
     */
    public Class getObjectIdClass(Class cls)
    {
        return getPM().getObjectIdClass(cls);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection)
     */
    public Collection getObjectsById(Collection oids)
    {
        return getPM().getObjectsById(oids);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[])
     */
    public Object[] getObjectsById(Object... oids)
    {
        return getPM().getObjectsById(oids);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection, boolean)
     */
    public Collection getObjectsById(Collection oids, boolean validate)
    {
        return getPM().getObjectsById(oids, validate);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[], boolean)
     */
    public Object[] getObjectsById(Object[] oids, boolean validate)
    {
        return getPM().getObjectsById(validate, oids);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(boolean, java.lang.Object[])
     */
    public Object[] getObjectsById(boolean validate, Object... oids)
    {
        return getPM().getObjectsById(validate, oids);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getPersistenceManagerFactory()
     */
    public PersistenceManagerFactory getPersistenceManagerFactory()
    {
        return getPM().getPersistenceManagerFactory();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSequence(java.lang.String)
     */
    public Sequence getSequence(String sequenceName)
    {
        return getPM().getSequence(sequenceName);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getServerDate()
     */
    public Date getServerDate()
    {
        return getPM().getServerDate();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getTransactionalObjectId(java.lang.Object)
     */
    public Object getTransactionalObjectId(Object pc)
    {
        return getPM().getTransactionalObjectId(pc);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject()
     */
    public Object getUserObject()
    {
        return getPM().getUserObject();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject(java.lang.Object)
     */
    public Object getUserObject(Object key)
    {
        return getPM().getUserObject(key);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#isClosed()
     */
    public boolean isClosed()
    {
        return getPM().isClosed();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactional(java.lang.Object)
     */
    public void makeNontransactional(Object pc)
    {
        getPM().makeNontransactional(pc);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.lang.Object[])
     */
    public void makeNontransactionalAll(Object... pcs)
    {
        getPM().makeNontransactionalAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.util.Collection)
     */
    public void makeNontransactionalAll(Collection arg0)
    {
        getPM().makeNontransactionalAll(arg0);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistent(java.lang.Object)
     */
    public <T> T makePersistent(T obj)
    {
        return getPM().makePersistent(obj);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(T[])
     */
    public <T> T[] makePersistentAll(T... arg0)
    {
        return getPM().makePersistentAll(arg0);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.util.Collection)
     */
    public <T> Collection<T> makePersistentAll(Collection<T> arg0)
    {
        return getPM().makePersistentAll(arg0);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactional(java.lang.Object)
     */
    public void makeTransactional(Object arg0)
    {
        getPM().makeTransactional(arg0);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.lang.Object[])
     */
    public void makeTransactionalAll(Object... arg0)
    {
        getPM().makeTransactionalAll(arg0);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.util.Collection)
     */
    public void makeTransactionalAll(Collection arg0)
    {
        getPM().makeTransactionalAll(arg0);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object)
     */
    public void makeTransient(Object pc)
    {
        getPM().makeTransient(pc);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
     */
    public void makeTransient(Object pc, boolean useFetchPlan)
    {
        getPM().makeTransient(pc, useFetchPlan);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[])
     */
    public void makeTransientAll(Object... pcs)
    {
        getPM().makeTransientAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection)
     */
    public void makeTransientAll(Collection pcs)
    {
        getPM().makeTransientAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    public void makeTransientAll(Object[] pcs, boolean includeFetchPlan)
    {
        getPM().makeTransientAll(includeFetchPlan, pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(boolean, java.lang.Object[])
     */
    public void makeTransientAll(boolean includeFetchPlan, Object... pcs)
    {
        getPM().makeTransientAll(includeFetchPlan, pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    public void makeTransientAll(Collection pcs, boolean useFetchPlan)
    {
        getPM().makeTransientAll(pcs, useFetchPlan);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newInstance(java.lang.Class)
     */
    public <T> T newInstance(Class<T> pc)
    {
        return getPM().newInstance(pc);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newNamedQuery(java.lang.Class, java.lang.String)
     */
    public Query newNamedQuery(Class cls, String filter)
    {
        return getPM().newNamedQuery(cls, filter);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newObjectIdInstance(java.lang.Class, java.lang.Object)
     */
    public Object newObjectIdInstance(Class pcClass, Object key)
    {
        return getPM().newObjectIdInstance(pcClass, key);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery()
     */
    public Query newQuery()
    {
        return getPM().newQuery();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Object)
     */
    public Query newQuery(Object obj)
    {
        return getPM().newQuery(obj);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String)
     */
    public Query newQuery(String query)
    {
        return getPM().newQuery(query);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class)
     */
    public Query newQuery(Class cls)
    {
        return getPM().newQuery(cls);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent)
     */
    public Query newQuery(Extent cln)
    {
        return getPM().newQuery(cln);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String, java.lang.Object)
     */
    public Query newQuery(String language, Object query)
    {
        return getPM().newQuery(language, query);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection)
     */
    public Query newQuery(Class cls, Collection cln)
    {
        return getPM().newQuery(cls, cln);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
     */
    public Query newQuery(Class cls, String filter)
    {
        return getPM().newQuery(cls, filter);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent, java.lang.String)
     */
    public Query newQuery(Extent cln, String filter)
    {
        return getPM().newQuery(cln, filter);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    public Query newQuery(Class cls, Collection cln, String filter)
    {
        return getPM().newQuery(cls, cln, filter);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#putUserObject(java.lang.Object, java.lang.Object)
     */
    public Object putUserObject(Object key, Object value)
    {
        return getPM().putUserObject(key, value);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refresh(java.lang.Object)
     */
    public void refresh(Object obj)
    {
        getPM().refresh(obj);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll()
     */
    public void refreshAll()
    {
        getPM().refreshAll();
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.lang.Object[])
     */
    public void refreshAll(Object... pcs)
    {
        getPM().refreshAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.util.Collection)
     */
    public void refreshAll(Collection pcs)
    {
        getPM().refreshAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(javax.jdo.JDOException)
     */
    public void refreshAll(JDOException exc)
    {
        getPM().refreshAll(exc);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    public void removeInstanceLifecycleListener(InstanceLifecycleListener listener)
    {
        getPM().removeInstanceLifecycleListener(listener);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeUserObject(java.lang.Object)
     */
    public Object removeUserObject(Object key)
    {
        return getPM().removeUserObject(key);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object)
     */
    public void retrieve(Object pc)
    {
        getPM().retrieve(pc);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
     */
    public void retrieve(Object pc, boolean fgOnly)
    {
        getPM().retrieve(pc, fgOnly);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    public void retrieveAll(Collection pcs)
    {
        getPM().retrieveAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[])
     */
    public void retrieveAll(Object... pcs)
    {
        getPM().retrieveAll(pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    public void retrieveAll(Collection pcs, boolean fgOnly)
    {
        getPM().retrieveAll(pcs, fgOnly);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[], boolean)
     */
    public void retrieveAll(Object[] pcs, boolean fgOnly)
    {
        getPM().retrieveAll(fgOnly, pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(boolean, java.lang.Object[])
     */
    public void retrieveAll(boolean fgOnly, Object... pcs)
    {
        getPM().retrieveAll(fgOnly, pcs);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setCopyOnAttach(boolean)
     */
    public void setCopyOnAttach(boolean flag)
    {
        getPM().setCopyOnAttach(flag);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDetachAllOnCommit(boolean)
     */
    public void setDetachAllOnCommit(boolean flag)
    {
        getPM().setDetachAllOnCommit(flag);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(boolean flag)
    {
        getPM().setIgnoreCache(flag);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag)
    {
        getPM().setMultithreaded(flag);
    }

    /*
     * (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setUserObject(java.lang.Object)
     */
    public void setUserObject(Object userObject)
    {
        getPM().setUserObject(userObject);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDatastoreReadTimeoutMillis()
     */
    public Integer getDatastoreReadTimeoutMillis()
    {
        return getPM().getDatastoreReadTimeoutMillis();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDatastoreReadTimeoutMillis(java.lang.Integer)
     */ 
    public void setDatastoreReadTimeoutMillis(Integer intvl)
    {
        getPM().setDatastoreReadTimeoutMillis(intvl);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDatastoreWriteTimeoutMillis()
     */
    public Integer getDatastoreWriteTimeoutMillis()
    {
        return getPM().getDatastoreWriteTimeoutMillis();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDatastoreWriteTimeoutMillis(java.lang.Integer)
     */
    public void setDatastoreWriteTimeoutMillis(Integer intvl)
    {
        getPM().setDatastoreWriteTimeoutMillis(intvl);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getProperties()
     */
    public Map<String, Object> getProperties()
    {
        return getPM().getProperties();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSupportedProperties()
     */
    public Set<String> getSupportedProperties()
    {
        return getPM().getSupportedProperties();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String arg0, Object arg1)
    {
        getPM().setProperty(arg0, arg1);
    }
}