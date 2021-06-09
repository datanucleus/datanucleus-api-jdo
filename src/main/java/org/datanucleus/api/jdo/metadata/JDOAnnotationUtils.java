/**********************************************************************
Copyright (c) 2006 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.api.jdo.metadata;

import java.lang.reflect.Method;
import java.util.Map;

import javax.jdo.annotations.Cacheable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Columns;
import javax.jdo.annotations.Convert;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Extensions;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.FetchPlan;
import javax.jdo.annotations.FetchPlans;
import javax.jdo.annotations.ForeignKey;
import javax.jdo.annotations.ForeignKeyAction;
import javax.jdo.annotations.ForeignKeys;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Indices;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Joins;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceAware;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Sequence;
import javax.jdo.annotations.SequenceStrategy;
import javax.jdo.annotations.Serialized;
import javax.jdo.annotations.Transactional;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;
import javax.jdo.annotations.Value;
import javax.jdo.annotations.Version;

import org.datanucleus.api.jdo.JDOQuery;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.DiscriminatorStrategy;
import org.datanucleus.metadata.FieldPersistenceModifier;
import org.datanucleus.metadata.ForeignKeyMetaData;
import org.datanucleus.metadata.ValueGenerationStrategy;
import org.datanucleus.metadata.IdentityType;
import org.datanucleus.metadata.IndexMetaData;
import org.datanucleus.metadata.InheritanceStrategy;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.metadata.QueryLanguage;
import org.datanucleus.metadata.UniqueMetaData;
import org.datanucleus.metadata.VersionStrategy;
import org.datanucleus.util.StringUtils;

/**
 * Series of utility methods for converting between annotations and metadata.
 */
public class JDOAnnotationUtils
{
    public static final String PERSISTENCE_CAPABLE = PersistenceCapable.class.getName();

    public static final String PERSISTENCE_AWARE = PersistenceAware.class.getName();

    public static final String EMBEDDED_ONLY = EmbeddedOnly.class.getName();

    public static final String VERSION = Version.class.getName();

    public static final String DATASTORE_IDENTITY = DatastoreIdentity.class.getName();

    public static final String PRIMARY_KEY = PrimaryKey.class.getName();

    public static final String JOINS = Joins.class.getName();

    public static final String JOIN = Join.class.getName();

    public static final String INHERITANCE = Inheritance.class.getName();

    public static final String DISCRIMINATOR = Discriminator.class.getName();

    public static final String QUERIES = Queries.class.getName();

    public static final String QUERY = Query.class.getName();

    public static final String FETCHPLAN = FetchPlan.class.getName();

    public static final String FETCHPLANS = FetchPlans.class.getName();

    public static final String FETCHGROUPS = FetchGroups.class.getName();

    public static final String FETCHGROUP = FetchGroup.class.getName();

    public static final String SEQUENCE = Sequence.class.getName();

    public static final String INDICES = Indices.class.getName();

    public static final String INDEX = Index.class.getName();

    public static final String UNIQUES = Uniques.class.getName();

    public static final String UNIQUE = Unique.class.getName();

    public static final String FOREIGNKEYS = ForeignKeys.class.getName();

    public static final String FOREIGNKEY = ForeignKey.class.getName();

    public static final String COLUMNS = Columns.class.getName();

    public static final String COLUMN = Column.class.getName();

    public static final String EXTENSIONS = Extensions.class.getName();

    public static final String EXTENSION = Extension.class.getName();

    public static final String PERSISTENT = Persistent.class.getName();

    public static final String TRANSACTIONAL = Transactional.class.getName();

    public static final String NOTPERSISTENT = NotPersistent.class.getName();

    public static final String SERIALIZED = Serialized.class.getName();

    public static final String ELEMENT = Element.class.getName();

    public static final String KEY = Key.class.getName();

    public static final String VALUE = Value.class.getName();

    public static final String ORDER = Order.class.getName();

    public static final String EMBEDDED = Embedded.class.getName();

