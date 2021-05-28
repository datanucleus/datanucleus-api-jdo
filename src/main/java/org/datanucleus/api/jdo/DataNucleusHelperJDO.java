/**********************************************************************
Copyright (c) 2021 Andy Jefferson and others. All rights reserved.
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

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;

import javax.jdo.JDOCanRetryException;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOQueryInterruptedException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.identity.ByteIdentity;
import javax.jdo.identity.CharIdentity;
import javax.jdo.identity.IntIdentity;
import javax.jdo.identity.LongIdentity;
import javax.jdo.identity.ObjectIdentity;
import javax.jdo.identity.ShortIdentity;
import javax.jdo.identity.SingleFieldIdentity;
import javax.jdo.identity.StringIdentity;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.ExecutionContext;
import org.datanucleus.api.jdo.exceptions.ClassNotPersistenceCapableException;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.exceptions.ClassNotPersistableException;
import org.datanucleus.exceptions.DatastoreReadOnlyException;
import org.datanucleus.exceptions.NoPersistenceInformationException;
import org.datanucleus.exceptions.NucleusCanRetryException;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.exceptions.NucleusOptimisticException;
import org.datanucleus.exceptions.NucleusUnsupportedOptionException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.exceptions.TransactionNotActiveException;
import org.datanucleus.exceptions.TransactionNotReadableException;
import org.datanucleus.exceptions.TransactionNotWritableException;
import org.datanucleus.identity.ByteId;
import org.datanucleus.identity.CharId;
import org.datanucleus.identity.IntId;
import org.datanucleus.identity.LongId;
import org.datanucleus.identity.ObjectId;
import org.datanucleus.identity.ShortId;
import org.datanucleus.identity.SingleFieldId;
import org.datanucleus.identity.StringId;
import org.datanucleus.metadata.ClassMetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.store.query.QueryInterruptedException;
import org.datanucleus.store.query.QueryTimeoutException;
import org.datanucleus.util.ClassUtils;

/**
 * Helper class for accessing DataNucleus internals from a JDO environment.
 * This is a supplement to the JDO API class JDOHelper.
 */
public class DataNucleusHelperJDO
{
    /**
     * Convenience accessor for the query results cache.
     * @param pmf The PMF
     * @return The Query results cache
     */
    public static JDOQueryCache getQueryResultCache(PersistenceManagerFactory pmf)
    {
        return ((JDOPersistenceManagerFactory)pmf).getQueryCache();
    }

    /**
     * Method to convert a DataNucleus "identity" for a single-field application id type to the JDO provided SingleFieldIdentity type.
     * TODO The targetClass "name" is part of the dnid, so could minimise the input args possibly
     * @param dnid DataNucleus identity
     * @param targetClass The target class
     * @return The JDO SingleFieldIdentity
     */
    public static SingleFieldIdentity getSingleFieldIdentityForDataNucleusIdentity(SingleFieldId dnid, Class targetClass)
    {
        if (dnid instanceof LongId)
        {
            return new LongIdentity(targetClass, dnid.toString());
        }
        else if (dnid instanceof IntId)
        {
            return new IntIdentity(targetClass, dnid.toString());
        }
        else if (dnid instanceof ShortId)
        {
            return new ShortIdentity(targetClass, dnid.toString());
        }
        else if (dnid instanceof ByteId)
        {
            return new ByteIdentity(targetClass, dnid.toString());
        }
        else if (dnid instanceof StringId)
        {
            return new StringIdentity(targetClass, dnid.toString());
        }
        else if (dnid instanceof CharId)
        {
            return new CharIdentity(targetClass, dnid.toString());
        }
        return new ObjectIdentity(targetClass, dnid.getKeyAsObject());
    }

