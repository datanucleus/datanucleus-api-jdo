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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
//import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.AttributeConverter;
import javax.jdo.AttributeConverter.UseDefault;
import javax.jdo.annotations.Column;
//import javax.jdo.annotations.Convert;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchPlan;
import javax.jdo.annotations.ForeignKey;
import javax.jdo.annotations.ForeignKeyAction;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.SequenceStrategy;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.VersionStrategy;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOQuery;
import org.datanucleus.api.jdo.JDOTypeConverter;
import org.datanucleus.api.jdo.JDOTypeConverterUtils;
import org.datanucleus.api.jdo.DataNucleusHelperJDO;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.ArrayMetaData;
import org.datanucleus.metadata.ClassPersistenceModifier;
import org.datanucleus.metadata.CollectionMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.ContainerMetaData;
import org.datanucleus.metadata.DiscriminatorMetaData;
import org.datanucleus.metadata.ElementMetaData;
import org.datanucleus.metadata.EmbeddedMetaData;
import org.datanucleus.metadata.EventListenerMetaData;
import org.datanucleus.metadata.FetchGroupMemberMetaData;
import org.datanucleus.metadata.FetchGroupMetaData;
import org.datanucleus.metadata.FetchPlanMetaData;
import org.datanucleus.metadata.FieldMetaData;
import org.datanucleus.metadata.FieldPersistenceModifier;
import org.datanucleus.metadata.FileMetaData;
import org.datanucleus.metadata.ForeignKeyMetaData;
import org.datanucleus.metadata.DatastoreIdentityMetaData;
import org.datanucleus.metadata.ValueGenerationStrategy;
import org.datanucleus.metadata.IdentityType;
import org.datanucleus.metadata.IndexMetaData;
import org.datanucleus.metadata.IndexedValue;
import org.datanucleus.metadata.InheritanceMetaData;
import org.datanucleus.metadata.InvalidClassMetaDataException;
import org.datanucleus.metadata.InvalidMetaDataException;
import org.datanucleus.metadata.JoinMetaData;
import org.datanucleus.metadata.KeyMetaData;
import org.datanucleus.metadata.MapMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.metadata.MetaDataUtils;
import org.datanucleus.metadata.OrderMetaData;
import org.datanucleus.metadata.PackageMetaData;
import org.datanucleus.metadata.PrimaryKeyMetaData;
import org.datanucleus.metadata.PropertyMetaData;
import org.datanucleus.metadata.QueryLanguage;
import org.datanucleus.metadata.QueryMetaData;
import org.datanucleus.metadata.SequenceMetaData;
import org.datanucleus.metadata.UniqueMetaData;
import org.datanucleus.metadata.ValueMetaData;
import org.datanucleus.metadata.VersionMetaData;
import org.datanucleus.metadata.annotations.AbstractAnnotationReader;
import org.datanucleus.metadata.annotations.AnnotationObject;
import org.datanucleus.metadata.annotations.Member;
import org.datanucleus.store.types.TypeManager;
import org.datanucleus.store.types.containers.ContainerHandler;
//import org.datanucleus.store.types.converters.TypeConverter;
import org.datanucleus.util.ClassUtils;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * Implementation for Annotation Reader for java annotations using the JDO definition.
 */
public class JDOAnnotationReader extends AbstractAnnotationReader
{
    /**
     * Constructor.
     * @param mgr MetaData manager
     */
    public JDOAnnotationReader(MetaDataManager mgr)
    {
        super(mgr);

        // We support JDO standard and DataNucleus JDO extension annotations in this reader.
        setSupportedAnnotationPackages(new String[]{"javax.jdo", "org.datanucleus.api.jdo.annotations"});

        // Create default AnnotationObject for PersistenceCapable to allow merging annotations
        persistenceCapableDefaults = getAnnotationObjectsForAnnotations(PersistenceCapableModel.class.getSimpleName(), PersistenceCapableModel.class.getAnnotations())[0];
        addSupportedDuplicateAnnotations(PersistenceCapable.class.getName());
    }

    @javax.jdo.annotations.PersistenceCapable
    protected class PersistenceCapableModel {}

    AnnotationObject persistenceCapableDefaults = null;

