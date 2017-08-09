/**********************************************************************
Copyright (c) 2006 Erik Bengtson and others. All rights reserved.
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
2008 Andy Jefferson - addition of persistence, identity methods
     ...
 **********************************************************************/
package org.datanucleus.api.jdo;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.ExecutionContext;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.ApiAdapter;
import org.datanucleus.api.jdo.state.LifeCycleStateFactory;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.IdentityType;
import org.datanucleus.metadata.InvalidPrimaryKeyException;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.util.ClassUtils;

/**
 * Adapter for the JDO API, to allow the DataNucleus core runtime to expose multiple APIs to clients.
 */
public class JDOAdapter implements ApiAdapter
{
    private static final long serialVersionUID = 4135712868062532386L;
    protected static final Set<String> defaultPersistentTypeNames = new HashSet<String>();

    static
    {
        defaultPersistentTypeNames.add(ClassNameConstants.BOOLEAN);
        defaultPersistentTypeNames.add(ClassNameConstants.BYTE);
        defaultPersistentTypeNames.add(ClassNameConstants.CHAR);
        defaultPersistentTypeNames.add(ClassNameConstants.DOUBLE);
        defaultPersistentTypeNames.add(ClassNameConstants.FLOAT);
        defaultPersistentTypeNames.add(ClassNameConstants.INT);
        defaultPersistentTypeNames.add(ClassNameConstants.LONG);
        defaultPersistentTypeNames.add(ClassNameConstants.SHORT);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_BOOLEAN);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_BYTE);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_CHARACTER);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_DOUBLE);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_FLOAT);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_INTEGER);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_LONG);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_SHORT);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_STRING);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_UTIL_DATE);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_SQL_DATE);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_SQL_TIME);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_SQL_TIMESTAMP);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_MATH_BIGDECIMAL);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_MATH_BIGINTEGER);
        defaultPersistentTypeNames.add(java.util.Locale.class.getName());
        defaultPersistentTypeNames.add(java.util.Currency.class.getName());
        defaultPersistentTypeNames.add(ClassNameConstants.BOOLEAN_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.BYTE_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.CHAR_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.DOUBLE_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.FLOAT_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.INT_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.LONG_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.SHORT_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_BOOLEAN_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_BYTE_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_CHARACTER_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_DOUBLE_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_FLOAT_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_INTEGER_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_LONG_ARRAY);
        defaultPersistentTypeNames.add(ClassNameConstants.JAVA_LANG_SHORT_ARRAY);
    }

    /**
     * Accessor for the name of the API.
     * @return Name of the API
     */
    public String getName()
    {
        return "JDO";
    }

    /* (non-Javadoc)
     * @see org.datanucleus.api.ApiAdapter#isMemberDefaultPersistent(java.lang.Class)
     */
    public boolean isMemberDefaultPersistent(Class type)
    {
        String typeName = type.getName();
        if (defaultPersistentTypeNames.contains(typeName))
        {
            return true;
        }
        else if (Enum.class.isAssignableFrom(type) || Serializable.class.isAssignableFrom(type))
        {
            return true;
        }
        else if (isPersistable(type))
        {
            return true;
        }
        return false;
    }

    // ------------------------------ Object Lifecycle --------------------------------

    /**
     * Method to return the ExecutionContext (if any) associated with the passed object.
     * Supports persistable objects, and PersistenceManager.
     * @param obj The object
     * @return The ExecutionContext
     */
    public ExecutionContext getExecutionContext(Object obj)
    {
        if (obj == null)
        {
            return null;
        }

        if (obj instanceof Persistable)
        {
            return (ExecutionContext) ((Persistable)obj).dnGetExecutionContext();
        }
        else if (obj instanceof JDOPersistenceManager)
        {
            return ((JDOPersistenceManager)obj).getExecutionContext();
        }

        return null;
    }

    /**
     * Returns the LifeCycleState for the state constant.
     * @param stateType the type as integer
     * @return the type as LifeCycleState object
     */
    public LifeCycleState getLifeCycleState(int stateType)
    {
        return LifeCycleStateFactory.getLifeCycleState(stateType);
    }

    /**
     * Accessor for the object state.
     * @param obj Object
     * @return The state ("persistent-clean", "detached-dirty" etc)
     */
    public String getObjectState(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        return JDOHelper.getObjectState(obj).toString();
    }

    // ------------------------------ Object Identity  --------------------------------

    /**
     * Utility to check if a primary-key class is valid.
     * Will throw a InvalidPrimaryKeyException if it is invalid, otherwise returning true.
     * @param pkClass The Primary Key class
     * @param cmd AbstractClassMetaData for the persistable class
     * @param clr the ClassLoaderResolver
     * @param noOfPkFields Number of primary key fields
     * @param mmgr MetaData manager
     * @return Whether it is valid
     */
    public boolean isValidPrimaryKeyClass(Class pkClass, AbstractClassMetaData cmd, ClassLoaderResolver clr, int noOfPkFields, MetaDataManager mmgr)
    {
        // When using inner class, must be static
        if (ClassUtils.isInnerClass(pkClass.getName()) && !Modifier.isStatic(pkClass.getModifiers()))
        {
            throw new InvalidPrimaryKeyException("019000", cmd.getFullClassName(), pkClass.getName());
        }

        // Must be public
        if (!Modifier.isPublic(pkClass.getModifiers()))
        {
            throw new InvalidPrimaryKeyException("019001", cmd.getFullClassName(), pkClass.getName());
        }

        // Must implement Serializable
        if (!Serializable.class.isAssignableFrom(pkClass))
        {
            throw new InvalidPrimaryKeyException("019002", cmd.getFullClassName(), pkClass.getName());
        }

        // a). SingleFieldIdentity class
        if (IdentityUtils.isSingleFieldIdentityClass(pkClass.getName()))
        {
            if (noOfPkFields != 1)
            {
                throw new InvalidPrimaryKeyException("019003", cmd.getFullClassName());
            }
        }
        // b). Users own primary key class
        else
        {
            // Must have public default constructor
            try
            {
                Constructor constructor = pkClass.getConstructor(new Class[0]);
                if (constructor == null ||
                    !Modifier.isPublic(constructor.getModifiers()))
                {
                    throw new InvalidPrimaryKeyException("019004", cmd.getFullClassName(), pkClass.getName());
                }
            }
            catch (NoSuchMethodException ex)
            {
                throw new InvalidPrimaryKeyException("019004", cmd.getFullClassName(), pkClass.getName());
            }

            // Must have public String arg constructor
            try
            {
                Constructor constructor = pkClass.getConstructor(new Class[] {String.class});
                if (constructor == null ||
                    !Modifier.isPublic(constructor.getModifiers()))
                {
                    throw new InvalidPrimaryKeyException("019005", cmd.getFullClassName(), pkClass.getName());
                }
            }
            catch (NoSuchMethodException nsme)
            {
            }

            // Must override toString() method
            try
            {
                java.lang.reflect.Method method=pkClass.getMethod("toString",new Class[0]);
                if (method == null ||
                    !Modifier.isPublic(method.getModifiers()) ||
                    method.getDeclaringClass().equals(Object.class))
                {
                    throw new InvalidPrimaryKeyException("019006", cmd.getFullClassName(), pkClass.getName());
                }
            }
            catch (NoSuchMethodException nsme)
            {
            }

            // Must override hashCode() method
            try
            {
                java.lang.reflect.Method method=pkClass.getMethod("hashCode",new Class[0]);
                if (method == null || method.getDeclaringClass().equals(Object.class))
                {
                    throw new InvalidPrimaryKeyException("019007", cmd.getFullClassName(), pkClass.getName());
                }
            }
            catch (NoSuchMethodException nsme)
            {
            }

            // Must override equals(Object) method
            try
            {
                java.lang.reflect.Method method=pkClass.getMethod("equals",new Class[] {Object.class});
                if (method == null || method.getDeclaringClass().equals(Object.class))
                {
                    throw new InvalidPrimaryKeyException("019008", cmd.getFullClassName(), pkClass.getName());
                }
            }
            catch (NoSuchMethodException nsme)
            {
            }

            // Check the field types of the objectid-class
            int noPkFields = processPrimaryKeyClass(pkClass, cmd, clr, mmgr);
            for (Class<?> supercls : ClassUtils.getSuperclasses(pkClass))
            {
                noPkFields += processPrimaryKeyClass(supercls, cmd, clr, mmgr);
            }

            // No of Primary Key fields and no of fields in
            // objectid-class must concur
            if (noOfPkFields != noPkFields &&
                cmd.getIdentityType() == IdentityType.APPLICATION)
            {
                throw new InvalidPrimaryKeyException("019015", cmd.getFullClassName(), pkClass.getName(), "" + noOfPkFields, "" + noPkFields);
            }
        }

        return true;
    }

    /**
     * Convenience method to process a PK class and return the number of valid fields found.
     * Throws InvalidPrimaryKeyException if a field is invalid
     * @param pkClass The PK class.
     * @param cmd MetaData for the class that this is the PK for
     * @param mmgr MetaData manager
     * @return The number of PK fields
     */
    private int processPrimaryKeyClass(Class pkClass, AbstractClassMetaData cmd, ClassLoaderResolver clr, MetaDataManager mmgr)
    {
        int noOfPkFields = 0;

        Field[] fieldsInPkClass = pkClass.getDeclaredFields();
        for (int i=0;i<fieldsInPkClass.length;i++)
        {
            if (!Modifier.isStatic(fieldsInPkClass[i].getModifiers()))
            {
                // All non-static fields must be serializable
                if (!fieldsInPkClass[i].getType().isPrimitive() && !(Serializable.class).isAssignableFrom(fieldsInPkClass[i].getType()))
                {
                    throw new InvalidPrimaryKeyException("019009", cmd.getFullClassName(), pkClass.getName(), fieldsInPkClass[i].getName());
                }

                // All non-static fields must be public
                if (!Modifier.isPublic(fieldsInPkClass[i].getModifiers()))
                {
                    throw new InvalidPrimaryKeyException("019010", cmd.getFullClassName(), pkClass.getName(), fieldsInPkClass[i].getName());
                }

                if (fieldsInPkClass[i].getName().equals("targetClassName"))
                {
                    // Ignore any field named "targetClassName"
                }
                else
                {
                    // Check if a non-static field of objectid-class is present in the Persistable class
                    AbstractMemberMetaData fieldInPcClass = cmd.getMetaDataForMember(fieldsInPkClass[i].getName());
                    boolean foundField = false;
                    if (fieldInPcClass == null)
                    {
                        throw new InvalidPrimaryKeyException("019011", cmd.getFullClassName(), pkClass.getName(), fieldsInPkClass[i].getName());
                    }

                    // check if the field in objectid-class has the same type as the type declared in the persistable class
                    if (fieldInPcClass.getTypeName().equals(fieldsInPkClass[i].getType().getName()))
                    {
                        foundField = true;
                    }

                    // Check for primary key field that is PC (Compound Identity - aka Identifying Relations)
                    if (!foundField)
                    {
                        String fieldTypePkClass = fieldsInPkClass[i].getType().getName();
                        AbstractClassMetaData refCmd = mmgr.getMetaDataForClassInternal(fieldInPcClass.getType(), clr);
                        if (refCmd == null)
                        {
                            throw new InvalidPrimaryKeyException("019012", cmd.getFullClassName(), pkClass.getName(),
                                fieldsInPkClass[i].getName(), fieldInPcClass.getType().getName());
                        }
                        if (refCmd.getObjectidClass() == null && IdentityUtils.isSingleFieldIdentityClass(fieldTypePkClass))
                        {
                            // Single Field Identity
                            throw new InvalidPrimaryKeyException("019014", cmd.getFullClassName(), pkClass.getName(),
                                fieldsInPkClass[i].getName(), fieldTypePkClass, refCmd.getFullClassName());
                        }
                        if (!fieldTypePkClass.equals(refCmd.getObjectidClass()))
                        {
                            throw new InvalidPrimaryKeyException("019013", cmd.getFullClassName(), pkClass.getName(),
                                fieldsInPkClass[i].getName(), fieldTypePkClass, refCmd.getObjectidClass());
                        }
                        foundField=true;
                    }
                    if (!foundField)
                    {
                        throw new InvalidPrimaryKeyException("019012", cmd.getFullClassName(), pkClass.getName(),
                            fieldsInPkClass[i].getName(), fieldInPcClass.getType().getName());
                    }

                    noOfPkFields++;
                }
            }
        }

        return noOfPkFields;
    }

    // ------------------------------ Persistence --------------------------------

    /**
     * Whether the API allows (re-)persistence of a deleted object.
     * @return Whether you can call persist on a deleted object
     */
    public boolean allowPersistOfDeletedObject()
    {
        // JDO doesnt allow re-persist of a deleted object
        return false;
    }

    /**
     * Whether the API allows deletion of a non-persistent object.
     * @return Whether you can call delete on an object not yet persisted
     */
    public boolean allowDeleteOfNonPersistentObject()
    {
        // JDO requires an exception throwing on attempts to delete transient objects
        return false;
    }

    /**
     * Whether the API allows reading a field of a deleted object.
     * @return Whether you can read after deleting
     */
    public boolean allowReadFieldOfDeletedObject()
    {
        return false;
    }

    /**
     * Whether the API requires clearing of the fields of an object when it is deleted.
     * @return Whether to clear loaded fields at delete
     */
    public boolean clearLoadedFlagsOnDeleteObject()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.api.ApiAdapter#getDefaultCascadePersistorField()
     */
    @Override
    public boolean getDefaultCascadePersistForField()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.api.ApiAdapter#getDefaultCascadeUpdateForField()
     */
    @Override
    public boolean getDefaultCascadeUpdateForField()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.api.ApiAdapter#getDefaultCascadeDeleteForField()
     */
    @Override
    public boolean getDefaultCascadeDeleteForField()
    {
        // JDO defaults to not deleting by reachability (unless using delete dependent)
        return false;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.api.ApiAdapter#getDefaultCascadeDetachForField()
     */
    @Override
    public boolean getDefaultCascadeDetachForField()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.api.ApiAdapter#getDefaultCascadeRefreshForField()
     */
    @Override
    public boolean getDefaultCascadeRefreshForField()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.api.ApiAdapter#getDefaultDFGForPersistableField()
     */
    public boolean getDefaultDFGForPersistableField()
    {
        // 1-1/N-1 default to being LAZY loaded
        return false;
    }

    /**
     * Method to return the default factory properties for this API.
     * @return The default props
     */
    public Map getDefaultFactoryProperties()
    {
        Map props = new HashMap();
        props.put(PropertyNames.PROPERTY_DETACH_ALL_ON_COMMIT, "false");
        props.put(PropertyNames.PROPERTY_COPY_ON_ATTACH, "true");
        props.put(PropertyNames.PROPERTY_IDENTIFIER_FACTORY, "datanucleus2"); // DN identifier naming
        props.put(PropertyNames.PROPERTY_PERSISTENCE_BY_REACHABILITY_AT_COMMIT, "true");
        props.put(PropertyNames.PROPERTY_QUERY_SQL_ALLOWALL, "false"); // JDO SQL has to start SELECT
        props.put(PropertyNames.PROPERTY_VALIDATION_MODE, "none"); // Default to no validation unless enabled
        props.put(PropertyNames.PROPERTY_EXECUTION_CONTEXT_CLOSE_ACTIVE_TX_ACTION, "rollback"); // JDO 3.2 changes to use this
        return props;
    }

    /**
     * Convenience method to return a user exception appropriate for this API.
     * @param msg The message
     * @param e The cause
     * @return The JDO exception
     */
    public RuntimeException getUserExceptionForException(String msg, Exception e)
    {
        return new JDOUserException(msg, e);
    }

    /**
     * Convenience method to return a datastore exception appropriate for this API.
     * @param msg The message
     * @param e Any root cause exception
     * @return The exception
     */
    public RuntimeException getDataStoreExceptionForException(String msg, Exception e)
    {
        return new JDODataStoreException(msg, e);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.api.ApiAdapter#getApiExceptionForNucleusException(org.datanucleus.exceptions.NucleusException)
     */
    public RuntimeException getApiExceptionForNucleusException(NucleusException ne)
    {
        return NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
    }
}