    public static SingleFieldId getDataNucleusIdentityForSingleFieldIdentity(SingleFieldIdentity sfid)
    {
        if (sfid instanceof javax.jdo.identity.LongIdentity)
        {
            return new LongId(sfid.getTargetClass(), sfid.toString());
        }
        else if (sfid instanceof javax.jdo.identity.IntIdentity)
        {
            return new IntId(sfid.getTargetClass(), sfid.toString());
        }
        else if (sfid instanceof javax.jdo.identity.ShortIdentity)
        {
            return new ShortId(sfid.getTargetClass(), sfid.toString());
        }
        else if (sfid instanceof javax.jdo.identity.ByteIdentity)
        {
            return new ByteId(sfid.getTargetClass(), sfid.toString());
        }
        else if (sfid instanceof javax.jdo.identity.StringIdentity)
        {
            return new StringId(sfid.getTargetClass(), sfid.toString());
        }
        else if (sfid instanceof javax.jdo.identity.CharIdentity)
        {
            return new CharId(sfid.getTargetClass(), sfid.toString());
        }
        return new ObjectId(sfid.getTargetClass(), sfid.getKeyAsObject());
    }

    public static String getObjectIdClassForInputIdClass(String objectIdClass)
    {
        if (objectIdClass != null && objectIdClass.startsWith("javax.jdo.identity"))
        {
            if (objectIdClass.equals("javax.jdo.identity.ByteIdentity"))
            {
                return ClassNameConstants.IDENTITY_SINGLEFIELD_BYTE;
            }
            else if (objectIdClass.equals("javax.jdo.identity.CharIdentity"))
            {
                return ClassNameConstants.IDENTITY_SINGLEFIELD_CHAR;
            }
            else if (objectIdClass.equals("javax.jdo.identity.IntIdentity"))
            {
                return ClassNameConstants.IDENTITY_SINGLEFIELD_INT;
            }
            else if (objectIdClass.equals("javax.jdo.identity.LongIdentity"))
            {
                return ClassNameConstants.IDENTITY_SINGLEFIELD_LONG;
            }
            else if (objectIdClass.equals("javax.jdo.identity.ShortIdentity"))
            {
                return ClassNameConstants.IDENTITY_SINGLEFIELD_SHORT;
            }
            else if (objectIdClass.equals("javax.jdo.identity.StringIdentity"))
            {
                return ClassNameConstants.IDENTITY_SINGLEFIELD_STRING;
            }
            else if (objectIdClass.equals("javax.jdo.identity.ObjectIdentity"))
            {
                return ClassNameConstants.IDENTITY_SINGLEFIELD_OBJECT;
            }
        }
        return objectIdClass;
    }

    // ------------------------------------ MetaData --------------------------------

    /**
     * Accessor for the MetaData for the specified class
     * @param pmf PersistenceManager factory
     * @param cls The class
     * @return The MetaData for the class
     */
    public static ClassMetaData getMetaDataForClass(PersistenceManagerFactory pmf, Class cls)
    {
        if (pmf == null || cls == null)
        {
            return null;
        }
        if (!(pmf instanceof JDOPersistenceManagerFactory))
        {
            return null;
        }

        JDOPersistenceManagerFactory myPMF = (JDOPersistenceManagerFactory)pmf;
        MetaDataManager mdmgr = myPMF.getNucleusContext().getMetaDataManager();
        return (ClassMetaData)mdmgr.getMetaDataForClass(cls, myPMF.getNucleusContext().getClassLoaderResolver(null));
    }

    /**
     * Accessor for the names of the classes that have MetaData for this PMF.
     * @param pmf The PMF
     * @return The class names
     */
    public static String[] getClassesWithMetaData(PersistenceManagerFactory pmf)
    {
        if (pmf == null || !(pmf instanceof JDOPersistenceManagerFactory))
        {
            return null;
        }

        JDOPersistenceManagerFactory myPMF = (JDOPersistenceManagerFactory)pmf;
        Collection classes = myPMF.getNucleusContext().getMetaDataManager().getClassesWithMetaData();
        return (String[])classes.toArray(new String[classes.size()]);
    }

    // ------------------------------ Object Lifecycle --------------------------------