    /**
     * Method to process the "class" level annotations and create the outline ClassMetaData object.
     * Supports classes annotated with @PersistenceCapable, classes annotated with @PersistenceAware, and classes which have neither of those but have @Queries or @Query.
     * @param pmd Parent PackageMetaData
     * @param cls The class
     * @param annotations Annotations for this class
     * @param clr ClassLoader resolver
     * @return The ClassMetaData/InterfaceMetaData (or null if no annotations)
     */
    protected AbstractClassMetaData processClassAnnotations(PackageMetaData pmd, Class cls, AnnotationObject[] annotations, ClassLoaderResolver clr)
    {
        if (annotations == null || annotations.length == 0)
        {
            return null;
        }

        AbstractClassMetaData cmd = null;

        AnnotationObject pcAnnotation = isClassPersistable(annotations);
        if (pcAnnotation != null)
        {
            // PersistenceCapable class
            cmd = (cls.isInterface()) ? pmd.newInterfaceMetaData(ClassUtils.getClassNameForClass(cls)) : pmd.newClassMetaData(ClassUtils.getClassNameForClass(cls));
            cmd.setPersistenceModifier(ClassPersistenceModifier.PERSISTENCE_CAPABLE);

            // Process all attributes here in case needed for other annotations
            processPersistenceCapableAnnotation(cls, cmd, pcAnnotation.getNameValueMap());
        }
        else if (isClassPersistenceAware(annotations))
        {
            // PersistenceAware class
            cmd = pmd.newClassMetaData(ClassUtils.getClassNameForClass(cls));
            cmd.setPersistenceModifier(ClassPersistenceModifier.PERSISTENCE_AWARE);
        }
        else if (doesClassHaveNamedQueries(annotations))
        {
            // Class with named query specified
            cmd = pmd.newClassMetaData(ClassUtils.getClassNameForClass(cls));
            cmd.setPersistenceModifier(ClassPersistenceModifier.NON_PERSISTENT);
        }
        else
        {
            // Not involved in the persistence process
            return null;
        }

        // Cater for named queries being specified on a persistence aware, or other class
        processNamedQueries(cmd, cls, annotations);

        if (cmd.getPersistenceModifier() != ClassPersistenceModifier.PERSISTENCE_CAPABLE)
        {
            // Not persistable, so no further information needed
            return cmd;
        }

        // Class is persistable so process annotations
        for (AnnotationObject annotation : annotations)
        {
            String annName = annotation.getName();
            if (annName.equals(JDOAnnotationUtils.PERSISTENCE_CAPABLE))
            {
                // @PersistenceCapable is merged and processed above
                continue;
            }

            Map<String, Object> annotationValues = annotation.getNameValueMap();

            if (annName.equals(JDOAnnotationUtils.EMBEDDED_ONLY))
            {
                cmd.setEmbeddedOnly(true);
            }
            else if (annName.equals(JDOAnnotationUtils.VERSION))
            {
                VersionStrategy versionStrategy = (VersionStrategy) annotationValues.get("strategy");
                String strategy = JDOAnnotationUtils.getVersionStrategyString(versionStrategy);
                String indexed = (String) annotationValues.get("indexed");
                String column = (String) annotationValues.get("column");
                Column[] columns = (Column[]) annotationValues.get("columns");
                VersionMetaData vermd = cmd.newVersionMetaData();
                vermd.setStrategy(strategy);
                vermd.setColumnName(column);
                vermd.setIndexed(IndexedValue.getIndexedValue(indexed));
                if (columns != null && columns.length > 0)
                {
                    // Only use the first column
                    ColumnMetaData colmd = JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(columns[0]);
                    vermd.setColumnMetaData(colmd);
                }
                JDOAnnotationUtils.addExtensionsToMetaData(vermd, (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.DATASTORE_IDENTITY))
            {
                String strategy = JDOAnnotationUtils.getValueGenerationStrategyString((IdGeneratorStrategy) annotationValues.get("strategy"));
                String customStrategy = (String) annotationValues.get("customStrategy");
                if (!StringUtils.isWhitespace(customStrategy))
                {
                    // User has provided an extension strategy
                    strategy = customStrategy;
                }
                String sequence = (String) annotationValues.get("sequence");
                String column = (String) annotationValues.get("column");
                Column[] columns = (Column[]) annotationValues.get("columns");
                DatastoreIdentityMetaData idmd = cmd.newDatastoreIdentityMetaData();
                idmd.setColumnName(column);
                idmd.setValueStrategy(ValueGenerationStrategy.getIdentityStrategy(strategy));
                idmd.setSequence(sequence);
                if (columns != null && columns.length > 0)
                {
                    // Only use the first column
                    ColumnMetaData colmd = JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(columns[0]);
                    idmd.setColumnMetaData(colmd);
                }
                JDOAnnotationUtils.addExtensionsToMetaData(idmd, (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.PRIMARY_KEY))
            {
                String pkName = (String) annotationValues.get("name");
                String pkColumn = (String) annotationValues.get("column");
                Column[] columns = (Column[]) annotationValues.get("columns");
                PrimaryKeyMetaData pkmd = cmd.newPrimaryKeyMetaData();
                pkmd.setName(pkName);
                pkmd.setColumnName(pkColumn);
                if (columns != null && columns.length > 0)
                {
                    for (Column column : columns)
                    {
                        pkmd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(column));
                    }
                }
                JDOAnnotationUtils.addExtensionsToMetaData(pkmd, (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.JOINS))
            {
                Join[] js = (Join[]) annotationValues.get("value");
                if (js != null && js.length > 0)
                {
                    for (Join join : js)
                    {
                        JoinMetaData joinmd = cmd.newJoinMetaData();
                        joinmd.setTable(join.table());
                        joinmd.setColumnName(join.column());
                        joinmd.setIndexed(IndexedValue.getIndexedValue(join.indexed()));
                        joinmd.setOuter(MetaDataUtils.getBooleanForString(join.outer(), false));
                        joinmd.setUnique(join.unique());
                        joinmd.setDeleteAction(JDOAnnotationUtils.getForeignKeyActionString(join.deleteAction()));
                        JDOAnnotationUtils.addExtensionsToMetaData(joinmd, join.extensions());
                    }
                }
            }
            else if (annName.equals(JDOAnnotationUtils.JOIN))
            {
                JoinMetaData joinmd = cmd.newJoinMetaData();
                joinmd.setTable((String) annotationValues.get("table"));
                joinmd.setColumnName((String) annotationValues.get("column"));
                joinmd.setIndexed(IndexedValue.getIndexedValue((String) annotationValues.get("indexed")));
                joinmd.setOuter(MetaDataUtils.getBooleanForString((String) annotationValues.get("outer"), false));
                joinmd.setUnique((String) annotationValues.get("unique"));
                joinmd.setDeleteAction(((ForeignKeyAction) annotationValues.get("deleteAction")).toString());
                JDOAnnotationUtils.addExtensionsToMetaData(joinmd, (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.INHERITANCE))
            {
                String strategy = JDOAnnotationUtils.getInheritanceStrategyString((InheritanceStrategy) annotationValues.get("strategy"));
                String customStrategy = (String) annotationValues.get("customStrategy");
                if (!StringUtils.isWhitespace(customStrategy))
                {
                    // User has provided an extension strategy
                    strategy = customStrategy;
                }

                InheritanceMetaData inhmd = cmd.getInheritanceMetaData();
                if (inhmd == null)
                {
                    inhmd = cmd.newInheritanceMetaData();
                }
                inhmd.setStrategy(strategy);
            }
            else if (annName.equals(JDOAnnotationUtils.DISCRIMINATOR))
            {
                DiscriminatorStrategy discriminatorStrategy = (DiscriminatorStrategy) annotationValues.get("strategy");
                String strategy = JDOAnnotationUtils.getDiscriminatorStrategyString(discriminatorStrategy);
                String column = (String) annotationValues.get("column");
                String indexed = (String) annotationValues.get("indexed");
                String value = (String) annotationValues.get("value");
                Column[] columns = (Column[]) annotationValues.get("columns");

                InheritanceMetaData inhmd = cmd.getInheritanceMetaData();
                if (inhmd == null)
                {
                    inhmd = cmd.newInheritanceMetaData();
                }

                DiscriminatorMetaData dismd = inhmd.newDiscriminatorMetaData();
                dismd.setColumnName(column);
                dismd.setValue(value);
                dismd.setStrategy(strategy);
                dismd.setIndexed(indexed);
                if (columns != null && columns.length > 0)
                {
                    // Only use the first column
                    ColumnMetaData colmd = JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(columns[0]);
                    dismd.setColumnMetaData(colmd);
                }
            }
            else if (annName.equals(JDOAnnotationUtils.FETCHPLANS))
            {
                FileMetaData filemd = (FileMetaData) pmd.getParent();

                FetchPlan[] plans = (FetchPlan[]) annotationValues.get("value");
                for (FetchPlan plan : plans)
                {
                    FetchPlanMetaData fpmd = filemd.newFetchPlanMetaData(plan.name());
                    fpmd.setFetchSize(plan.fetchSize());
                    fpmd.setMaxFetchDepth(plan.maxFetchDepth());
                    int numGroups = plan.fetchGroups().length;
                    for (int k = 0; k < numGroups; k++)
                    {
                        fpmd.addFetchGroup(new FetchGroupMetaData(plan.fetchGroups()[k]));
                    }
                }
            }
            else if (annName.equals(JDOAnnotationUtils.FETCHPLAN))
            {
                FileMetaData filemd = (FileMetaData) pmd.getParent();
                FetchPlanMetaData fpmd = filemd.newFetchPlanMetaData((String)annotationValues.get("name"));
                fpmd.setFetchSize(((Integer) annotationValues.get("fetchSize")).intValue());
                fpmd.setMaxFetchDepth(((Integer) annotationValues.get("maxFetchDepth")).intValue());
                String[] fpFetchGroups = (String[]) annotationValues.get("fetchGroups");
                for (String fpFetchGroup : fpFetchGroups)
                {
                    fpmd.addFetchGroup(new FetchGroupMetaData(fpFetchGroup));
                }
            }
            else if (annName.equals(JDOAnnotationUtils.FETCHGROUPS))
            {
                FetchGroup[] groups = (FetchGroup[]) annotationValues.get("value");
                for (FetchGroup group : groups)
                {
                    FetchGroupMetaData fgmd = cmd.newFetchGroupMetaData(group.name());

                    if (!StringUtils.isWhitespace(group.postLoad()))
                    {
                        fgmd.setPostLoad(Boolean.valueOf(group.postLoad()));
                    }
                    int numFields = group.members().length;
                    for (int k = 0; k < numFields; k++)
                    {
                        FetchGroupMemberMetaData fgmmd = new FetchGroupMemberMetaData(fgmd, group.members()[k].name());
                        fgmmd.setRecursionDepth(group.members()[k].recursionDepth());
                        fgmd.addMember(fgmmd);
                    }
                    int numGroups = group.fetchGroups().length;
                    for (int k = 0; k < numGroups; k++)
                    {
                        fgmd.addFetchGroup(new FetchGroupMetaData(group.fetchGroups()[k]));
                    }
                }
            }
            else if (annName.equals(JDOAnnotationUtils.FETCHGROUP))
            {
                FetchGroupMetaData fgmd = cmd.newFetchGroupMetaData((String) annotationValues.get("name"));
                String postLoadStr = (String) annotationValues.get("postLoad");
                if (!StringUtils.isWhitespace(postLoadStr))
                {
                    fgmd.setPostLoad(Boolean.valueOf(postLoadStr));
                }
                Persistent[] fields = (Persistent[]) annotationValues.get("members");
                if (fields != null)
                {
                    for (Persistent field : fields)
                    {
                        FetchGroupMemberMetaData fgmmd = new FetchGroupMemberMetaData(fgmd, field.name());
                        fgmmd.setRecursionDepth(field.recursionDepth());
                        fgmd.addMember(fgmmd);
                    }
                }
            }
            else if (annName.equals(JDOAnnotationUtils.SEQUENCE))
            {
                String seqName = (String) annotationValues.get("name");
                String seqStrategy = JDOAnnotationUtils.getSequenceStrategyString((SequenceStrategy) annotationValues.get("strategy"));
                String seqSeq = (String) annotationValues.get("datastoreSequence");
                Class seqFactory = (Class) annotationValues.get("factoryClass");
                String seqFactoryClassName = null;
                if (seqFactory != null && seqFactory != void.class)
                {
                    seqFactoryClassName = seqFactory.getName();
                }
                Integer seqSize = (Integer) annotationValues.get("allocationSize");
                Integer seqStart = (Integer) annotationValues.get("initialValue");

                if (StringUtils.isWhitespace(seqName))
                {
                    throw new InvalidClassMetaDataException("044155", cmd.getFullClassName());
                }
                SequenceMetaData seqmd = new SequenceMetaData(seqName, seqStrategy);
                seqmd.setFactoryClass(seqFactoryClassName);
                seqmd.setDatastoreSequence(seqSeq);
                if (seqSize != null)
                {
                    seqmd.setAllocationSize(seqSize);
                }
                if (seqStart != null)
                {
                    seqmd.setInitialValue(seqStart);
                }
                JDOAnnotationUtils.addExtensionsToMetaData(seqmd, (Extension[]) annotationValues.get("extensions"));

                // Sequence - currently only allowing 1 per class (should really be on the package)
                cmd.getPackageMetaData().addSequence(seqmd);
            }
            else if (annName.equals(JDOAnnotationUtils.INDICES))
            {
                // Multiple Indices for the class
                Index[] values = (Index[]) annotationValues.get("value");
                if (values != null && values.length > 0)
                {
                    for (Index idx : values)
                    {
                        IndexMetaData idxmd = JDOAnnotationUtils.getIndexMetaData(idx.name(), idx.table(), "" + idx.unique(), idx.members(), idx.columns());
                        if (idxmd.getNumberOfColumns() == 0 && idxmd.getNumberOfMembers() == 0)
                        {
                            NucleusLogger.METADATA.warn(Localiser.msg("044204", cls.getName()));
                        }
                        else
                        {
                            cmd.addIndex(idxmd);
                            idxmd.setParent(cmd);
                        }
                    }
                }
            }
            else if (annName.equals(JDOAnnotationUtils.INDEX))
            {
                // Single Index for the class
                String name = (String) annotationValues.get("name");
                String table = (String) annotationValues.get("table");
                String unique = (String) annotationValues.get("unique");
                String[] members = (String[]) annotationValues.get("members");
                Column[] columns = (Column[]) annotationValues.get("columns");

                IndexMetaData idxmd = JDOAnnotationUtils.getIndexMetaData(name, table, unique, members, columns);
                JDOAnnotationUtils.addExtensionsToMetaData(idxmd, (Extension[]) annotationValues.get("extensions"));
                if (idxmd.getNumberOfColumns() == 0 && idxmd.getNumberOfMembers() == 0)
                {
                    NucleusLogger.METADATA.warn(Localiser.msg("044204", cls.getName()));
                }
                else
                {
                    cmd.addIndex(idxmd);
                    idxmd.setParent(cmd);
                }
            }
            else if (annName.equals(JDOAnnotationUtils.UNIQUES))
            {
                // Multiple Unique Constraints for the class
                Unique[] values = (Unique[]) annotationValues.get("value");
                if (values != null && values.length > 0)
                {
                    for (Unique uni : values)
                    {
                        UniqueMetaData unimd = JDOAnnotationUtils.getUniqueMetaData(uni.name(), uni.table(), "" + uni.deferred(), uni.members(), uni.columns());
                        if (unimd.getNumberOfColumns() == 0 && unimd.getNumberOfMembers() == 0)
                        {
                            NucleusLogger.METADATA.warn(Localiser.msg("044205", cls.getName()));
                        }
                        else
                        {
                            cmd.addUniqueConstraint(unimd);
                            unimd.setParent(cmd);
                        }
                    }
                }
            }
            else if (annName.equals(JDOAnnotationUtils.UNIQUE))
            {
                // Single Unique constraint for the class
                String name = (String) annotationValues.get("name");
                String table = (String) annotationValues.get("table");
                String deferred = (String) annotationValues.get("deferred");
                String[] members = (String[]) annotationValues.get("members");
                Column[] columns = (Column[]) annotationValues.get("columns");

                UniqueMetaData unimd = JDOAnnotationUtils.getUniqueMetaData(name, table, deferred, members, columns);
                JDOAnnotationUtils.addExtensionsToMetaData(unimd, (Extension[]) annotationValues.get("extensions"));
                if (unimd.getNumberOfColumns() == 0 && unimd.getNumberOfMembers() == 0)
                {
                    NucleusLogger.METADATA.warn(Localiser.msg("044205", cls.getName()));
                }
                else
                {
                    cmd.addUniqueConstraint(unimd);
                    unimd.setParent(cmd);
                }
            }
            else if (annName.equals(JDOAnnotationUtils.FOREIGNKEYS))
            {
                // Multiple FKs for the class
                ForeignKey[] values = (ForeignKey[]) annotationValues.get("value");
                if (values != null && values.length > 0)
                {
                    for (ForeignKey fk : values)
                    {
                        String deleteAction = JDOAnnotationUtils.getForeignKeyActionString(fk.deleteAction());
                        String updateAction = JDOAnnotationUtils.getForeignKeyActionString(fk.updateAction());
                        ForeignKeyMetaData fkmd = JDOAnnotationUtils.getFKMetaData(fk.name(), fk.table(), fk.unique(), "" + fk.deferred(), deleteAction, updateAction, fk.members(), fk.columns());
                        if (fkmd.getNumberOfColumns() == 0 && fkmd.getNumberOfMembers() == 0)
                        {
                            NucleusLogger.METADATA.warn(Localiser.msg("044206", cls.getName()));
                        }
                        else
                        {
                            cmd.addForeignKey(fkmd);
                            fkmd.setParent(cmd);
                        }
                    }
                }
            }
            else if (annName.equals(JDOAnnotationUtils.FOREIGNKEY))
            {
                // Single FK constraint for the class
                String name = (String) annotationValues.get("name");
                String table = (String) annotationValues.get("table");
                String unique = (String) annotationValues.get("unique");
                String deferred = (String) annotationValues.get("deferred");
                String deleteAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("deleteAction"));
                String updateAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("updateAction"));
                String[] members = (String[]) annotationValues.get("members");
                Column[] columns = (Column[]) annotationValues.get("columns");

                ForeignKeyMetaData fkmd = JDOAnnotationUtils.getFKMetaData(name, table, unique, deferred, deleteAction, updateAction, members, columns);
                JDOAnnotationUtils.addExtensionsToMetaData(fkmd, (Extension[]) annotationValues.get("extensions"));
                if (fkmd.getNumberOfColumns() == 0 && fkmd.getNumberOfMembers() == 0)
                {
                    NucleusLogger.METADATA.warn(Localiser.msg("044206", cls.getName()));
                }
                else
                {
                    cmd.addForeignKey(fkmd);
                    fkmd.setParent(cmd);
                }
            }
            else if (annName.equals(JDOAnnotationUtils.COLUMNS))
            {
                // Unmapped column specification
                Column[] cols = (Column[]) annotationValues.get("value");
                if (cols != null && cols.length > 0)
                {
                    for (Column col : cols)
                    {
                        ColumnMetaData colmd = JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(col);
                        JDOAnnotationUtils.addExtensionsToMetaData(colmd, col.extensions());

                        colmd.setParent(cmd);
                        cmd.addUnmappedColumn(colmd);
                    }
                }
            }
            else if (annName.equals(JDOAnnotationUtils.CACHEABLE))
            {
                String cache = (String) annotationValues.get("value");
                if (cache != null && cache.equalsIgnoreCase("false"))
                {
                    cmd.setCacheable(false);
                }
            }
            else if (annName.equals(JDOAnnotationUtils.EXTENSIONS))
            {
                Extension[] values = (Extension[]) annotationValues.get("value");
                if (values != null && values.length > 0)
                {
                    for (Extension ext : values)
                    {
                        String vendorName = ext.vendorName();
                        if (StringUtils.isWhitespace(vendorName))
                        {
                            throw new InvalidMetaDataException("044160", vendorName, ext.key().toString(), ext.value().toString());
                        }
                        else if (vendorName.equalsIgnoreCase(MetaData.VENDOR_NAME))
                        {
                            cmd.addExtension(ext.key().toString(), ext.value().toString());
                        }
                    }
                }
            }
            else if (annName.equals(JDOAnnotationUtils.CONVERT))
            {
                // TODO Support this when @Convert is for TYPE also
                NucleusLogger.METADATA.warn("Don't currently support @Convert specified on a class. Specify a global default converter for the type, or specify on individual fields");
            }
            else if (annName.equals(JDOAnnotationUtils.EXTENSION))
            {
                String vendorName = (String)annotationValues.get("vendorName");
                if (StringUtils.isWhitespace(vendorName))
                {
                    throw new InvalidMetaDataException("044160", vendorName, annotationValues.get("key"), annotationValues.get("value"));
                }
                else if (vendorName.equalsIgnoreCase(MetaData.VENDOR_NAME))
                {
                    cmd.addExtension((String)annotationValues.get("key"), (String)annotationValues.get("value"));
                }
            }
            else
            {
                if (!annName.equals(JDOAnnotationUtils.PERSISTENCE_AWARE) && !annName.equals(JDOAnnotationUtils.QUERIES) && !annName.equals(JDOAnnotationUtils.QUERY))
                {
                    NucleusLogger.METADATA.debug(Localiser.msg("044203", cls.getName(), annotation.getName()));
                }
            }
        }

        NucleusLogger.METADATA.debug(Localiser.msg("044200", cls.getName(), "JDO"));

        return cmd;
    }

    private void processPersistenceCapableAnnotation(Class cls, AbstractClassMetaData cmd, Map<String, Object> annotationValues)
    {
        String identityType = JDOAnnotationUtils.getIdentityTypeString((javax.jdo.annotations.IdentityType) annotationValues.get("identityType"));
        cmd.setIdentityType(IdentityType.getIdentityType(identityType));

        Class idClass = (Class) annotationValues.get("objectIdClass");
        if (idClass != null && idClass != void.class)
        {
            cmd.setObjectIdClass(DataNucleusHelperJDO.getObjectIdClassForInputIdClass(idClass.getName()));
        }

        cmd.setEmbeddedOnly((String) annotationValues.get("embeddedOnly"));
        cmd.setCacheable((String) annotationValues.get("cacheable"));

        String serializeRead = (String) annotationValues.get("serializeRead");
        if (serializeRead != null)
        {
            cmd.setSerializeRead(serializeRead.equals("true") ? true : false);
        }
        cmd.setRequiresExtent((String) annotationValues.get("requiresExtent"));

        if (mmgr.getNucleusContext().getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_METADATA_ALWAYS_DETACHABLE))
        {
            cmd.setDetachable(true);
        }
        else
        {
            cmd.setDetachable((String) annotationValues.get("detachable"));
        }

        JDOAnnotationUtils.addExtensionsToMetaData(cmd, (Extension[]) annotationValues.get("extensions"));

        String tblName = (String) annotationValues.get("table");
        if (!StringUtils.isWhitespace(tblName))
        {
            cmd.setTable(tblName);
        }
        String catName = (String) annotationValues.get("catalog");
        if (!StringUtils.isWhitespace(catName))
        {
            cmd.setCatalog(catName);
        }
        String schName = (String) annotationValues.get("schema");
        if (!StringUtils.isWhitespace(schName))
        {
            cmd.setSchema(schName);
        }

        // Members typically providing specification of overridden fields/properties
        Persistent[] members = (Persistent[]) annotationValues.get("members");
        if (members != null)
        {
            // Add on the fields/properties direct to the metadata for the class/interface
            for (Persistent member : members)
            {
                String memberName = member.name();
                if (memberName.indexOf('.') > 0)
                {
                    memberName = memberName.substring(memberName.lastIndexOf('.') + 1);
                }
                boolean isField = isMemberOfClassAField(cls, memberName);
                AbstractMemberMetaData fmd = getFieldMetaDataForPersistent(cmd, member, isField);
                cmd.addMember(fmd);
            }
        }
    }

