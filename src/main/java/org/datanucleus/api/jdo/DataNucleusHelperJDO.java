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
import java.util.Collection;

import javax.jdo.JDOHelper;
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

import org.datanucleus.ClassNameConstants;
import org.datanucleus.ExecutionContext;
import org.datanucleus.FetchPlanForClass;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.identity.ByteId;
import org.datanucleus.identity.CharId;
import org.datanucleus.identity.IntId;
import org.datanucleus.identity.LongId;
import org.datanucleus.identity.ObjectId;
import org.datanucleus.identity.ShortId;
import org.datanucleus.identity.SingleFieldId;
import org.datanucleus.identity.StringId;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.ClassMetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.state.DNStateManager;
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
     * Convenience accessor for the fields of a specified class that are currently in the FetchPlan.
     * @param pm Persistence Manager
     * @param className Name of the class
     * @return The fields that are in the FetchPlan
     */
    public static int[] getFieldsInFetchPlanForClass(PersistenceManager pm, String className)
    {
        ExecutionContext ec = ((JDOPersistenceManager)pm).getExecutionContext();
        AbstractClassMetaData cmd = ec.getMetaDataManager().getMetaDataForClass(className, ec.getClassLoaderResolver());
        FetchPlanForClass fpClass = ((JDOFetchPlan)pm.getFetchPlan()).getInternalFetchPlan().getFetchPlanForClass(cmd);
        return fpClass.getMemberNumbers();
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
            DNStateManager sm = ec.getNucleusContext().getStateManagerFactory().newForDetached(ec, pc, JDOHelper.getObjectId(pc), null);
            pc.dnReplaceStateManager(sm);
            sm.retrieveDetachState(sm);
            String[] dirtyFieldNames = sm.getDirtyFieldNames();
            pc.dnReplaceStateManager(null);

            return dirtyFieldNames;
        }

        ExecutionContext ec = (ExecutionContext) pc.dnGetExecutionContext();
        DNStateManager sm = ec.findStateManager(pc);
        if (sm == null)
        {
            return null;
        }
        return sm.getDirtyFieldNames();
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
            DNStateManager sm = ec.getNucleusContext().getStateManagerFactory().newForDetached(ec, pc, JDOHelper.getObjectId(pc), null);
            pc.dnReplaceStateManager(sm);
            sm.retrieveDetachState(sm);
            String[] loadedFieldNames = sm.getLoadedFieldNames();
            pc.dnReplaceStateManager(null);

            return loadedFieldNames;
        }

        ExecutionContext ec = (ExecutionContext) pc.dnGetExecutionContext();
        DNStateManager sm = ec.findStateManager(pc);
        if (sm == null)
        {
            return null;
        }
        return sm.getLoadedFieldNames();
    }

    /**
     * Accessor for whether the specified member (field/property) of the passed persistable object is loaded.
     * @param obj The persistable object
     * @param memberName Name of the field/property
     * @param pm PersistenceManager (if the object is detached)
     * @return Whether the member is loaded
     */
    public static Boolean isFieldLoaded(Object obj, String memberName, PersistenceManager pm)
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
            DNStateManager sm = ec.getNucleusContext().getStateManagerFactory().newForDetached(ec, pc, JDOHelper.getObjectId(pc), null);
            pc.dnReplaceStateManager(sm);
            sm.retrieveDetachState(sm);
            int position = sm.getClassMetaData().getAbsolutePositionOfMember(memberName);
            boolean loaded = sm.isFieldLoaded(position);
            pc.dnReplaceStateManager(null);

            return loaded;
        }

        ExecutionContext ec = (ExecutionContext) pc.dnGetExecutionContext();
        DNStateManager sm = ec.findStateManager(pc);
        if (sm == null)
        {
            return null;
        }
        int position = sm.getClassMetaData().getAbsolutePositionOfMember(memberName);
        return sm.isFieldLoaded(position);
    }

    /**
     * Accessor for whether the specified member (field/property) of the passed persistable object is dirty.
     * @param obj The persistable object
     * @param memberName Name of the field/property
     * @param pm PersistenceManager (if the object is detached)
     * @return Whether the member is dirty
     */
    public static Boolean isFieldDirty(Object obj, String memberName, PersistenceManager pm)
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
            DNStateManager sm = ec.getNucleusContext().getStateManagerFactory().newForDetached(ec, pc, JDOHelper.getObjectId(pc), null);
            pc.dnReplaceStateManager(sm);
            sm.retrieveDetachState(sm);
            int position = sm.getClassMetaData().getAbsolutePositionOfMember(memberName);
            boolean[] dirtyFieldNumbers = sm.getDirtyFields();
            pc.dnReplaceStateManager(null);

            return dirtyFieldNumbers[position];
        }

        ExecutionContext ec = (ExecutionContext) pc.dnGetExecutionContext();
        DNStateManager sm = ec.findStateManager(pc);
        if (sm == null)
        {
            return null;
        }
        int position = sm.getClassMetaData().getAbsolutePositionOfMember(memberName);
        boolean[] dirtyFieldNumbers = sm.getDirtyFields();
        return dirtyFieldNumbers[position];
    }
}