    /**
     * Accessor for the jdoDetachedState field of a detached object.
     * The returned array is made up of :
     * <ul>
     * <li>0 - the identity of the object</li>
     * <li>1 - the version of the object (upon detach)</li>
     * <li>2 - loadedFields BitSet</li>
     * <li>3 - dirtyFields BitSet</li>
     * </ul>
     * @param obj The detached object
     * @return The detached state
     */
    public static Object[] getDetachedStateForObject(Object obj)
    {
        if (obj == null || !JDOHelper.isDetached(obj))
        {
            return null;
        }
        try
        {
            Field fld = ClassUtils.getFieldForClass(obj.getClass(), "dnDetachedState");
            fld.setAccessible(true);
            return (Object[]) fld.get(obj);
        }
        catch (Exception e)
        {
            throw new NucleusException("Exception accessing dnDetachedState field", e);
        }
    }

    /**
     * Accessor for the names of the dirty fields of the persistable object.
     * @param obj The persistable object
     * @param pm The Persistence Manager (only required if the object is detached)
     * @return Names of the dirty fields
     */
    public static String[] getDirtyFields(Object obj, PersistenceManager pm)
    {
        if (obj == null || !(obj instanceof Persistable))
        {
            return null;
        }
        Persistable pc = (Persistable)obj;

        if (JDOHelper.isDetached(pc))
        {
            ExecutionContext ec = ((JDOPersistenceManager)pm).getExecutionContext();

            // Temporarily attach a StateManager to access the detached field information
            ObjectProvider op = ec.getNucleusContext().getObjectProviderFactory().newForDetached(ec, pc, JDOHelper.getObjectId(pc), null);
            pc.dnReplaceStateManager(op);
            op.retrieveDetachState(op);
            String[] dirtyFieldNames = op.getDirtyFieldNames();
            pc.dnReplaceStateManager(null);

            return dirtyFieldNames;
        }

        ExecutionContext ec = ((JDOPersistenceManager)pm).getExecutionContext();
        ObjectProvider op = ec.findObjectProvider(pc);
        if (op == null)
        {
            return null;
        }
        return op.getDirtyFieldNames();
    }

    /**
     * Accessor for the names of the loaded fields of the persistable object.
     * @param obj Persistable object
     * @param pm The Persistence Manager (only required if the object is detached)
     * @return Names of the loaded fields
     */
    public static String[] getLoadedFields(Object obj, PersistenceManager pm)
    {
        if (obj == null || !(obj instanceof Persistable))
        {
            return null;
        }
        Persistable pc = (Persistable)obj;

        if (JDOHelper.isDetached(pc))
        {
            // Temporarily attach a StateManager to access the detached field information
            ExecutionContext ec = ((JDOPersistenceManager)pm).getExecutionContext();
            ObjectProvider op = ec.getNucleusContext().getObjectProviderFactory().newForDetached(ec, pc, JDOHelper.getObjectId(pc), null);
            pc.dnReplaceStateManager(op);
            op.retrieveDetachState(op);
            String[] loadedFieldNames = op.getLoadedFieldNames();
            pc.dnReplaceStateManager(null);

            return loadedFieldNames;
        }

        ExecutionContext ec = ((JDOPersistenceManager)pm).getExecutionContext();
        ObjectProvider op = ec.findObjectProvider(pc);
        if (op == null)
        {
            return null;
        }
        return op.getLoadedFieldNames();
    }

    /**
     * Accessor for whether the specified member (field/property) of the passed persistable object is loaded.
     * @param obj The persistable object
     * @param memberName Name of the field/property
     * @param pm PersistenceManager (if the object is detached)
     * @return Whether the member is loaded
     */
    public static Boolean isLoaded(Object obj, String memberName, PersistenceManager pm)
    {
        if (obj == null || !(obj instanceof Persistable))
        {
            return null;
        }
        Persistable pc = (Persistable)obj;

        if (JDOHelper.isDetached(pc))
        {
            // Temporarily attach a StateManager to access the detached field information
            ExecutionContext ec = ((JDOPersistenceManager)pm).getExecutionContext();
            ObjectProvider op = ec.getNucleusContext().getObjectProviderFactory().newForDetached(ec, pc, JDOHelper.getObjectId(pc), null);
            pc.dnReplaceStateManager(op);
            op.retrieveDetachState(op);
            int position = op.getClassMetaData().getAbsolutePositionOfMember(memberName);
            boolean loaded = op.isFieldLoaded(position);
            pc.dnReplaceStateManager(null);

            return loaded;
        }

        ExecutionContext ec = (ExecutionContext) pc.dnGetExecutionContext();
        ObjectProvider op = ec.findObjectProvider(pc);
        if (op == null)
        {
            return null;
        }
        int position = op.getClassMetaData().getAbsolutePositionOfMember(memberName);
        return op.isFieldLoaded(position);
    }