    /**
     * Convenience method to process @Queries, @Query.
     * @param cmd Metadata for the class, to which any queries will be added
     * @param cls Class that the named queries are registered against
     * @param annotations Annotations specified on the class
     */
    protected void processNamedQueries(AbstractClassMetaData cmd, Class cls, AnnotationObject[] annotations)
    {
        for (AnnotationObject annotation : annotations)
        {
            Map<String, Object> annotationValues = annotation.getNameValueMap();
            String annName = annotation.getName();

            if (annName.equals(JDOAnnotationUtils.QUERIES))
            {
                Query[] qs = (Query[]) annotationValues.get("value");
                for (Query query : qs)
                {
                    String lang = JDOAnnotationUtils.getQueryLanguageName(query.language());
                    if (!StringUtils.isWhitespace(lang))
                    {
                        if (lang.equals(JDOQuery.JDOQL_QUERY_LANGUAGE)) // Convert to JDOQL
                        {
                            lang = QueryLanguage.JDOQL.name();
                        }
                        else if (lang.equals(JDOQuery.SQL_QUERY_LANGUAGE)) // Convert to SQL
                        {
                            lang = QueryLanguage.SQL.name();
                        }
                        else if (lang.equals(JDOQuery.JPQL_QUERY_LANGUAGE)) // Convert to JPQL
                        {
                            lang = QueryLanguage.JPQL.name();
                        }
                    }
                    String resultClassName = (query.resultClass() != null && query.resultClass() != void.class ? query.resultClass().getName() : null);
                    if (StringUtils.isWhitespace(query.name()))
                    {
                        throw new InvalidClassMetaDataException("044154", cmd.getFullClassName());
                    }
                    QueryMetaData qmd = new QueryMetaData(query.name());
                    qmd.setScope(cls.getName());
                    qmd.setLanguage(lang);
                    qmd.setUnmodifiable(query.unmodifiable());
                    qmd.setResultClass(resultClassName);
                    qmd.setUnique(query.unique());
                    qmd.setFetchPlanName(query.fetchPlan());
                    qmd.setQuery(query.value());
                    JDOAnnotationUtils.addExtensionsToMetaData(qmd, query.extensions());
                    cmd.addQuery(qmd);
                    qmd.setParent(cmd);
                }
            }
            else if (annName.equals(JDOAnnotationUtils.QUERY))
            {
                String unmodifiable = "" + annotationValues.get("unmodifiable");
                Class resultClassValue = (Class) annotationValues.get("resultClass");
                String resultClassName = (resultClassValue != null && resultClassValue != void.class ? resultClassValue.getName() : null);
                String lang = JDOAnnotationUtils.getQueryLanguageName((String) annotationValues.get("language"));
                if (!StringUtils.isWhitespace(lang))
                {
                    if (lang.equals(JDOQuery.JDOQL_QUERY_LANGUAGE)) // Convert to JDOQL
                    {
                        lang = QueryLanguage.JDOQL.name();
                    }
                    else if (lang.equals(JDOQuery.SQL_QUERY_LANGUAGE)) // Convert to SQL
                    {
                        lang = QueryLanguage.SQL.name();
                    }
                    else if (lang.equals(JDOQuery.JPQL_QUERY_LANGUAGE)) // Convert to JPQL
                    {
                        lang = QueryLanguage.JPQL.name();
                    }
                }
                if (StringUtils.isWhitespace((String) annotationValues.get("name")))
                {
                    throw new InvalidClassMetaDataException("044154", cmd.getFullClassName());
                }
                QueryMetaData qmd = new QueryMetaData((String) annotationValues.get("name"));
                qmd.setScope(cls.getName());
                qmd.setLanguage(lang);
                qmd.setUnmodifiable(unmodifiable);
                qmd.setResultClass(resultClassName);
                qmd.setUnique((String) annotationValues.get("unique"));
                qmd.setFetchPlanName((String) annotationValues.get("fetchPlan"));
                qmd.setQuery((String) annotationValues.get("value"));
                JDOAnnotationUtils.addExtensionsToMetaData(qmd, (Extension[]) annotationValues.get("extensions"));
                cmd.addQuery(qmd);
                qmd.setParent(cmd);
            }
        }
    }