    public static final String CACHEABLE = Cacheable.class.getName();

    public static final String CONVERT = Convert.class.getName();

    public static final String CONVERTS = "javax.jdo.annotations.Converts"; // TODO Use annotation direct if ever part of JDO 3.2

    /**
     * Convenience accessor for the query language to a valid internal value.
     * @param value The query language name
     * @return The internal name
     */
    public static String getQueryLanguageName(String value)
    {
        if (value == null)
        {
            // Fallback to JDOQL
            return QueryLanguage.JDOQL.toString();
        }
        else if (value.equalsIgnoreCase(JDOQuery.JDOQL_QUERY_LANGUAGE))
        {
            // Allow shortcut to "JDOQL"
            return QueryLanguage.JDOQL.toString();
        }
        else if (value.equalsIgnoreCase(JDOQuery.SQL_QUERY_LANGUAGE))
        {
            // Allow shortcut to "SQL"
            return QueryLanguage.SQL.toString();
        }
        else if (value.equalsIgnoreCase(JDOQuery.JPQL_QUERY_LANGUAGE))
        {
            // Allow shortcut to "JPQL"
            return QueryLanguage.JPQL.toString();
        }
        return value;
    }

    /**
     * Convenience accessor for the string name of a null value action.
     * @param value The annotation null value
     * @return The name
     */
    public static String getNullValueString(NullValue value)
    {
        if (value == NullValue.DEFAULT)
        {
            return org.datanucleus.metadata.NullValue.DEFAULT.toString();
        }
        else if (value == NullValue.EXCEPTION)
        {
            return org.datanucleus.metadata.NullValue.EXCEPTION.toString();
        }
        else if (value == NullValue.NONE)
        {
            return org.datanucleus.metadata.NullValue.NONE.toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Convenience accessor for the string name of a FK action.
     * @param action The annotation action
     * @return The name
     */
    public static String getForeignKeyActionString(ForeignKeyAction action)
    {
        if (action == ForeignKeyAction.CASCADE)
        {
            return ForeignKeyAction.CASCADE.toString();
        }
        else if (action == ForeignKeyAction.DEFAULT)
        {
            return ForeignKeyAction.DEFAULT.toString();
        }
        else if (action == ForeignKeyAction.NONE)
        {
            return ForeignKeyAction.NONE.toString();
        }
        else if (action == ForeignKeyAction.NULL)
        {
            return ForeignKeyAction.NULL.toString();
        }
        else if (action == ForeignKeyAction.RESTRICT)
        {
            return ForeignKeyAction.RESTRICT.toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Convenience accessor for the persistence-modifier on a field.
     * @param modifier The annotation modifier
     * @return The modifier
     */
    public static FieldPersistenceModifier getFieldPersistenceModifier(PersistenceModifier modifier)
    {
        if (modifier == PersistenceModifier.PERSISTENT)
        {
            return org.datanucleus.metadata.FieldPersistenceModifier.PERSISTENT;
        }
        else if (modifier == PersistenceModifier.TRANSACTIONAL)
        {
            return org.datanucleus.metadata.FieldPersistenceModifier.TRANSACTIONAL;
        }
        else if (modifier == PersistenceModifier.NONE)
        {
            return org.datanucleus.metadata.FieldPersistenceModifier.NONE;
        }
        else
        {
            return null;
        }
    }

    /**
     * Convenience accessor for the string name of the identity type.
     * @param idType The id type
     * @return The name
     */
    public static String getIdentityTypeString(javax.jdo.annotations.IdentityType idType)
    {
        if (idType == javax.jdo.annotations.IdentityType.APPLICATION)
        {
            return IdentityType.APPLICATION.toString();
        }
        else if (idType == javax.jdo.annotations.IdentityType.DATASTORE)
        {
            return IdentityType.DATASTORE.toString();
        }
        else if (idType == javax.jdo.annotations.IdentityType.NONDURABLE)
        {
            return IdentityType.NONDURABLE.toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Convenience accessor for the string name of a datastore sequence strategy.
     * @param strategy The annotation strategy
     * @return The name
     */
    public static String getSequenceStrategyString(SequenceStrategy strategy)
    {
        if (strategy == SequenceStrategy.NONTRANSACTIONAL)
        {
            return org.datanucleus.metadata.SequenceStrategy.NONTRANSACTIONAL.toString();
        }
        else if (strategy == SequenceStrategy.CONTIGUOUS)
        {
            return org.datanucleus.metadata.SequenceStrategy.CONTIGUOUS.toString();
        }
        else if (strategy == SequenceStrategy.NONCONTIGUOUS)
        {
            return org.datanucleus.metadata.SequenceStrategy.NONCONTIGUOUS.toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Convenience accessor for the string name of a id generator strategy (from JDO annotations).
     * @param strategy The id generation strategy
     * @return The name
     */
    public static String getValueGenerationStrategyString(IdGeneratorStrategy strategy)
    {
        if (strategy == IdGeneratorStrategy.NATIVE)
        {
            return ValueGenerationStrategy.NATIVE.toString();
        }
        else if (strategy == IdGeneratorStrategy.IDENTITY)
        {
            return ValueGenerationStrategy.IDENTITY.toString();
        }
        else if (strategy == IdGeneratorStrategy.SEQUENCE)
        {
            return ValueGenerationStrategy.SEQUENCE.toString();
        }
        else if (strategy == IdGeneratorStrategy.UUIDSTRING)
        {
            return ValueGenerationStrategy.UUIDSTRING.toString();
        }
        else if (strategy == IdGeneratorStrategy.UUIDHEX)
        {
            return ValueGenerationStrategy.UUIDHEX.toString();
        }
        else if (strategy == IdGeneratorStrategy.INCREMENT)
        {
            return ValueGenerationStrategy.INCREMENT.toString();
        }
        // TODO Allow for value generator extensions
        else
        {
            return null;
        }
    }

    /**
     * Convenience accessor for the string name of a version strategy.
     * @param strategy The version strategy
     * @return The name
     */
    public static String getVersionStrategyString(javax.jdo.annotations.VersionStrategy strategy)
    {
        if (strategy == javax.jdo.annotations.VersionStrategy.NONE)
        {
            return VersionStrategy.NONE.toString();
        }
        else if (strategy == javax.jdo.annotations.VersionStrategy.DATE_TIME)
        {
            return VersionStrategy.DATE_TIME.toString();
        }
        else if (strategy == javax.jdo.annotations.VersionStrategy.VERSION_NUMBER)
        {
            return VersionStrategy.VERSION_NUMBER.toString();
        }
        else if (strategy == javax.jdo.annotations.VersionStrategy.STATE_IMAGE)
        {
            return VersionStrategy.STATE_IMAGE.toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Convenience accessor for the string name of an inheritance strategy.
     * @param strategy The inheritance strategy
     * @return The name
     */
    public static String getInheritanceStrategyString(javax.jdo.annotations.InheritanceStrategy strategy)
    {
        if (strategy == javax.jdo.annotations.InheritanceStrategy.NEW_TABLE)
        {
            return InheritanceStrategy.NEW_TABLE.toString();
        }
        else if (strategy == javax.jdo.annotations.InheritanceStrategy.SUBCLASS_TABLE)
        {
            return InheritanceStrategy.SUBCLASS_TABLE.toString();
        }
        else if (strategy == javax.jdo.annotations.InheritanceStrategy.SUPERCLASS_TABLE)
        {
            return InheritanceStrategy.SUPERCLASS_TABLE.toString();
        }
        else
        {
            // COMPLETE_TABLE only present in JDO3.1
            try
            {
                if (strategy == javax.jdo.annotations.InheritanceStrategy.COMPLETE_TABLE)
                {
                    return InheritanceStrategy.COMPLETE_TABLE.toString();
                }
            }
            catch (Exception e)
            {
                // Not present in this jar
            }
            catch (Error e)
            {
                // Not present in this jar
            }

            return null;
        }
    }

    /**
     * Convenience accessor for the string name of a discriminator strategy.
     * @param strategy The discriminator strategy
     * @return The name
     */
    public static String getDiscriminatorStrategyString(javax.jdo.annotations.DiscriminatorStrategy strategy)
    {
        if (strategy == javax.jdo.annotations.DiscriminatorStrategy.NONE)
        {
            return DiscriminatorStrategy.NONE.toString();
        }
        else if (strategy == javax.jdo.annotations.DiscriminatorStrategy.VALUE_MAP)
        {
            return DiscriminatorStrategy.VALUE_MAP.toString();
        }
        else if (strategy == javax.jdo.annotations.DiscriminatorStrategy.CLASS_NAME)
        {
            return DiscriminatorStrategy.CLASS_NAME.toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Convenience method to get the column metadata for annotation values of a @Column.
     * @param annotationValues The values for the annotation
     * @return The MetaData for the column
     */
    public static ColumnMetaData getColumnMetaDataForAnnotations(Map<String, Object> annotationValues)
    {
        ColumnMetaData colmd = new ColumnMetaData();
        colmd.setName((String)annotationValues.get("name"));
        colmd.setTarget((String)annotationValues.get("target"));
        colmd.setTargetMember((String)annotationValues.get("targetField"));
        colmd.setJdbcType((String)annotationValues.get("jdbcType"));
        colmd.setSqlType((String)annotationValues.get("sqlType"));
        colmd.setLength((Integer)annotationValues.get("length"));
        colmd.setScale((Integer)annotationValues.get("scale"));
        colmd.setAllowsNull((String)annotationValues.get("allowsNull"));
        colmd.setDefaultValue((String)annotationValues.get("defaultValue"));
        colmd.setInsertValue((String)annotationValues.get("insertValue"));
        if (annotationValues.containsKey("position"))
        {
            colmd.setPosition((Integer)annotationValues.get("position"));
        }
        addExtensionsToMetaData(colmd, (Extension[])annotationValues.get("extensions"));
        return colmd;
    }

    /**
     * Convenience method to get the column metadata for a Column annotation.
     * @param col The Column annotation
     * @return The MetaData for the column
     */
    public static ColumnMetaData getColumnMetaDataForColumnAnnotation(Column col)
    {
        String length = null;
        String scale = null;
        if (col.length() > 0)
        {
            length = "" + col.length();
        }
        if (col.scale() >= 0)
        {
            scale = "" + col.scale();
        }

        ColumnMetaData colmd = new ColumnMetaData();
        colmd.setName(col.name());
        colmd.setTarget(col.target());
        colmd.setTargetMember(col.targetMember());
        colmd.setJdbcType(col.jdbcType());
        colmd.setSqlType(col.sqlType());
        colmd.setLength(length);
        colmd.setScale(scale);
        colmd.setAllowsNull(col.allowsNull());
        colmd.setDefaultValue(col.defaultValue());
        colmd.setInsertValue(col.insertValue());

        // "position" only present in JDO3.1+
        try
        {
            Method posMethod = col.getClass().getDeclaredMethod("position", (Class)null);
            Integer posValue = (Integer)posMethod.invoke(col, (Object[])null);
            colmd.setPosition(posValue);
        }
        catch (Exception e)
        {
            // Not present in this jdo.jar
        }

        addExtensionsToMetaData(colmd, col.extensions());
        return colmd;
    }

    /**
     * Convenience method to create an IndexMetaData from the annotations data.
     * @param name Name of the constraint
     * @param table Name of the table (optional)
     * @param unique Whether the constraint is unique
     * @param fields Fields to apply the constraint across (optional)
     * @param columns Columns to apply the constraint across (optional)
     * @return The IndexMetaData
     */
    public static IndexMetaData getIndexMetaData(String name, String table, String unique, String[] fields, Column[] columns)
    {
        IndexMetaData idxmd = new IndexMetaData();
        idxmd.setName(name);
        idxmd.setTable(table);
        if (!StringUtils.isWhitespace(unique))
        {
            idxmd.setUnique(Boolean.valueOf(unique));
        }
        if (fields != null && fields.length > 0)
        {
            for (int j=0;j<fields.length;j++)
            {
                idxmd.addMember(fields[j]);
            }
        }
        if (idxmd.getNumberOfMembers() == 0 && columns != null && columns.length > 0)
        {
            for (int j=0;j<columns.length;j++)
            {
                idxmd.addColumn(columns[j].name());
            }
        }

        return idxmd;
    }

    /**
     * Convenience method to create a UniqueMetaData from the annotations data.
     * @param name Name of the constraint
     * @param table Name of the table (optional)
     * @param deferred Whether the constraint is deferred
     * @param fields Fields to apply the constraint across (optional)
     * @param columns Columns to apply the constraint across (optional)
     * @return The UniqueMetaData
     */
    public static UniqueMetaData getUniqueMetaData(String name, String table, String deferred, String[] fields, 
            Column[] columns)
    {
        UniqueMetaData unimd = new UniqueMetaData();
        unimd.setName(name);
        unimd.setTable(table);

        if (!StringUtils.isWhitespace(deferred))
        {
            unimd.setDeferred(Boolean.valueOf(deferred));
        }
        if (fields != null && fields.length > 0)
        {
            for (int j=0;j<fields.length;j++)
            {
                unimd.addMember(fields[j]);
            }
        }
        if (unimd.getNumberOfMembers() == 0 && columns != null && columns.length > 0)
        {
            for (int j=0;j<columns.length;j++)
            {
                unimd.addColumn(columns[j].name());
            }
        }

        return unimd;
    }

    /**
     * Convenience method to create a ForeignKeyMetaData from the annotations data.
     * @param name Name of the constraint
     * @param table Name of the table (optional)
     * @param unique Whether the constraint is unique
     * @param deferred Whether the constraint is deferred
     * @param deleteAction Any delete action
     * @param updateAction Any update action
     * @param fields Fields to apply the constraint across (optional)
     * @param columns Columns to apply the constraint across (optional)
     * @return The ForeignKeyMetaData
     */
    public static ForeignKeyMetaData getFKMetaData(String name, String table, String unique, String deferred, 
            String deleteAction, String updateAction, String[] fields, Column[] columns)
    {
        ForeignKeyMetaData fkmd = new ForeignKeyMetaData();
        fkmd.setName(name);
        fkmd.setTable(table);
        fkmd.setUnique(unique);
        fkmd.setDeferred(deferred);
        fkmd.setDeleteAction(org.datanucleus.metadata.ForeignKeyAction.getForeignKeyAction(deleteAction));
        fkmd.setUpdateAction(org.datanucleus.metadata.ForeignKeyAction.getForeignKeyAction(updateAction));
        if (fields != null && fields.length > 0)
        {
            for (int j=0;j<fields.length;j++)
            {
                fkmd.addMember(fields[j]);
            }
        }
        if (fkmd.getNumberOfMembers() == 0 && columns != null && columns.length > 0)
        {
            for (int j=0;j<columns.length;j++)
            {
                ColumnMetaData colmd = JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(columns[j]);
                fkmd.addColumn(colmd);
            }
        }

        return fkmd;
    }

    /**
     * Convenience method to add extensions to a metadata element.
     * @param metadata The metadata element
     * @param extensions The extension annotations
     */
    public static void addExtensionsToMetaData(MetaData metadata, Extension[] extensions)
    {
        if (extensions == null || extensions.length == 0)
        {
            return;
        }

        for (int i=0;i<extensions.length;i++)
        {
            if (extensions[i].vendorName() != null && extensions[i].vendorName().equalsIgnoreCase(MetaData.VENDOR_NAME))
            {
                metadata.addExtension(extensions[i].key(), extensions[i].value());
            }
        }
    }
}