    /**
     * Accessor for whether the specified member (field/property) of the passed persistable object is dirty.
     * @param obj The persistable object
     * @param memberName Name of the field/property
     * @param pm PersistenceManager (if the object is detached)
     * @return Whether the member is dirty
     */
    public static Boolean isDirty(Object obj, String memberName, PersistenceManager pm)
    {
        if (obj == null || !(obj instanceof Persistable))
        {
            return null;
        }
        Persistable pc = (Persistable)obj;

        if (JDOHelper.isDetached(pc))
        {
            // Temporarily attach a StateManager to access the detached field information
            ExecutionContext ec = ((JDOPersistenceManager)pm).getExecutionContext();
            ObjectProvider op = ec.getNucleusContext().getObjectProviderFactory().newForDetached(ec, pc, JDOHelper.getObjectId(pc), null);
            pc.dnReplaceStateManager(op);
            op.retrieveDetachState(op);
            int position = op.getClassMetaData().getAbsolutePositionOfMember(memberName);
            boolean[] dirtyFieldNumbers = op.getDirtyFields();
            pc.dnReplaceStateManager(null);

            return dirtyFieldNumbers[position];
        }

        ExecutionContext ec = (ExecutionContext) pc.dnGetExecutionContext();
        ObjectProvider op = ec.findObjectProvider(pc);
        if (op == null)
        {
            return null;
        }
        int position = op.getClassMetaData().getAbsolutePositionOfMember(memberName);
        boolean[] dirtyFieldNumbers = op.getDirtyFields();
        return dirtyFieldNumbers[position];
    }

    // ------------------------------ Convenience --------------------------------