    /**
     * Convenience method to process the annotations for a field/property. The passed annotations may have been specified on the field or on the getter methods.
     * @param cmd The ClassMetaData/InterfaceMetaData to update
     * @param member The field/property
     * @param annotations Annotations for the field/property
     * @return The FieldMetaData/PropertyMetaData that was added (if any)
     */
    protected AbstractMemberMetaData processMemberAnnotations(AbstractClassMetaData cmd, Member member, AnnotationObject[] annotations)
    {
        if (annotations == null || annotations.length == 0)
        {
            return null;
        }

        AbstractMemberMetaData mmd = null;

        boolean primaryKey = false;
        boolean serialised = false;
        boolean embeddedMember = false;
        boolean nonPersistentField = false;
        boolean transactionalField = false;
        String cacheable = null;

        Class[] elementTypes = null;
        String embeddedElement = null;
        String serializedElement = null;
        String dependentElement = null;

        Class[] keyTypes = null;
        String embeddedKey = null;
        String serializedKey = null;
        String dependentKey = null;

        Class[] valueTypes = null;
        String embeddedValue = null;
        String serializedValue = null;
        String dependentValue = null;

        String embeddedOwnerField = null;
        String embeddedNullIndicatorColumn = null;
        String embeddedNullIndicatorValue = null;
        Persistent[] embeddedMembers = null;
        Persistent[] embeddedElementMembers = null;
        Persistent[] embeddedKeyMembers = null;
        Persistent[] embeddedValueMembers = null;

        ColumnMetaData[] colmds = null;
        JoinMetaData joinmd = null;
        ElementMetaData elemmd = null;
        KeyMetaData keymd = null;
        ValueMetaData valuemd = null;
        OrderMetaData ordermd = null;
        IndexMetaData idxmd = null;
        UniqueMetaData unimd = null;
        ForeignKeyMetaData fkmd = null;
        Map<String, String> extensions = null;
        Class convertConverterCls = null;
        FieldPersistenceModifier updateModifier = null;

        for (AnnotationObject annotation : annotations)
        {
            String annName = annotation.getName();
            Map<String, Object> annotationValues = annotation.getNameValueMap();

            if (annName.equals(JDOAnnotationUtils.PERSISTENT))
            {
                String pkStr = "" + annotationValues.get("primaryKey");
                Boolean pk = null;
                if (!StringUtils.isWhitespace(pkStr))
                {
                    pk = Boolean.valueOf(pkStr);
                }
                String dfgStr = (String) annotationValues.get("defaultFetchGroup");
                Boolean dfg = null;
                if (!StringUtils.isWhitespace(dfgStr))
                {
                    dfg = Boolean.valueOf(dfgStr);
                }
                String nullValue = JDOAnnotationUtils.getNullValueString((NullValue) annotationValues.get("nullValue"));
                String embStr = (String) annotationValues.get("embedded");
                Boolean embedded = null;
                if (!StringUtils.isWhitespace(embStr))
                {
                    embedded = Boolean.valueOf(embStr);
                }
                String serStr = (String) annotationValues.get("serialized");
                Boolean serialized = null;
                if (!StringUtils.isWhitespace(serStr))
                {
                    serialized = Boolean.valueOf(serStr);
                }
                String depStr = (String) annotationValues.get("dependent");
                Boolean dependent = null;
                if (!StringUtils.isWhitespace(depStr))
                {
                    dependent = Boolean.valueOf(depStr);
                }

                String valueStrategy = JDOAnnotationUtils.getValueGenerationStrategyString((IdGeneratorStrategy) annotationValues.get("valueStrategy"));
                String customValueStrategy = (String) annotationValues.get("customValueStrategy");
                if (!StringUtils.isWhitespace(customValueStrategy))
                {
                    // User has provided an extension strategy
                    valueStrategy = customValueStrategy;
                }

                FieldPersistenceModifier modifier = JDOAnnotationUtils.getFieldPersistenceModifier((PersistenceModifier) annotationValues.get("persistenceModifier"));
                if (modifier == null)
                {
                    modifier = FieldPersistenceModifier.PERSISTENT;
                }
                String sequence = (String) annotationValues.get("sequence");
                String mappedBy = (String) annotationValues.get("mappedBy");
                String table = (String) annotationValues.get("table");
                String column = (String) annotationValues.get("column");
                String loadFetchGroup = (String) annotationValues.get("loadFetchGroup");
                String fieldTypeName = null;
                int recursionDepth = ((Integer) annotationValues.get("recursionDepth")).intValue();
                cacheable = (String) annotationValues.get("cacheable");
                Class[] fieldTypes = (Class[]) annotationValues.get("types");
                if (fieldTypes != null && fieldTypes.length > 0)
                {
                    StringBuilder typeStr = new StringBuilder();
                    for (Class fieldType : fieldTypes)
                    {
                        if (typeStr.length() > 0)
                        {
                            typeStr.append(',');
                        }
                        if (fieldType != null && fieldType != void.class)
                        {
                            typeStr.append(fieldType.getName());
                        }
                    }
                    fieldTypeName = typeStr.toString();
                }
                dependentElement = (String) annotationValues.get("dependentElement");
                serializedElement = (String) annotationValues.get("serializedElement");
                embeddedElement = (String) annotationValues.get("embeddedElement");
                dependentKey = (String) annotationValues.get("dependentKey");
                serializedKey = (String) annotationValues.get("serializedKey");
                embeddedKey = (String) annotationValues.get("embeddedKey");
                dependentValue = (String) annotationValues.get("dependentValue");
                serializedValue = (String) annotationValues.get("serializedValue");
                embeddedValue = (String) annotationValues.get("embeddedValue");
                Class converterCls = (Class) annotationValues.get("converter");
                if (converterCls == UseDefault.class)
                {
                    converterCls = null;
                }
                Boolean disableConversion = (Boolean)annotationValues.get("useDefaultConversion");

                mmd = member.isProperty() ? new PropertyMetaData(cmd, member.getName()) : new FieldMetaData(cmd, member.getName());

                if (isPersistenceContext())
                {
                    if (disableConversion != null && disableConversion)
                    {
                        mmd.setTypeConverterDisabled();
                    }
                    else if (converterCls != null)
                    {
                        TypeManager typeMgr = mmgr.getNucleusContext().getTypeManager();
                        if (typeMgr.getTypeConverterForName(converterCls.getName()) == null)
                        {
                            // Not yet cached an instance of this converter so create one
                            AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter(mmgr.getNucleusContext(), converterCls);
                            Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(converterCls, member.getType());
                            Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(converterCls, attrType, null);

                            // Register the TypeConverter under the name of the AttributeConverter class
                            JDOTypeConverter typeConv = new JDOTypeConverter(conv);
                            typeMgr.registerConverter(converterCls.getName(), typeConv, attrType, dbType, false, null);
                        }

                        mmd.setTypeConverterName(converterCls.getName());
                    }
                }

                if (modifier != null)
                {
                    mmd.setPersistenceModifier(modifier);
                }
                if (dfg != null)
                {
                    mmd.setDefaultFetchGroup(dfg);
                }
                if (pk != null)
                {
                    mmd.setPrimaryKey(pk);
                }
                if (embedded != null)
                {
                    mmd.setEmbedded(embedded);
                }
                if (serialized != null)
                {
                    mmd.setSerialised(serialized);
                }
                if (dependent != null)
                {
                    mmd.setDependent(dependent);
                }
                mmd.setNullValue(org.datanucleus.metadata.NullValue.getNullValue(nullValue));
                mmd.setMappedBy(mappedBy);
                mmd.setColumn(column);
                mmd.setTable(table);
                mmd.setRecursionDepth(recursionDepth); // TODO Not applicable when just for a member
                mmd.setLoadFetchGroup(loadFetchGroup);
                mmd.setValueStrategy(valueStrategy);
                mmd.setSequence(sequence);
                mmd.setFieldTypes(fieldTypeName);

                // Add any columns defined on the @Persistent
                Column[] columns = (Column[]) annotationValues.get("columns");
                if (columns != null && columns.length > 0)
                {
                    for (Column col : columns)
                    {
                        mmd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(col));
                    }
                }
                JDOAnnotationUtils.addExtensionsToMetaData(mmd, (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.PRIMARY_KEY))
            {
                primaryKey = true;
                if (cmd.getIdentityType() == IdentityType.DATASTORE)
                {
                    // ClassMetaData was created as DATASTORE so change it to APPLICATION
                    cmd.setIdentityType(IdentityType.APPLICATION);
                }
            }
            else if (annName.equals(JDOAnnotationUtils.SERIALIZED))
            {
                serialised = true;
            }
            else if (annName.equals(JDOAnnotationUtils.NOTPERSISTENT))
            {
                nonPersistentField = true;
            }
            else if (annName.equals(JDOAnnotationUtils.TRANSACTIONAL))
            {
                transactionalField = true;
            }
            else if (annName.equals(JDOAnnotationUtils.VERSION))
            {
                // Tag this field as the version field. We ignore the column/columns attributes since specifiable on the field itself
                VersionMetaData vermd = cmd.newVersionMetaData();
                vermd.setMemberName(member.getName());
                VersionStrategy versionStrategy = (VersionStrategy) annotationValues.get("strategy");
                String strategy = null;
                if (versionStrategy == VersionStrategy.UNSPECIFIED)
                {
                    if (Number.class.isAssignableFrom(member.getType()))
                    {
                        strategy = "version-number";
                    }
                    else if (Date.class.isAssignableFrom(member.getType()) || Instant.class.isAssignableFrom(member.getType()) || 
                            LocalTime.class.isAssignableFrom(member.getType()) || LocalDateTime.class.isAssignableFrom(member.getType()))
                    {
                        strategy = "date-time";
                    }
                }
                else
                {
                    strategy = JDOAnnotationUtils.getVersionStrategyString(versionStrategy);
                }
                vermd.setStrategy(strategy);
                String indexed = (String) annotationValues.get("indexed");
                vermd.setIndexed(IndexedValue.getIndexedValue(indexed));
                JDOAnnotationUtils.addExtensionsToMetaData(vermd, (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.COLUMNS))
            {
                // Multiple column specification
                Column[] cols = (Column[]) annotationValues.get("value");
                if (cols != null && cols.length > 0)
                {
                    colmds = new ColumnMetaData[cols.length];
                    for (int j = 0; j < cols.length; j++)
                    {
                        colmds[j] = JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(cols[j]);
                        JDOAnnotationUtils.addExtensionsToMetaData(colmds[j], cols[j].extensions());
                    }
                }
            }
            else if (annName.equals(JDOAnnotationUtils.COLUMN))
            {
                // Single column specification
                colmds = new ColumnMetaData[1];
                colmds[0] = JDOAnnotationUtils.getColumnMetaDataForAnnotations(annotationValues);
                JDOAnnotationUtils.addExtensionsToMetaData(colmds[0], (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.JOIN))
            {
                String joinColumn = (String) annotationValues.get("column");
                String joinOuter = (String) annotationValues.get("outer");
                String deleteAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("deleteAction"));
                String pkName = (String) annotationValues.get("primaryKey");
                String fkName = (String) annotationValues.get("foreignKey");
                String generateFK = (String) annotationValues.get("generateForeignKey");
                String indexed = (String) annotationValues.get("indexed");
                String indexName = (String) annotationValues.get("index");
                String unique = (String) annotationValues.get("unique");
                String uniqueName = (String) annotationValues.get("uniqueKey");
                String generatePK = (String) annotationValues.get("generatePrimaryKey");
                if (!StringUtils.isWhitespace(uniqueName))
                {
                    unique = "true";
                }
                if (!StringUtils.isWhitespace(indexName))
                {
                    indexed = "true";
                }
                Column[] joinColumns = (Column[]) annotationValues.get("columns");
                joinmd = new JoinMetaData();
                joinmd.setColumnName(joinColumn);
                joinmd.setOuter(MetaDataUtils.getBooleanForString(joinOuter, false));
                joinmd.setIndexed(IndexedValue.getIndexedValue(indexed));
                joinmd.setUnique(unique);
                joinmd.setDeleteAction(deleteAction);

                if (!StringUtils.isWhitespace(pkName))
                {
                    PrimaryKeyMetaData pkmd = new PrimaryKeyMetaData();
                    pkmd.setName(pkName);
                    joinmd.setPrimaryKeyMetaData(pkmd);
                }
                else if (generatePK != null && generatePK.equalsIgnoreCase("true"))
                {
                    joinmd.setPrimaryKeyMetaData(new PrimaryKeyMetaData());
                }

                if (!StringUtils.isWhitespace(fkName))
                {
                    ForeignKeyMetaData joinFkmd = joinmd.getForeignKeyMetaData();
                    if (joinFkmd == null)
                    {
                        joinFkmd = new ForeignKeyMetaData();
                        joinFkmd.setName(fkName);
                        joinmd.setForeignKeyMetaData(joinFkmd);
                    }
                    else
                    {
                        joinFkmd.setName(fkName);
                    }
                }
                else if (generateFK != null && generateFK.equalsIgnoreCase("true"))
                {
                    joinmd.setForeignKeyMetaData(new ForeignKeyMetaData());
                }

                if (!StringUtils.isWhitespace(indexName))
                {
                    IndexMetaData joinIdxmd = joinmd.getIndexMetaData();
                    if (joinIdxmd == null)
                    {
                        joinIdxmd = new IndexMetaData();
                        joinmd.setIndexMetaData(joinIdxmd);
                    }
                    joinIdxmd.setName(indexName);
                }

                if (!StringUtils.isWhitespace(uniqueName))
                {
                    UniqueMetaData joinUnimd = joinmd.getUniqueMetaData();
                    if (joinUnimd == null)
                    {
                        joinUnimd = new UniqueMetaData();
                        joinmd.setUniqueMetaData(joinUnimd);
                    }
                    joinUnimd.setName(uniqueName);
                }
                if (joinColumns != null && joinColumns.length > 0)
                {
                    for (Column joinCol : joinColumns)
                    {
                        joinmd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(joinCol));
                    }
                }
                JDOAnnotationUtils.addExtensionsToMetaData(joinmd, (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.ELEMENT))
            {
                // Element of a Collection/Array
                elementTypes = (Class[]) annotationValues.get("types");
                embeddedElement = (String) annotationValues.get("embedded");
                serializedElement = (String) annotationValues.get("serialized");
                dependentElement = (String) annotationValues.get("dependent");

                String elementDeleteAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("deleteAction"));
                String elementUpdateAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("updateAction"));
                String fkName = (String) annotationValues.get("foreignKey");
                String generateFK = (String) annotationValues.get("generateForeignKey");
                String indexed = (String) annotationValues.get("indexed");
                String indexName = (String) annotationValues.get("index");
                String unique = (String) annotationValues.get("unique");
                String uniqueName = (String) annotationValues.get("uniqueKey");
                Class converterCls = (Class) annotationValues.get("converter");
                if (converterCls == UseDefault.class)
                {
                    converterCls = null;
                }
                Boolean disableConversion = (Boolean)annotationValues.get("useDefaultConversion");

                if (!StringUtils.isWhitespace(uniqueName))
                {
                    unique = "true";
                }
                if (!StringUtils.isWhitespace(indexName))
                {
                    indexed = "true";
                }
                elemmd = new ElementMetaData();
                elemmd.setTable((String) annotationValues.get("table"));
                elemmd.setColumnName((String) annotationValues.get("column"));
                elemmd.setDeleteAction(elementDeleteAction);
                elemmd.setUpdateAction(elementUpdateAction);
                elemmd.setIndexed(IndexedValue.getIndexedValue(indexed));
                elemmd.setUnique(MetaDataUtils.getBooleanForString(unique, false));
                elemmd.setMappedBy((String) annotationValues.get("mappedBy"));
                if (!StringUtils.isWhitespace(fkName))
                {
                    ForeignKeyMetaData elemFkmd = elemmd.getForeignKeyMetaData();
                    if (elemFkmd == null)
                    {
                        elemFkmd = new ForeignKeyMetaData();
                        elemFkmd.setName(fkName);
                        elemmd.setForeignKeyMetaData(elemFkmd);
                    }
                    else
                    {
                        elemFkmd.setName(fkName);
                    }
                }
                else if (generateFK != null && generateFK.equalsIgnoreCase("true"))
                {
                    elemmd.setForeignKeyMetaData(new ForeignKeyMetaData());
                }

                if (!StringUtils.isWhitespace(indexName))
                {
                    IndexMetaData elemIdxmd = elemmd.getIndexMetaData();
                    if (elemIdxmd == null)
                    {
                        elemIdxmd = new IndexMetaData();
                        elemmd.setIndexMetaData(elemIdxmd);
                    }
                    elemIdxmd.setName(indexName);
                }

                if (!StringUtils.isWhitespace(uniqueName))
                {
                    UniqueMetaData elemUnimd = elemmd.getUniqueMetaData();
                    if (elemUnimd == null)
                    {
                        elemUnimd = new UniqueMetaData();
                        elemmd.setUniqueMetaData(elemUnimd);
                    }
                    elemUnimd.setName(uniqueName);
                }

                Column[] elementColumns = (Column[]) annotationValues.get("columns");
                if (elementColumns != null && elementColumns.length > 0)
                {
                    for (Column elementCol : elementColumns)
                    {
                        elemmd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(elementCol));
                    }
                }

                if (isPersistenceContext())
                {
                    if (disableConversion != null && disableConversion)
                    {
                        elemmd.addExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_DISABLED, "true");
                    }
                    else if (converterCls != null)
                    {
                        TypeManager typeMgr = mmgr.getNucleusContext().getTypeManager();
                        if (typeMgr.getTypeConverterForName(converterCls.getName()) == null)
                        {
                            // Not yet cached an instance of this converter so create one
                            AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter(mmgr.getNucleusContext(), converterCls);
                            Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(converterCls, 
                                ClassUtils.getCollectionElementType(member.getType(), member.getGenericType()));
                            Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(converterCls, attrType, null);

                            // Register the TypeConverter under the name of the AttributeConverter class
                            JDOTypeConverter typeConv = new JDOTypeConverter(conv);
                            typeMgr.registerConverter(converterCls.getName(), typeConv, attrType, dbType, false, null);
                        }

                        elemmd.addExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_NAME, converterCls.getName());
                    }
                }

                JDOAnnotationUtils.addExtensionsToMetaData(elemmd, (Extension[]) annotationValues.get("extensions"));

                Embedded[] embeddedMappings = (Embedded[]) annotationValues.get("embeddedMapping");
                if (embeddedMappings != null && embeddedMappings.length > 0)
                {
                    // Embedded element
                    EmbeddedMetaData embmd = new EmbeddedMetaData();
                    embmd.setOwnerMember(embeddedMappings[0].ownerMember());
                    embmd.setNullIndicatorColumn(embeddedMappings[0].nullIndicatorColumn());
                    embmd.setNullIndicatorValue(embeddedMappings[0].nullIndicatorValue());
                    try
                    {
                        Discriminator disc = embeddedMappings[0].discriminatorColumnName();
                        if (disc != null)
                        {
                            DiscriminatorMetaData dismd = embmd.newDiscriminatorMetaData();
                            dismd.setColumnName(disc.column());
                            dismd.setStrategy(JDOAnnotationUtils.getDiscriminatorStrategyString(disc.strategy()));
                            // TODO Support other attributes of discriminator?
                        }
                    }
                    catch (Throwable thr)
                    {
                        // Ignore this. Maybe not using JDO3.1 jar
                    }
                    elemmd.setEmbeddedMetaData(embmd);
                    embeddedElementMembers = embeddedMappings[0].members();
                    // Delay addition of embeddedElementMembers til completion of this loop so we have the
                    // element type
                }
            }
            else if (annName.equals(JDOAnnotationUtils.KEY))
            {
                // Key of a Map
                keyTypes = (Class[]) annotationValues.get("types");
                embeddedKey = (String) annotationValues.get("embedded");
                serializedKey = (String) annotationValues.get("serialized");
                dependentKey = (String) annotationValues.get("dependent");

                String keyDeleteAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("deleteAction"));
                String keyUpdateAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("updateAction"));
                String fkName = (String) annotationValues.get("foreignKey");
                String generateFK = (String) annotationValues.get("generateForeignKey");
                String indexed = (String) annotationValues.get("indexed");
                String indexName = (String) annotationValues.get("index");
                String unique = (String) annotationValues.get("unique");
                String uniqueName = (String) annotationValues.get("uniqueKey");
                Class converterCls = (Class) annotationValues.get("converter");
                if (converterCls == UseDefault.class)
                {
                    converterCls = null;
                }
                Boolean disableConversion = (Boolean)annotationValues.get("useDefaultConversion");

                if (!StringUtils.isWhitespace(uniqueName))
                {
                    unique = "true";
                }
                if (!StringUtils.isWhitespace(indexName))
                {
                    indexed = "true";
                }
                keymd = new KeyMetaData();
                keymd.setTable((String) annotationValues.get("table"));
                keymd.setColumnName((String) annotationValues.get("column"));
                keymd.setDeleteAction(keyDeleteAction);
                keymd.setUpdateAction(keyUpdateAction);
                keymd.setIndexed(IndexedValue.getIndexedValue(indexed));
                keymd.setUnique(MetaDataUtils.getBooleanForString(unique, false));
                keymd.setMappedBy((String) annotationValues.get("mappedBy"));
                if (!StringUtils.isWhitespace(fkName))
                {
                    ForeignKeyMetaData keyFkmd = keymd.getForeignKeyMetaData();
                    if (keyFkmd == null)
                    {
                        keyFkmd = new ForeignKeyMetaData();
                        keyFkmd.setName(fkName);
                        keymd.setForeignKeyMetaData(keyFkmd);
                    }
                    else
                    {
                        keyFkmd.setName(fkName);
                    }
                }
                else if (generateFK != null && generateFK.equalsIgnoreCase("true"))
                {
                    keymd.setForeignKeyMetaData(new ForeignKeyMetaData());
                }

                if (!StringUtils.isWhitespace(indexName))
                {
                    IndexMetaData keyIdxmd = keymd.getIndexMetaData();
                    if (keyIdxmd == null)
                    {
                        keyIdxmd = new IndexMetaData();
                        keymd.setIndexMetaData(keyIdxmd);
                    }
                    keyIdxmd.setName(indexName);
                }

                if (!StringUtils.isWhitespace(uniqueName))
                {
                    UniqueMetaData keyUnimd = keymd.getUniqueMetaData();
                    if (keyUnimd == null)
                    {
                        keyUnimd = new UniqueMetaData();
                        keymd.setUniqueMetaData(keyUnimd);
                    }
                    keyUnimd.setName(uniqueName);
                }

                Column[] keyColumns = (Column[]) annotationValues.get("columns");
                if (keyColumns != null && keyColumns.length > 0)
                {
                    for (Column keyCol : keyColumns)
                    {
                        keymd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(keyCol));
                    }
                }

                if (isPersistenceContext())
                {
                    if (disableConversion != null && disableConversion)
                    {
                        keymd.addExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_DISABLED, "true");
                    }
                    else if (converterCls != null)
                    {
                        TypeManager typeMgr = mmgr.getNucleusContext().getTypeManager();
                        if (typeMgr.getTypeConverterForName(converterCls.getName()) == null)
                        {
                            // Not yet cached an instance of this converter so create one
                            AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter(mmgr.getNucleusContext(), converterCls);
                            Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(converterCls, 
                                ClassUtils.getMapKeyType(member.getType(), member.getGenericType()));
                            Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(converterCls, attrType, null);

                            // Register the TypeConverter under the name of the AttributeConverter class
                            JDOTypeConverter typeConv = new JDOTypeConverter(conv);
                            typeMgr.registerConverter(converterCls.getName(), typeConv, attrType, dbType, false, null);
                        }

                        keymd.addExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_NAME, converterCls.getName());
                    }
                }

                JDOAnnotationUtils.addExtensionsToMetaData(keymd, (Extension[]) annotationValues.get("extensions"));

                Embedded[] embeddedMappings = (Embedded[]) annotationValues.get("embeddedMapping");
                if (embeddedMappings != null && embeddedMappings.length > 0)
                {
                    // Embedded key
                    EmbeddedMetaData embmd = new EmbeddedMetaData();
                    embmd.setOwnerMember(embeddedMappings[0].ownerMember());
                    embmd.setNullIndicatorColumn(embeddedMappings[0].nullIndicatorColumn());
                    embmd.setNullIndicatorValue(embeddedMappings[0].nullIndicatorValue());
                    keymd.setEmbeddedMetaData(embmd);
                    embeddedKeyMembers = embeddedMappings[0].members();
                    // Delay addition of embeddedKeyMembers til completion of this loop so we have the key type
                }
            }
            else if (annName.equals(JDOAnnotationUtils.VALUE))
            {
                // Value of a Map
                valueTypes = (Class[]) annotationValues.get("types");
                embeddedValue = (String) annotationValues.get("embedded");
                serializedValue = (String) annotationValues.get("serialized");
                dependentValue = (String) annotationValues.get("dependent");

                String valueDeleteAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("deleteAction"));
                String valueUpdateAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("updateAction"));
                String fkName = (String) annotationValues.get("foreignKey");
                String generateFK = (String) annotationValues.get("generateForeignKey");
                String indexed = (String) annotationValues.get("indexed");
                String indexName = (String) annotationValues.get("index");
                String unique = (String) annotationValues.get("unique");
                String uniqueName = (String) annotationValues.get("uniqueKey");
                Class converterCls = (Class) annotationValues.get("converter");
                if (converterCls == UseDefault.class)
                {
                    converterCls = null;
                }
                Boolean disableConversion = (Boolean)annotationValues.get("useDefaultConversion");

                if (!StringUtils.isWhitespace(uniqueName))
                {
                    unique = "true";
                }
                if (!StringUtils.isWhitespace(indexName))
                {
                    indexed = "true";
                }
                valuemd = new ValueMetaData();
                valuemd.setTable((String) annotationValues.get("table"));
                valuemd.setColumnName((String) annotationValues.get("column"));
                valuemd.setDeleteAction(valueDeleteAction);
                valuemd.setUpdateAction(valueUpdateAction);
                valuemd.setIndexed(IndexedValue.getIndexedValue(indexed));
                valuemd.setUnique(MetaDataUtils.getBooleanForString(unique, false));
                valuemd.setMappedBy((String) annotationValues.get("mappedBy"));
                if (!StringUtils.isWhitespace(fkName))
                {
                    ForeignKeyMetaData valueFkmd = valuemd.getForeignKeyMetaData();
                    if (valueFkmd == null)
                    {
                        valueFkmd = new ForeignKeyMetaData();
                        valueFkmd.setName(fkName);
                        valuemd.setForeignKeyMetaData(valueFkmd);
                    }
                    else
                    {
                        valueFkmd.setName(fkName);
                    }
                }
                else if (generateFK != null && generateFK.equalsIgnoreCase("true"))
                {
                    valuemd.setForeignKeyMetaData(new ForeignKeyMetaData());
                }

                if (!StringUtils.isWhitespace(indexName))
                {
                    IndexMetaData valueIdxmd = valuemd.getIndexMetaData();
                    if (valueIdxmd == null)
                    {
                        valueIdxmd = new IndexMetaData();
                        valuemd.setIndexMetaData(valueIdxmd);
                    }
                    valueIdxmd.setName(indexName);
                }

                if (!StringUtils.isWhitespace(uniqueName))
                {
                    UniqueMetaData valueUnimd = valuemd.getUniqueMetaData();
                    if (valueUnimd == null)
                    {
                        valueUnimd = new UniqueMetaData();
                        valuemd.setUniqueMetaData(valueUnimd);
                    }
                    valueUnimd.setName(uniqueName);
                }

                Column[] valueColumns = (Column[]) annotationValues.get("columns");
                if (valueColumns != null && valueColumns.length > 0)
                {
                    for (Column valueCol : valueColumns)
                    {
                        valuemd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(valueCol));
                    }
                }

                if (isPersistenceContext())
                {
                    if (disableConversion != null && disableConversion)
                    {
                        valuemd.addExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_DISABLED, "true");
                    }
                    else if (converterCls != null)
                    {
                        TypeManager typeMgr = mmgr.getNucleusContext().getTypeManager();
                        if (typeMgr.getTypeConverterForName(converterCls.getName()) == null)
                        {
                            // Not yet cached an instance of this converter so create one
                            AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter(mmgr.getNucleusContext(), converterCls);
                            Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(converterCls, 
                                ClassUtils.getMapValueType(member.getType(), member.getGenericType()));
                            Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(converterCls, attrType, null);

                            // Register the TypeConverter under the name of the AttributeConverter class
                            JDOTypeConverter typeConv = new JDOTypeConverter(conv);
                            typeMgr.registerConverter(converterCls.getName(), typeConv, attrType, dbType, false, null);
                        }

                        valuemd.addExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_NAME, converterCls.getName());
                    }
                }

                JDOAnnotationUtils.addExtensionsToMetaData(valuemd, (Extension[]) annotationValues.get("extensions"));

                Embedded[] embeddedMappings = (Embedded[]) annotationValues.get("embeddedMapping");
                if (embeddedMappings != null && embeddedMappings.length > 0)
                {
                    // Embedded value
                    EmbeddedMetaData embmd = new EmbeddedMetaData();
                    embmd.setOwnerMember(embeddedMappings[0].ownerMember());
                    embmd.setNullIndicatorColumn(embeddedMappings[0].nullIndicatorColumn());
                    embmd.setNullIndicatorValue(embeddedMappings[0].nullIndicatorValue());
                    valuemd.setEmbeddedMetaData(embmd);
                    embeddedValueMembers = embeddedMappings[0].members();
                    // Delay addition of embeddedValueMembers til completion of this loop so we have the value type
                }
            }
            else if (annName.equals(JDOAnnotationUtils.ORDER))
            {
                ordermd = new OrderMetaData();
                ordermd.setColumnName((String) annotationValues.get("column"));
                ordermd.setMappedBy((String) annotationValues.get("mappedBy"));

                Column[] orderColumns = (Column[]) annotationValues.get("columns");
                if (orderColumns != null && orderColumns.length > 0)
                {
                    for (Column orderCol : orderColumns)
                    {
                        ordermd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(orderCol));
                    }
                }
                JDOAnnotationUtils.addExtensionsToMetaData(ordermd, (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.EMBEDDED))
            {
                embeddedMember = true;
                embeddedOwnerField = (String) annotationValues.get("ownerMember");
                if (StringUtils.isWhitespace(embeddedOwnerField))
                {
                    embeddedOwnerField = null;
                }
                embeddedNullIndicatorColumn = (String) annotationValues.get("nullIndicatorColumn");
                if (StringUtils.isWhitespace(embeddedNullIndicatorColumn))
                {
                    embeddedNullIndicatorColumn = null;
                }
                embeddedNullIndicatorValue = (String) annotationValues.get("nullIndicatorValue");
                if (StringUtils.isWhitespace(embeddedNullIndicatorValue))
                {
                    embeddedNullIndicatorValue = null;
                }
                embeddedMembers = (Persistent[]) annotationValues.get("members");
                if (embeddedMembers != null && embeddedMembers.length == 0)
                {
                    embeddedMembers = null;
                }
                // TODO Support discriminator
            }
            else if (annName.equals(JDOAnnotationUtils.INDEX))
            {
                // Index for the field
                String name = (String) annotationValues.get("name");
                String table = (String) annotationValues.get("table");
                String unique = (String) annotationValues.get("unique");
                String[] members = (String[]) annotationValues.get("members");
                Column[] columns = (Column[]) annotationValues.get("columns");

                idxmd = JDOAnnotationUtils.getIndexMetaData(name, table, unique, members, columns);
                JDOAnnotationUtils.addExtensionsToMetaData(idxmd, (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.UNIQUE))
            {
                // Unique for the field
                String name = (String) annotationValues.get("name");
                String table = (String) annotationValues.get("table");
                String deferred = (String) annotationValues.get("deferred");
                String[] members = (String[]) annotationValues.get("members");
                Column[] columns = (Column[]) annotationValues.get("columns");

                unimd = JDOAnnotationUtils.getUniqueMetaData(name, table, deferred, members, columns);
                JDOAnnotationUtils.addExtensionsToMetaData(unimd, (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.FOREIGNKEY))
            {
                // ForeignKey for field
                String name = (String) annotationValues.get("name");
                String table = (String) annotationValues.get("table");
                String unique = (String) annotationValues.get("unique");
                String deferred = (String) annotationValues.get("deferred");
                String deleteAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("deleteAction"));
                String updateAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("updateAction"));
                String[] members = (String[]) annotationValues.get("members");
                Column[] columns = (Column[]) annotationValues.get("columns");

                fkmd = JDOAnnotationUtils.getFKMetaData(name, table, unique, deferred, deleteAction, updateAction, members, columns);
                JDOAnnotationUtils.addExtensionsToMetaData(fkmd, (Extension[]) annotationValues.get("extensions"));
            }
            else if (annName.equals(JDOAnnotationUtils.CACHEABLE))
            {
                String cache = (String) annotationValues.get("value");
                if (cache != null)
                {
                    cacheable = cache;
                }
            }
            else if (annName.equals(JDOAnnotationUtils.CONVERT))
            {
                convertConverterCls = (Class) annotationValues.get("value");
                if (convertConverterCls == UseDefault.class)
                {
                    convertConverterCls = null;
                }
                Boolean enabled = (Boolean) annotationValues.get("enabled");
                if (!enabled)
                {
                    convertConverterCls = null;
                }
                if (convertConverterCls != null)
                {
                	// We have a converter so assume this field is to be persisted
                    updateModifier = FieldPersistenceModifier.PERSISTENT;
                }
            }
            else if (annName.equals(JDOAnnotationUtils.EXTENSIONS))
            {
                Extension[] values = (Extension[]) annotationValues.get("value");
                if (values != null && values.length > 0)
                {
                    extensions = new HashMap<String,String>(values.length);
                    for (Extension ext : values)
                    {
                        String vendorName = ext.vendorName();
                        if (StringUtils.isWhitespace(vendorName))
                        {
                            throw new InvalidMetaDataException("044160", vendorName, ext.key().toString(), ext.value().toString());
                        }
                        else if (vendorName.equalsIgnoreCase(MetaData.VENDOR_NAME))
                        {
                            extensions.put(ext.key().toString(), ext.value().toString());
                        }
                    }
                }
            }
            else if (annName.equals(JDOAnnotationUtils.EXTENSION))
            {
                String vendorName = (String)annotationValues.get("vendorName");
                if (StringUtils.isWhitespace(vendorName))
                {
                    throw new InvalidMetaDataException("044160", vendorName, annotationValues.get("key"), annotationValues.get("value"));
                }
                else if (vendorName.equalsIgnoreCase(MetaData.VENDOR_NAME))
                {
                    extensions = new HashMap<String,String>(1);
                    extensions.put((String)annotationValues.get("key"), (String)annotationValues.get("value"));
                }
            }
            else
            {
                NucleusLogger.METADATA.debug(Localiser.msg("044211", cmd.getFullClassName(), member.getName(), annotation.getName()));
            }
        }

        if (mmd == null && (transactionalField || nonPersistentField || primaryKey || colmds != null || serialised || embeddedOwnerField != null || embeddedMember ||
            embeddedNullIndicatorColumn != null || embeddedNullIndicatorValue != null || embeddedMembers != null || elemmd != null || keymd != null || valuemd != null || 
            ordermd != null || idxmd != null || unimd != null || fkmd != null || joinmd != null || extensions != null || convertConverterCls != null))
        {
            // @Persistent not supplied but other relevant annotations defined, so add default metadata element
            mmd = member.isProperty() ? new PropertyMetaData(cmd, member.getName()) : new FieldMetaData(cmd, member.getName());

            if (updateModifier != null && mmd.getPersistenceModifier() == FieldPersistenceModifier.DEFAULT)
            {
                mmd.setPersistenceModifier(updateModifier);
            }
            if (primaryKey)
            {
                mmd.setPersistenceModifier(FieldPersistenceModifier.PERSISTENT);
                mmd.setPrimaryKey(primaryKey);
            }
            if (serialised)
            {
                mmd.setPersistenceModifier(FieldPersistenceModifier.PERSISTENT);
            }
        }

        if (mmd != null)
        {
            cmd.addMember(mmd);

            if (primaryKey)
            {
                mmd.setPrimaryKey(true);
            }
            if (serialised)
            {
                mmd.setSerialised(true);
            }
            if (embeddedMember)
            {
                mmd.setEmbedded(true);
            }
            if (nonPersistentField)
            {
                mmd.setNotPersistent();
            }
            if (transactionalField)
            {
                mmd.setTransactional();
            }

            if (isPersistenceContext())
            {
                if (convertConverterCls != null)
                {
                    TypeManager typeMgr = mmgr.getNucleusContext().getTypeManager();
                    if (typeMgr.getTypeConverterForName(convertConverterCls.getName()) == null)
                    {
                        // Not yet cached an instance of this converter so create one
                        AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter(mmgr.getNucleusContext(), convertConverterCls);
                        Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(convertConverterCls, member.getType());
                        Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(convertConverterCls, attrType, null);

                        // Register the TypeConverter under the name of the AttributeConverter class
                        JDOTypeConverter typeConv = new JDOTypeConverter(conv);
                        typeMgr.registerConverter(convertConverterCls.getName(), typeConv, attrType, dbType, false, null);
                    }

                    mmd.setTypeConverterName(convertConverterCls.getName());
                }
            }

            // Add any embedded info
            if (embeddedMember)
            {
                if (embeddedOwnerField != null || embeddedNullIndicatorColumn != null || embeddedNullIndicatorValue != null || embeddedMembers != null)
                {
                    EmbeddedMetaData embmd = new EmbeddedMetaData();
                    embmd.setOwnerMember(embeddedOwnerField);
                    embmd.setNullIndicatorColumn(embeddedNullIndicatorColumn);
                    embmd.setNullIndicatorValue(embeddedNullIndicatorValue);
                    mmd.setEmbeddedMetaData(embmd);
                    if (embeddedMembers != null && embeddedMembers.length > 0)
                    {
                        for (Persistent embMember : embeddedMembers)
                        {
                            // Add the metadata for the embedded field/property to the embedded metadata
                            String memberName = embMember.name();
                            if (memberName.indexOf('.') > 0)
                            {
                                memberName = memberName.substring(memberName.lastIndexOf('.') + 1);
                            }
                            AbstractMemberMetaData embfmd = getFieldMetaDataForPersistent(embmd, embMember, isMemberOfClassAField(member.getType(), memberName));
                            embmd.addMember(embfmd);
                        }
                    }
                }
            }

            TypeManager typeManager = mmgr.getNucleusContext().getTypeManager();
            ContainerHandler containerHandler = typeManager.getContainerHandler(member.getType());
            ContainerMetaData contmd = null;

            // If the field is a container then add its container element
            if (containerHandler != null)
            {
                contmd = containerHandler.newMetaData();
            }

            if (contmd instanceof CollectionMetaData)
            {
                Class collectionElementType = null;
                StringBuilder elementTypeStr = new StringBuilder();
                if (elementTypes != null && elementTypes.length > 0 && elementTypes[0] != void.class)
                {
                    // User-specified element type(s)
                    for (Class elementType : elementTypes)
                    {
                        if (elementTypeStr.length() > 0)
                        {
                            elementTypeStr.append(',');
                        }
                        elementTypeStr.append(elementType.getName());
                    }
                    collectionElementType = elementTypes[0]; // Use the first only
                }
                else
                {
                    // Try to derive element type from generics info
                    collectionElementType = ClassUtils.getCollectionElementType(member.getType(), member.getGenericType());
                }

                contmd = new CollectionMetaData();
                contmd.setParent(mmd);
                CollectionMetaData collmd = (CollectionMetaData) contmd;

                collmd.setElementType(elementTypeStr.toString());
                if (!StringUtils.isWhitespace(embeddedElement))
                {
                    collmd.setEmbeddedElement(Boolean.valueOf(embeddedElement));
                }
                if (!StringUtils.isWhitespace(serializedElement))
                {
                    collmd.setSerializedElement(Boolean.valueOf(serializedElement));
                }
                if (!StringUtils.isWhitespace(dependentElement))
                {
                    collmd.setDependentElement(Boolean.valueOf(dependentElement));
                }

                if ((embeddedElementMembers != null || "true".equalsIgnoreCase(embeddedElement)) && elemmd == null)
                {
                    elemmd = new ElementMetaData();
                    mmd.setElementMetaData(elemmd);
                }
                if (elemmd != null)
                {
                    if (embeddedElementMembers != null)
                    {
                        // Add any embedded element mappings
                        EmbeddedMetaData embmd = elemmd.getEmbeddedMetaData();
                        if ("true".equalsIgnoreCase(embeddedElement) && elemmd.getEmbeddedMetaData() == null)
                        {
                            // Create EmbeddedMetaData for element since not existing
                            embmd = elemmd.newEmbeddedMetaData();
                        }
                        for (Persistent embeddedElementMember : embeddedElementMembers)
                        {
                            // Add the metadata for the embedded element to the embedded metadata
                            String memberName = embeddedElementMember.name();
                            if (memberName.indexOf('.') > 0)
                            {
                                memberName = memberName.substring(memberName.lastIndexOf('.') + 1);
                            }
                            AbstractMemberMetaData embfmd = getFieldMetaDataForPersistent(embmd, embeddedElementMember, isMemberOfClassAField(collectionElementType, memberName));
                            embmd.addMember(embfmd);
                        }
                    }
                }
            }
            else if (contmd instanceof ArrayMetaData)
            {
                StringBuilder elementTypeStr = new StringBuilder();
                if (elementTypes != null && elementTypes.length > 0 && elementTypes[0] != void.class)
                {
                    // User-specified element type(s)
                    for (Class elementType : elementTypes)
                    {
                        if (elementTypeStr.length() > 0)
                        {
                            elementTypeStr.append(',');
                        }
                        elementTypeStr.append(elementType.getName());
                    }
                }
                else
                {
                    // Derive from component type
                    elementTypeStr.append(member.getType().getComponentType().getName());
                }

                contmd = new ArrayMetaData();
                contmd.setParent(mmd);
                ArrayMetaData arrmd = (ArrayMetaData) contmd;

                arrmd.setElementType(elementTypeStr.toString());
                if (!StringUtils.isWhitespace(embeddedElement))
                {
                    arrmd.setEmbeddedElement(Boolean.valueOf(embeddedElement));
                }
                if (!StringUtils.isWhitespace(serializedElement))
                {
                    arrmd.setSerializedElement(Boolean.valueOf(serializedElement));
                }
                if (!StringUtils.isWhitespace(dependentElement))
                {
                    arrmd.setDependentElement(Boolean.valueOf(dependentElement));
                }
            }
            else if (contmd instanceof MapMetaData)
            {
                Class mapKeyType = null;
                if (keyTypes != null && keyTypes.length > 0 && keyTypes[0] != void.class)
                {
                    // User-specified key type TODO Support multiple keys (interface implementations)
                    mapKeyType = keyTypes[0];
                }
                else
                {
                    // Try to derive key type from generics info
                    mapKeyType = ClassUtils.getMapKeyType(member.getType(), member.getGenericType());
                }

                Class mapValueType = null;
                if (valueTypes != null && valueTypes.length > 0 && valueTypes[0] != void.class)
                {
                    // User-specified value type TODO Support multiple values (interface implementations)
                    mapValueType = valueTypes[0];
                }
                else
                {
                    // Try to derive value type from generics info
                    mapValueType = ClassUtils.getMapValueType(member.getType(), member.getGenericType());
                }

                contmd = new MapMetaData();
                contmd.setParent(mmd);
                MapMetaData mapmd = (MapMetaData) contmd;

                mapmd.setKeyType((mapKeyType != null ? mapKeyType.getName() : null));
                if (!StringUtils.isWhitespace(embeddedKey))
                {
                    mapmd.setEmbeddedKey(Boolean.valueOf(embeddedKey));
                }
                if (!StringUtils.isWhitespace(serializedKey))
                {
                    mapmd.setSerializedKey(Boolean.valueOf(serializedKey));
                }
                if (!StringUtils.isWhitespace(dependentKey))
                {
                    mapmd.setDependentKey(Boolean.valueOf(dependentKey));
                }

                mapmd.setValueType((mapValueType != null ? mapValueType.getName() : null));
                if (!StringUtils.isWhitespace(embeddedValue))
                {
                    mapmd.setEmbeddedValue(Boolean.valueOf(embeddedValue));
                }
                if (!StringUtils.isWhitespace(serializedValue))
                {
                    mapmd.setSerializedValue(Boolean.valueOf(serializedValue));
                }
                if (!StringUtils.isWhitespace(dependentValue))
                {
                    mapmd.setDependentValue(Boolean.valueOf(dependentValue));
                }

                if ((embeddedKeyMembers != null || "true".equalsIgnoreCase(embeddedKey)) && keymd == null)
                {
                    keymd = new KeyMetaData();
                    mmd.setKeyMetaData(keymd);
                }
                if (keymd != null)
                {
                    if (embeddedKeyMembers != null)
                    {
                        // Add any embedded key mappings
                        EmbeddedMetaData embmd = keymd.getEmbeddedMetaData();
                        if ("true".equalsIgnoreCase(embeddedKey) && keymd.getEmbeddedMetaData() == null)
                        {
                            // Create EmbeddedMetaData for key since not existing
                            embmd = keymd.newEmbeddedMetaData();
                        }
                        for (Persistent embeddedKeyMember : embeddedKeyMembers)
                        {
                            // Add the metadata for the embedded key to the embedded metadata
                            String memberName = embeddedKeyMember.name();
                            if (memberName.indexOf('.') > 0)
                            {
                                memberName = memberName.substring(memberName.lastIndexOf('.') + 1);
                            }
                            AbstractMemberMetaData embfmd = getFieldMetaDataForPersistent(embmd, embeddedKeyMember, isMemberOfClassAField(mapKeyType, memberName));
                            embmd.addMember(embfmd);
                        }
                    }
                }

                if ((embeddedKeyMembers != null || "true".equalsIgnoreCase(embeddedKey)) && valuemd == null)
                {
                    valuemd = new ValueMetaData();
                    mmd.setValueMetaData(valuemd);
                }
                if (valuemd != null)
                {
                    if (embeddedValueMembers != null)
                    {
                        // Add any embedded value mappings
                        EmbeddedMetaData embmd = valuemd.getEmbeddedMetaData();
                        if ("true".equalsIgnoreCase(embeddedValue) && valuemd.getEmbeddedMetaData() == null)
                        {
                            // Create EmbeddedMetaData for value since not existing
                            embmd = valuemd.newEmbeddedMetaData();
                        }
                        for (Persistent embeddedValueMember : embeddedValueMembers)
                        {
                            // Add the metadata for the embedded value to the embedded metadata
                            String memberName = embeddedValueMember.name();
                            if (memberName.indexOf('.') > 0)
                            {
                                memberName = memberName.substring(memberName.lastIndexOf('.') + 1);
                            }
                            AbstractMemberMetaData embfmd = getFieldMetaDataForPersistent(embmd, embeddedValueMember, isMemberOfClassAField(mapValueType, memberName));
                            embmd.addMember(embfmd);
                        }
                    }
                }
            }

            if (contmd != null)
            {
                mmd.setContainer(contmd);

                if (elemmd != null)
                {
                    elemmd.setParent(mmd);
                    mmd.setElementMetaData(elemmd);
                    if (elemmd.getMappedBy() != null && mmd.getMappedBy() == null)
                    {
                        // With collection/array this is the same as mapped-by on the field
                        mmd.setMappedBy(elemmd.getMappedBy());
                    }
                }
                if (keymd != null)
                {
                    keymd.setParent(mmd);
                    mmd.setKeyMetaData(keymd);
                }
                if (valuemd != null)
                {
                    valuemd.setParent(mmd);
                    mmd.setValueMetaData(valuemd);
                }
                if (ordermd != null)
                {
                    ordermd.setParent(mmd);
                    mmd.setOrderMetaData(ordermd);
                }
            }
            if (joinmd != null)
            {
                mmd.setJoinMetaData(joinmd);
            }
            if (colmds != null)
            {
                for (ColumnMetaData colmd : colmds)
                {
                    mmd.addColumn(colmd);
                }
            }
            if (idxmd != null)
            {
                mmd.setIndexMetaData(idxmd);
            }
            if (unimd != null)
            {
                mmd.setUniqueMetaData(unimd);
            }
            if (fkmd != null)
            {
                mmd.setForeignKeyMetaData(fkmd);
            }
            if (cacheable != null && cacheable.equalsIgnoreCase("false"))
            {
                mmd.setCacheable(false);
            }
            if (extensions != null)
            {
                mmd.addExtensions(extensions);
            }
        }

        return mmd;
    }

    /**
     * Method to take the passed in outline ClassMetaData and process the annotations for method adding any necessary MetaData to the ClassMetaData.
     * @param cmd The ClassMetaData/InterfaceMetaData (to be updated)
     * @param method The method
     */
    protected void processMethodAnnotations(AbstractClassMetaData cmd, Method method)
    {
        Annotation[] annotations = method.getAnnotations();
        if (annotations != null && annotations.length > 0)
        {
            EventListenerMetaData elmd = cmd.getListenerForClass(cmd.getFullClassName());
            for (Annotation annotation : annotations)
            {
                String annotationTypeName = annotation.annotationType().getName();
                if (annotationTypeName.equals(JDOAnnotationUtils.PRESTORE) ||
                    annotationTypeName.equals(JDOAnnotationUtils.PREDELETE) ||
                    annotationTypeName.equals(JDOAnnotationUtils.PREATTACH) ||
                    annotationTypeName.equals(JDOAnnotationUtils.POSTATTACH) ||
                    annotationTypeName.equals(JDOAnnotationUtils.PREDETACH) ||
                    annotationTypeName.equals(JDOAnnotationUtils.POSTDETACH) ||
                    annotationTypeName.equals(JDOAnnotationUtils.PRECLEAR) ||
                    annotationTypeName.equals(JDOAnnotationUtils.POSTLOAD))
                {
                    if (elmd == null)
                    {
                        // TODO Make use of "order"
                        elmd = new EventListenerMetaData(cmd.getFullClassName());
                        cmd.addListener(elmd);
                    }
                    elmd.addCallback(annotationTypeName, method.getDeclaringClass().getName(), method.getName());
                }
            }
        }
    }

    /**
     * Convenience method to create MetaData for a @Persistent annotation representing a field or property.
     * @param parent Parent MetaData
     * @param member The @Persistent annotation
     * @param isField Whether this is a field (otherwise is a property)
     * @return The metadata for the field/property
     */
    private AbstractMemberMetaData getFieldMetaDataForPersistent(MetaData parent, Persistent member, boolean isField)
    {
        AbstractMemberMetaData fmd = isField ? new FieldMetaData(parent, member.name()) : new PropertyMetaData(parent, member.name());

        FieldPersistenceModifier modifier = JDOAnnotationUtils.getFieldPersistenceModifier(member.persistenceModifier());
        if (modifier != null)
        {
            fmd.setPersistenceModifier(modifier);
        }
        if (!StringUtils.isWhitespace(member.defaultFetchGroup()))
        {
            fmd.setDefaultFetchGroup(Boolean.valueOf(member.defaultFetchGroup()));
        }
        if (!StringUtils.isWhitespace(member.primaryKey()))
        {
            fmd.setPrimaryKey(Boolean.valueOf(member.primaryKey()));
        }
        if (!StringUtils.isWhitespace(member.embedded()))
        {
            fmd.setEmbedded(Boolean.valueOf(member.embedded()));
        }
        if (!StringUtils.isWhitespace(member.serialized()))
        {
            fmd.setSerialised(Boolean.valueOf(member.serialized()));
        }
        if (!StringUtils.isWhitespace(member.dependent()))
        {
            fmd.setDependent(Boolean.valueOf(member.dependent()));
        }

        String nullValue = JDOAnnotationUtils.getNullValueString(member.nullValue());
        fmd.setNullValue(org.datanucleus.metadata.NullValue.getNullValue(nullValue));

        fmd.setMappedBy(member.mappedBy());
        fmd.setColumn(member.column());
        fmd.setTable(member.table());
        fmd.setLoadFetchGroup(member.loadFetchGroup());
        fmd.setCacheable(Boolean.valueOf(member.cacheable()));
        fmd.setRecursionDepth(member.recursionDepth()); // TODO Not applicable when just for a member

        String valueStrategy = JDOAnnotationUtils.getValueGenerationStrategyString(member.valueStrategy());
        if (!StringUtils.isWhitespace(member.customValueStrategy()))
        {
            // User has provided an extension strategy
            valueStrategy = member.customValueStrategy();
        }
        fmd.setValueStrategy(valueStrategy);
        fmd.setSequence(member.sequence());

        String fieldTypeName = null;
        Class[] fieldTypes = member.types();
        if (fieldTypes != null && fieldTypes.length > 0)
        {
            StringBuilder typeStr = new StringBuilder();
            for (Class fieldType : fieldTypes)
            {
                if (typeStr.length() > 0)
                {
                    typeStr.append(',');
                }
                if (fieldType != null && fieldType != void.class)
                {
                    typeStr.append(fieldType.getName());
                }
            }
            fieldTypeName = typeStr.toString();
        }
        fmd.setFieldTypes(fieldTypeName);

        Class converterCls = member.converter();
        if (converterCls.getName().equals(UseDefault.class.getName()))
        {
            converterCls = null;
        }
        if (converterCls != null)
        {
            TypeManager typeMgr = mmgr.getNucleusContext().getTypeManager();
            if (typeMgr.getTypeConverterForName(converterCls.getName()) == null)
            {
                // Not yet cached an instance of this converter so create one
                // TODO We don't know the type of the member, so cannot do this
                /*AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter(mmgr.getNucleusContext(), converterCls);
                if (fieldTypeName != null)
                {
                    Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(converterCls, clr.classForName(fieldTypeName));
                    Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(converterCls, attrType, null);

                    // Register the TypeConverter under the name of the AttributeConverter class
                    JDOTypeConverter typeConv = new JDOTypeConverter(conv);
                    typeMgr.registerConverter(converterCls.getName(), typeConv, attrType, dbType, false, null);
                }*/
            }

            fmd.setTypeConverterName(converterCls.getName());
        }
        if (member.useDefaultConversion())
        {
            fmd.setTypeConverterDisabled();
        }

        // Add any columns defined on the @Persistent
        Column[] columns = member.columns();
        if (columns != null && columns.length > 0)
        {
            for (Column column : columns)
            {
                fmd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(column));
            }
        }

        // Add any extensions defined on the @Persistent
        Extension[] memberExts = member.extensions();
        if (memberExts != null && memberExts.length > 0)
        {
            for (Extension memberExt : memberExts)
            {
                fmd.addExtension(memberExt.key(), memberExt.value());
            }
        }

        return fmd;
    }

    /**
     * Convenience method that tries to find if a specified member name (field or property) is for a field.
     * @param cls The class
     * @param memberName Name of the member
     * @return Whether it is a field (else it's a property).
     */
    private boolean isMemberOfClassAField(Class cls, String memberName)
    {
        try
        {
            cls.getDeclaredField(memberName);
        }
        catch (NoSuchFieldException nsfe)
        {
            return false;
        }
        // TODO It is possible that a memberName is a field AND a property. What do we do then ?
        return true;
    }

    /**
     * Check if a class is persistable, by looking at its annotations.
     * @param annotations Annotations for the class
     * @return The annotationObject for PersistenceCapable
     */
    protected AnnotationObject isClassPersistable(AnnotationObject[] annotations)
    {
        AnnotationObject result = null;
        for (AnnotationObject annotation : annotations)
        {
            String annName = annotation.getName();
            if (annName.equals(JDOAnnotationUtils.PERSISTENCE_CAPABLE))
            {
                if (result == null)
                {
                    result = annotation;
                }
                else
                {
                    // Multiple annotations to be merged
                    result = mergeAnnotation(persistenceCapableDefaults, result, annotation);
                }
            }
        }
        return result;
    }

    /**
     * Merge a duplicated annotation into the original annotation. Iterate all values in the default annotation.
     * If the duplicate annotation has a non-default value, add it to the base value.
     * If both annotations have a non-default value, logs a warning.
     */
    AnnotationObject mergeAnnotation(AnnotationObject defaults, AnnotationObject base, AnnotationObject dup)
    {
        Map<String, Object> baseEntry = base.getNameValueMap();
        Map<String, Object> dupEntry = dup.getNameValueMap();

        for (java.util.Map.Entry<String, Object> entry : defaults.getNameValueMap().entrySet())
        {
            String key = entry.getKey();
            Object defaultValue = entry.getValue();
            Object dupValue = dupEntry.get(key);
            if (!valueEqual(defaultValue, dupValue))
            {
                NucleusLogger.METADATA.warn("Merging duplicated PersistenceCapable annotation : using key=" + key + " with value=" + dupValue + " instead of " + baseEntry.get(key));
                baseEntry.put(key, dupValue);
            }
        }
        return base;
    }

    boolean valueEqual(Object defaultValue, Object dupValue)
    {
        if (defaultValue == null)
        {
            return dupValue == null;
        }
        else if (defaultValue == void.class)
        {
            return dupValue == void.class;
        }
        else if ("".equals(defaultValue))
        {
            return "".equals(dupValue);
        }
        else
        {
            return defaultValue.equals(dupValue);
        }
    }

    /**
     * Check if class is persistence aware, by looking at annotations.
     * @param annotations Annotations for the class
     * @return true if the class has @PersistenceAware
     */
    protected boolean isClassPersistenceAware(AnnotationObject[] annotations)
    {
        for (AnnotationObject annotation : annotations)
        {
            String annName = annotation.getName();
            if (annName.equals(JDOAnnotationUtils.PERSISTENCE_AWARE))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if class has Query annotations (for classes that are not persistable but provide named query definitions.
     * @param annotations Annotations for the class
     * @return true if the class has Named query annotations
     */
    protected boolean doesClassHaveNamedQueries(AnnotationObject[] annotations)
    {
        for (AnnotationObject annotation : annotations)
        {
            String annName = annotation.getName();
            if (annName.equals(JDOAnnotationUtils.QUERIES) || annName.equals(JDOAnnotationUtils.QUERY))
            {
                return true;
            }
        }
        return false;
    }
}
