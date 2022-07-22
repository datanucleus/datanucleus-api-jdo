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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.ExecutionContext;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.ApiAdapter;
import org.datanucleus.api.jdo.exceptions.ClassNotPersistenceCapableException;
import org.datanucleus.api.jdo.metadata.JDOXmlMetaDataHelper;
import org.datanucleus.api.jdo.state.LifeCycleStateFactory;
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
import org.datanucleus.identity.IdentityManager;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.IdentityType;
import org.datanucleus.metadata.InvalidPrimaryKeyException;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.store.query.QueryInterruptedException;
import org.datanucleus.store.query.QueryTimeoutException;
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

    @Override
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

    @Override
    public String getXMLMetaDataForClass(AbstractClassMetaData cmd, String prefix, String indent)
    {
        return new JDOXmlMetaDataHelper().getXMLForMetaData(cmd, prefix, indent);
    }

    @Override
    public String getDefaultMappingFileLocation() 
    {
        return null;
    }

    // ------------------------------ Object Lifecycle --------------------------------

    @Override
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

    @Override
    public LifeCycleState getLifeCycleState(int stateType)
    {
        return LifeCycleStateFactory.getLifeCycleState(stateType);
    }

    @Override
    public String getObjectState(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        return JDOHelper.getObjectState(obj).toString();
    }

    // ------------------------------ Object Identity  --------------------------------

    @Override
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
                Constructor<?> constructor = pkClass.getConstructor(new Class[0]);
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
                Constructor<?> constructor = pkClass.getConstructor(new Class[] {String.class});
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
                java.lang.reflect.Method method=pkClass.getMethod("toString", new Class[0]);
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
                java.lang.reflect.Method method=pkClass.getMethod("hashCode", new Class[0]);
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
                java.lang.reflect.Method method=pkClass.getMethod("equals", new Class[] {Object.class});
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

                if (fieldsInPkClass[i].getName().equals(IdentityManager.IDENTITY_CLASS_TARGET_CLASS_NAME_FIELD))
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

    @Override
    public boolean allowPersistOfDeletedObject()
    {
        // JDO doesnt allow re-persist of a deleted object
        return false;
    }

    @Override
    public boolean allowDeleteOfNonPersistentObject()
    {
        // JDO requires an exception throwing on attempts to delete transient objects
        return false;
    }

    @Override
    public boolean allowReadFieldOfDeletedObject()
    {
        return false;
    }

    @Override
    public boolean clearLoadedFlagsOnDeleteObject()
    {
        return true;
    }

    @Override
    public boolean getDefaultCascadePersistForField()
    {
        return true;
    }

    @Override
    public boolean getDefaultCascadeAttachForField()
    {
        return true;
    }

    @Override
    public boolean getDefaultCascadeDeleteForField()
    {
        // JDO defaults to not deleting by reachability (unless using delete dependent)
        return false;
    }

    @Override
    public boolean getDefaultCascadeDetachForField()
    {
        return true;
    }

    @Override
    public boolean getDefaultCascadeRefreshForField()
    {
        return false;
    }

    @Override
    public boolean getDefaultDFGForPersistableField()
    {
        // 1-1/N-1 default to being LAZY loaded
        return false;
    }

    @Override
    public Map<String, Object> getDefaultFactoryProperties()
    {
        Map<String, Object> props = new HashMap<>();
        props.put(PropertyNames.PROPERTY_DETACH_ALL_ON_COMMIT, "false");
        props.put(PropertyNames.PROPERTY_COPY_ON_ATTACH, "true");
        props.put(PropertyNames.PROPERTY_IDENTIFIER_FACTORY, "datanucleus2"); // DN identifier naming
        props.put(PropertyNames.PROPERTY_PERSISTENCE_BY_REACHABILITY_AT_COMMIT, "true");
        props.put(PropertyNames.PROPERTY_QUERY_SQL_ALLOWALL, "false"); // JDO SQL has to start SELECT
        props.put(PropertyNames.PROPERTY_VALIDATION_MODE, "none"); // Default to no validation unless enabled
        props.put(PropertyNames.PROPERTY_EXECUTION_CONTEXT_CLOSE_ACTIVE_TX_ACTION, "rollback"); // JDO 3.2 changes to use this
        return props;
    }

    @Override
    public boolean getDefaultPersistentPropertyWhenNotSpecified()
    {
        return false;
    }

    @Override
    public RuntimeException getUserExceptionForException(String msg, Exception e)
    {
        return new JDOUserException(msg, e);
    }

    @Override
    public RuntimeException getDataStoreExceptionForException(String msg, Exception e)
    {
        return new JDODataStoreException(msg, e);
    }

    @Override
    public RuntimeException getApiExceptionForNucleusException(NucleusException ne)
    {
        return JDOAdapter.getJDOExceptionForNucleusException(ne);
    }

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
                throw (JDOUserException)ClassUtils.newInstance(cls, new Class[] {String.class}, new Object[] {ne.getMessage()});
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
}