    /**
     * Convenience method to convert an exception into a JDO exception.
     * If the incoming exception has a "failed object" then create the new exception with
     * a failed object. Otherwise if the incoming exception has nested exceptions then
     * create this exception with those nested exceptions. Else create this exception with
     * the incoming exception as its nested exception.
     * @param ne NucleusException
     * @return The JDOException
     */
    public static JDOException getJDOExceptionForNucleusException(NucleusException ne)
    {
        // Specific exceptions first
        if (ne instanceof ClassNotPersistableException)
        {
            return new ClassNotPersistenceCapableException(ne.getMessage(), ne);
        }
        else if (ne instanceof NoPersistenceInformationException)
        {
            return new org.datanucleus.api.jdo.exceptions.NoPersistenceInformationException(ne.getMessage(), ne);
        }
        else if (ne instanceof TransactionNotReadableException)
        {
            return new org.datanucleus.api.jdo.exceptions.TransactionNotReadableException(ne.getMessage(), ne.getCause());
        }
        else if (ne instanceof TransactionNotWritableException)
        {
            return new org.datanucleus.api.jdo.exceptions.TransactionNotWritableException(ne.getMessage(), ne.getCause());
        }
        else if (ne instanceof TransactionNotActiveException)
        {
            return new org.datanucleus.api.jdo.exceptions.TransactionNotActiveException(ne.getMessage(), ne);
        }
        else if (ne instanceof QueryInterruptedException)
        {
            return new JDOQueryInterruptedException(ne.getMessage());
        }
        else if (ne instanceof QueryTimeoutException)
        {
            return new JDODataStoreException(ne.getMessage(), ne);
        }
        else if (ne instanceof NucleusUnsupportedOptionException)
        {
            return new JDOUnsupportedOptionException(ne.getMessage(), ne);
        }
        else if (ne instanceof DatastoreReadOnlyException)
        {
            ClassLoaderResolver clr = ((DatastoreReadOnlyException)ne).getClassLoaderResolver();
            try
            {
                Class cls = clr.classForName("javax.jdo.JDOReadOnlyException");
                throw (JDOUserException)ClassUtils.newInstance(cls, 
                    new Class[] {String.class}, new Object[] {ne.getMessage()});
            }
            catch (NucleusException ne2)
            {
                // No JDOReadOnlyException so JDO1.0-JDO2.1
                throw new JDOUserException(ne2.getMessage());
            }
        }
        else if (ne instanceof NucleusDataStoreException)
        {
            if (ne.isFatal())
            {
                //sadly JDOFatalDataStoreException dont allow nested exceptions and failed objects together
                if (ne.getFailedObject() != null)
                {
                    return new JDOFatalDataStoreException(ne.getMessage(), ne.getFailedObject());
                }
                else if (ne.getNestedExceptions() != null)
                {
                    return new JDOFatalDataStoreException(ne.getMessage(), ne.getNestedExceptions());
                }
                else
                {
                    return new JDOFatalDataStoreException(ne.getMessage(), ne);
                }
            }

            if (ne.getNestedExceptions() != null)
            {
                if (ne.getFailedObject() != null)
                {
                    return new JDODataStoreException(ne.getMessage(), ne.getNestedExceptions(), ne.getFailedObject());
                }
                return new JDODataStoreException(ne.getMessage(), ne.getNestedExceptions());
            }
            else if (ne.getFailedObject() != null)
            {
                JDOPersistenceManager.LOGGER.info("Exception thrown", ne);
                return new JDODataStoreException(ne.getMessage(), ne.getFailedObject());
            }
            else
            {
                JDOPersistenceManager.LOGGER.info("Exception thrown", ne);
                return new JDODataStoreException(ne.getMessage(), ne);
            }
        }
        else if (ne instanceof NucleusObjectNotFoundException)
        {
            if (ne.getFailedObject() != null)
            {
                if (ne.getNestedExceptions() != null)
                {
                    return new JDOObjectNotFoundException(ne.getMessage(), ne.getNestedExceptions(), ne.getFailedObject());
                }
                return new JDOObjectNotFoundException(ne.getMessage(), ne, ne.getFailedObject());
            }
            else if (ne.getNestedExceptions() != null)
            {
                return new JDOObjectNotFoundException(ne.getMessage(), ne.getNestedExceptions());
            }
            else
            {
                return new JDOObjectNotFoundException(ne.getMessage(), new Throwable[]{ne});
            }
        }
        else if (ne instanceof NucleusCanRetryException)
        {
            if (ne.getNestedExceptions() != null)
            {
                if (ne.getFailedObject() != null)
                {
                    return new JDOCanRetryException(ne.getMessage(), ne.getNestedExceptions(), ne.getFailedObject());
                }
                return new JDOCanRetryException(ne.getMessage(), ne.getNestedExceptions());
            }
            else if (ne.getFailedObject() != null)
            {
                JDOPersistenceManager.LOGGER.info("Exception thrown", ne);
                return new JDOCanRetryException(ne.getMessage(), ne.getFailedObject());
            }
            else
            {
                JDOPersistenceManager.LOGGER.info("Exception thrown", ne);
                return new JDOCanRetryException(ne.getMessage(), ne);
            }
        }
        else if (ne instanceof NucleusUserException)
        {
            if (ne.isFatal())
            {
                if (ne.getNestedExceptions() != null)
                {
                    if (ne.getFailedObject() != null)
                    {
                        return new JDOFatalUserException(ne.getMessage(), ne.getNestedExceptions(), ne.getFailedObject());
                    }
                    return new JDOFatalUserException(ne.getMessage(), ne.getNestedExceptions());
                }
                else if (ne.getFailedObject() != null)
                {
                    JDOPersistenceManager.LOGGER.info("Exception thrown", ne);
                    return new JDOFatalUserException(ne.getMessage(), ne.getFailedObject());
                }
                else
                {
                    JDOPersistenceManager.LOGGER.info("Exception thrown", ne);
                    return new JDOFatalUserException(ne.getMessage(), ne);
                }
            }

            if (ne.getNestedExceptions() != null)
            {
                if (ne.getFailedObject() != null)
                {
                    return new JDOUserException(ne.getMessage(), ne.getNestedExceptions(), ne.getFailedObject());
                }
                return new JDOUserException(ne.getMessage(), ne.getNestedExceptions());
            }
            else if (ne.getFailedObject() != null)
            {
                JDOPersistenceManager.LOGGER.info("Exception thrown", ne);
                return new JDOUserException(ne.getMessage(), ne.getFailedObject());
            }
            else
            {
                JDOPersistenceManager.LOGGER.info("Exception thrown", ne);
                return new JDOUserException(ne.getMessage(), ne);
            }
        }
        else if (ne instanceof NucleusOptimisticException)
        {
            //sadly JDOOptimisticVerificationException dont allow nested exceptions and failed objects together
            if (ne.getFailedObject() != null)
            {
                return new JDOOptimisticVerificationException(ne.getMessage(), ne.getFailedObject());
            }
            else if (ne.getNestedExceptions() != null)
            {
                return new JDOOptimisticVerificationException(ne.getMessage(), ne.getNestedExceptions());
            }
            else
            {
                return new JDOOptimisticVerificationException(ne.getMessage(), ne);
            }
        }
        else if (ne instanceof org.datanucleus.transaction.HeuristicRollbackException && ne.getNestedExceptions().length == 1 &&
                ne.getNestedExceptions()[0].getCause() instanceof SQLException) 
        {
            return new JDODataStoreException(ne.getMessage(), ne.getNestedExceptions()[0].getCause());
        }
        else if (ne instanceof org.datanucleus.transaction.HeuristicRollbackException && ne.getNestedExceptions().length == 1 &&
                ne.getNestedExceptions()[0] instanceof NucleusDataStoreException) 
        {
            return new JDODataStoreException(ne.getMessage(), ne.getNestedExceptions()[0].getCause());
        }
        else
        {
            if (ne.isFatal())
            {
                if (ne.getNestedExceptions() != null)
                {
                    return new JDOFatalInternalException(ne.getMessage(), ne.getNestedExceptions());
                }
                return new JDOFatalInternalException(ne.getMessage(), ne);
            }
            else if (ne.getNestedExceptions() != null)
            {
                return new JDOException(ne.getMessage(), ne.getNestedExceptions());
            }
            else
            {
                return new JDOException(ne.getMessage(), ne);
            }
        }
    }

    // ---------------------------------- Replication -------------------------------

    /**
     * Convenience method to replicate a group of objects from one datastore (managed by PMF1)
     * to a second datastore (managed by PMF2).
     * @param pmf1 PersistenceManagerFactory for the source of the objects
     * @param pmf2 PersistenceManagerFactory for the target of the objects
     * @param oids Identities of the objects to replicate
     */
    public static void replicate(PersistenceManagerFactory pmf1, PersistenceManagerFactory pmf2, Object... oids)
    {
        JDOReplicationManager replicator = new JDOReplicationManager(pmf1, pmf2);
        replicator.replicate(oids);
    }

    /**
     * Convenience method to replicate objects of particular types from one datastore (managed by PMF1)
     * to a second datastore (managed by PMF2).
     * @param pmf1 PersistenceManagerFactory for the source of the objects
     * @param pmf2 PersistenceManagerFactory for the target of the objects
     * @param types Types of objects to replicate
     */
    public static void replicate(PersistenceManagerFactory pmf1, PersistenceManagerFactory pmf2, Class... types)
    {
        JDOReplicationManager replicator = new JDOReplicationManager(pmf1, pmf2);
        replicator.replicate(types);
    }

    /**
     * Convenience method to replicate objects of particular types from one datastore (managed by PMF1)
     * to a second datastore (managed by PMF2).
     * @param pmf1 PersistenceManagerFactory for the source of the objects
     * @param pmf2 PersistenceManagerFactory for the target of the objects
     * @param classNames Names of classes to replicate
     */
    public static void replicate(PersistenceManagerFactory pmf1, PersistenceManagerFactory pmf2, String... classNames)
    {
        JDOReplicationManager replicator = new JDOReplicationManager(pmf1, pmf2);
        replicator.replicate(classNames);
    }
}
