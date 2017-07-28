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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.AttributeConverter;
import javax.jdo.AttributeConverter.UseDefault;
import javax.jdo.annotations.Column;
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
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.SequenceStrategy;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.VersionStrategy;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOQuery;
import org.datanucleus.api.jdo.JDOTypeConverter;
import org.datanucleus.api.jdo.JDOTypeConverterUtils;
import org.datanucleus.api.jdo.NucleusJDOHelper;
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
import org.datanucleus.metadata.FetchGroupMemberMetaData;
import org.datanucleus.metadata.FetchGroupMetaData;
import org.datanucleus.metadata.FetchPlanMetaData;
import org.datanucleus.metadata.FieldMetaData;
import org.datanucleus.metadata.FieldPersistenceModifier;
import org.datanucleus.metadata.ForeignKeyMetaData;
import org.datanucleus.metadata.IdentityMetaData;
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
import org.datanucleus.store.types.ContainerHandler;
import org.datanucleus.store.types.TypeManager;
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
    }

    /**
     * Method to process the "class" level annotations and create the outline ClassMetaData object. Supports
     * classes annotated with @PersistenceCapable, classes annotated with @PersistenceAware, and classes which
     * have neither of those but have @Queries or @Query.
     * @param pmd Parent PackageMetaData
     * @param cls The class
     * @param annotations Annotations for this class
     * @param clr ClassLoader resolver
     * @return The ClassMetaData/InterfaceMetaData (or null if no annotations)
     */
    protected AbstractClassMetaData processClassAnnotations(PackageMetaData pmd, Class cls, AnnotationObject[] annotations, ClassLoaderResolver clr)
    {
        AbstractClassMetaData cmd = null;

        if (annotations != null && annotations.length > 0)
        {
            AnnotationObject pcAnnotation = isClassPersistable(annotations);
            if (pcAnnotation != null)
            {
                // PersistenceCapable class
                cmd = (cls.isInterface()) ? pmd.newInterfaceMetadata(ClassUtils.getClassNameForClass(cls)) : pmd.newClassMetadata(ClassUtils.getClassNameForClass(cls));
                cmd.setPersistenceModifier(ClassPersistenceModifier.PERSISTENCE_CAPABLE);

                // Process all attributes here in case needed for other annotations
                processPersistenceCapableAnnotation(cls, cmd, pcAnnotation.getNameValueMap());
            }
            else if (isClassPersistenceAware(annotations))
            {
                // PersistenceAware class
                cmd = pmd.newClassMetadata(ClassUtils.getClassNameForClass(cls));
                cmd.setPersistenceModifier(ClassPersistenceModifier.PERSISTENCE_AWARE);
            }
            else if (doesClassHaveNamedQueries(annotations))
            {
                // Class with named query specified
                cmd = pmd.newClassMetadata(ClassUtils.getClassNameForClass(cls));
                cmd.setPersistenceModifier(ClassPersistenceModifier.NON_PERSISTENT);
            }
            else
            {
                // Not involved in the persistence process
                return null;
            }

            processNamedQueries(cmd, cls, annotations);
            if (cmd.getPersistenceModifier() != ClassPersistenceModifier.PERSISTENCE_CAPABLE)
            {
                // Not persistable, so no further information needed
                return cmd;
            }

            // Class is persistable so process remaining annotations
            InheritanceMetaData inhmd = null;
            DiscriminatorMetaData dismd = null;
            IdentityMetaData idmd = null;
            PrimaryKeyMetaData pkmd = null;
            VersionMetaData vermd = null;
            JoinMetaData[] joins = null;
            FetchPlanMetaData[] fetchPlans = null;
            FetchGroupMetaData[] fetchGroups = null;
            SequenceMetaData seqmd = null;
            String cacheable = null;
            boolean embeddedOnly = false;
            ColumnMetaData[] unmappedColumns = null;
            Set<IndexMetaData> indices = null;
            Set<UniqueMetaData> uniqueKeys = null;
            Set<ForeignKeyMetaData> fks = null;
            Map<String, String> extensions = null;

            for (int i = 0; i < annotations.length; i++)
            {
                if (annotations[i] == pcAnnotation)
                {
                    // Already processed above
                    continue;
                }

                Map<String, Object> annotationValues = annotations[i].getNameValueMap();
                String annName = annotations[i].getName();
                if (annName.equals(JDOAnnotationUtils.PERSISTENCE_CAPABLE))
                {
                    processPersistenceCapableAnnotation(cls, cmd, annotationValues);
                }
                else if (annName.equals(JDOAnnotationUtils.EMBEDDED_ONLY))
                {
                    embeddedOnly = true;
                }
                else if (annName.equals(JDOAnnotationUtils.VERSION))
                {
                    VersionStrategy versionStrategy = (VersionStrategy) annotationValues.get("strategy");
                    String strategy = JDOAnnotationUtils.getVersionStrategyString(versionStrategy);
                    String indexed = (String) annotationValues.get("indexed");
                    String column = (String) annotationValues.get("column");
                    Column[] columns = (Column[]) annotationValues.get("columns");
                    vermd = new VersionMetaData();
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
                    idmd = new IdentityMetaData();
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
                    pkmd = new PrimaryKeyMetaData();
                    pkmd.setName(pkName);
                    pkmd.setColumnName(pkColumn);
                    if (columns != null && columns.length > 0)
                    {
                        for (int j = 0; j < columns.length; j++)
                        {
                            pkmd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(columns[j]));
                        }
                    }
                    JDOAnnotationUtils.addExtensionsToMetaData(pkmd, (Extension[]) annotationValues.get("extensions"));
                }
                else if (annName.equals(JDOAnnotationUtils.JOINS))
                {
                    if (joins != null)
                    {
                        NucleusLogger.METADATA.warn(Localiser.msg("044210", cmd.getFullClassName()));
                    }
                    Join[] js = (Join[]) annotationValues.get("value");
                    if (js != null && js.length > 0)
                    {
                        joins = new JoinMetaData[js.length];
                        for (int j = 0; j < js.length; j++)
                        {
                            joins[j] = new JoinMetaData();
                            joins[j].setTable(js[j].table());
                            joins[j].setColumnName(js[j].column());
                            joins[j].setIndexed(IndexedValue.getIndexedValue(js[j].indexed()));
                            joins[j].setOuter(MetaDataUtils.getBooleanForString(js[j].outer(), false));
                            joins[j].setUnique(js[j].unique());
                            joins[j].setDeleteAction(JDOAnnotationUtils.getForeignKeyActionString(js[j].deleteAction()));
                        }
                    }
                }
                else if (annName.equals(JDOAnnotationUtils.JOIN))
                {
                    if (joins != null)
                    {
                        NucleusLogger.METADATA.warn(Localiser.msg("044210", cmd.getFullClassName()));
                    }
                    joins = new JoinMetaData[1];
                    joins[0] = new JoinMetaData();
                    joins[0].setTable((String) annotationValues.get("table"));
                    joins[0].setColumnName((String) annotationValues.get("column"));
                    joins[0].setIndexed(IndexedValue.getIndexedValue((String) annotationValues.get("indexed")));
                    joins[0].setOuter(MetaDataUtils.getBooleanForString((String) annotationValues.get("outer"), false));
                    joins[0].setUnique((String) annotationValues.get("unique"));
                    joins[0].setDeleteAction(((ForeignKeyAction) annotationValues.get("deleteAction")).toString());
                    JDOAnnotationUtils.addExtensionsToMetaData(joins[0], (Extension[]) annotationValues.get("extensions"));
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
                    inhmd = new InheritanceMetaData();
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
                    dismd = new DiscriminatorMetaData();
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
                    if (fetchPlans != null)
                    {
                        NucleusLogger.METADATA.warn(Localiser.msg("044207", cmd.getFullClassName()));
                    }
                    FetchPlan[] plans = (FetchPlan[]) annotationValues.get("value");
                    fetchPlans = new FetchPlanMetaData[plans.length];
                    for (int j = 0; j < plans.length; j++)
                    {
                        fetchPlans[j] = new FetchPlanMetaData(plans[j].name());
                        fetchPlans[j].setMaxFetchDepth(plans[j].maxFetchDepth());
                        fetchPlans[j].setFetchSize(plans[j].fetchSize());
                        int numGroups = plans[j].fetchGroups().length;
                        for (int k = 0; k < numGroups; k++)
                        {
                            FetchGroupMetaData fgmd = new FetchGroupMetaData(plans[j].fetchGroups()[k]);
                            fetchPlans[j].addFetchGroup(fgmd);
                        }
                    }
                }
                else if (annName.equals(JDOAnnotationUtils.FETCHPLAN))
                {
                    if (fetchPlans != null)
                    {
                        NucleusLogger.METADATA.warn(Localiser.msg("044207", cmd.getFullClassName()));
                    }
                    fetchPlans = new FetchPlanMetaData[1];
                    int maxFetchDepth = ((Integer) annotationValues.get("maxFetchDepth")).intValue();
                    int fetchSize = ((Integer) annotationValues.get("fetchSize")).intValue();
                    fetchPlans[0] = new FetchPlanMetaData((String) annotationValues.get("name"));
                    fetchPlans[0].setMaxFetchDepth(maxFetchDepth);
                    fetchPlans[0].setFetchSize(fetchSize);
                }
                else if (annName.equals(JDOAnnotationUtils.FETCHGROUPS))
                {
                    if (fetchGroups != null)
                    {
                        NucleusLogger.METADATA.warn(Localiser.msg("044208", cmd.getFullClassName()));
                    }
                    FetchGroup[] groups = (FetchGroup[]) annotationValues.get("value");
                    fetchGroups = new FetchGroupMetaData[groups.length];
                    for (int j = 0; j < groups.length; j++)
                    {
                        fetchGroups[j] = new FetchGroupMetaData(groups[j].name());
                        if (!StringUtils.isWhitespace(groups[j].postLoad()))
                        {
                            fetchGroups[j].setPostLoad(Boolean.valueOf(groups[j].postLoad()));
                        }
                        int numFields = groups[j].members().length;
                        for (int k = 0; k < numFields; k++)
                        {
                            FetchGroupMemberMetaData fgmmd = new FetchGroupMemberMetaData(fetchGroups[j], groups[j].members()[k].name());
                            fgmmd.setRecursionDepth(groups[j].members()[k].recursionDepth());
                            fetchGroups[j].addMember(fgmmd);
                        }
                        int numGroups = groups[j].fetchGroups().length;
                        for (int k = 0; k < numGroups; k++)
                        {
                            FetchGroupMetaData subgrp = new FetchGroupMetaData(groups[j].fetchGroups()[k]);
                            fetchGroups[j].addFetchGroup(subgrp);
                        }
                    }
                }
                else if (annName.equals(JDOAnnotationUtils.FETCHGROUP))
                {
                    if (fetchGroups != null)
                    {
                        NucleusLogger.METADATA.warn(Localiser.msg("044208", cmd.getFullClassName()));
                    }
                    fetchGroups = new FetchGroupMetaData[1];
                    fetchGroups[0] = new FetchGroupMetaData((String) annotationValues.get("name"));
                    String postLoadStr = (String) annotationValues.get("postLoad");
                    if (!StringUtils.isWhitespace(postLoadStr))
                    {
                        fetchGroups[0].setPostLoad(Boolean.valueOf(postLoadStr));
                    }
                    Persistent[] fields = (Persistent[]) annotationValues.get("members");
                    if (fields != null)
                    {
                        for (int j = 0; j < fields.length; j++)
                        {
                            FetchGroupMemberMetaData fgmmd = new FetchGroupMemberMetaData(fetchGroups[0], fields[j].name());
                            fgmmd.setRecursionDepth(fields[j].recursionDepth());
                            fetchGroups[0].addMember(fgmmd);
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
                    seqmd = new SequenceMetaData(seqName, seqStrategy);
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
                }
                else if (annName.equals(JDOAnnotationUtils.INDICES))
                {
                    // Multiple Indices for the class
                    Index[] values = (Index[]) annotationValues.get("value");
                    if (values != null && values.length > 0)
                    {
                        indices = new HashSet<IndexMetaData>(values.length);
                        for (int j = 0; j < values.length; j++)
                        {
                            IndexMetaData idxmd = JDOAnnotationUtils.getIndexMetaData(values[j].name(), values[j].table(), "" + values[j].unique(),
                                values[j].members(), values[j].columns());
                            if (idxmd.getNumberOfColumns() == 0 && idxmd.getNumberOfMembers() == 0)
                            {
                                NucleusLogger.METADATA.warn(Localiser.msg("044204", cls.getName()));
                            }
                            else
                            {
                                indices.add(idxmd);
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
                        indices = new HashSet<IndexMetaData>(1);
                        indices.add(idxmd);
                    }
                }
                else if (annName.equals(JDOAnnotationUtils.UNIQUES))
                {
                    // Multiple Unique Constraints for the class
                    Unique[] values = (Unique[]) annotationValues.get("value");
                    if (values != null && values.length > 0)
                    {
                        uniqueKeys = new HashSet<UniqueMetaData>(values.length);
                        for (int j = 0; j < values.length; j++)
                        {
                            UniqueMetaData unimd = JDOAnnotationUtils.getUniqueMetaData(values[j].name(), values[j].table(), "" + values[j].deferred(),
                                values[j].members(), values[j].columns());
                            if (unimd.getNumberOfColumns() == 0 && unimd.getNumberOfMembers() == 0)
                            {
                                NucleusLogger.METADATA.warn(Localiser.msg("044205", cls.getName()));
                            }
                            else
                            {
                                uniqueKeys.add(unimd);
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
                        uniqueKeys = new HashSet<UniqueMetaData>(1);
                        uniqueKeys.add(unimd);
                    }
                }
                else if (annName.equals(JDOAnnotationUtils.FOREIGNKEYS))
                {
                    // Multiple FKs for the class
                    ForeignKey[] values = (ForeignKey[]) annotationValues.get("value");
                    if (values != null && values.length > 0)
                    {
                        fks = new HashSet<ForeignKeyMetaData>(values.length);
                        for (int j = 0; j < values.length; j++)
                        {
                            String deleteAction = JDOAnnotationUtils.getForeignKeyActionString(values[j].deleteAction());
                            String updateAction = JDOAnnotationUtils.getForeignKeyActionString(values[j].updateAction());
                            ForeignKeyMetaData fkmd = JDOAnnotationUtils.getFKMetaData(values[j].name(), values[j].table(), values[j].unique(),
                                "" + values[j].deferred(), deleteAction, updateAction, values[j].members(), values[j].columns());
                            if (fkmd.getNumberOfColumns() == 0 && fkmd.getNumberOfMembers() == 0)
                            {
                                NucleusLogger.METADATA.warn(Localiser.msg("044206", cls.getName()));
                            }
                            else
                            {
                                fks.add(fkmd);
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
                        fks = new HashSet<ForeignKeyMetaData>(1);
                        fks.add(fkmd);
                    }
                }
                else if (annName.equals(JDOAnnotationUtils.COLUMNS))
                {
                    // Unmapped column specification
                    Column[] cols = (Column[]) annotationValues.get("value");
                    if (cols != null && cols.length > 0)
                    {
                        unmappedColumns = new ColumnMetaData[cols.length];
                        for (int j = 0; j < cols.length; j++)
                        {
                            unmappedColumns[j] = JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(cols[j]);
                            JDOAnnotationUtils.addExtensionsToMetaData(unmappedColumns[j], cols[j].extensions());
                        }
                    }
                }
                else if (annName.equals(JDOAnnotationUtils.CACHEABLE))
                {
                    String cache = (String) annotationValues.get("value");
                    if (cache != null)
                    {
                        cacheable = cache;
                    }
                }
                else if (annName.equals(JDOAnnotationUtils.EXTENSIONS))
                {
                    Extension[] values = (Extension[]) annotationValues.get("value");
                    if (values != null && values.length > 0)
                    {
                        extensions = new HashMap<String, String>(values.length);
                        for (int j = 0; j < values.length; j++)
                        {
                            String vendorName = values[j].vendorName();
                            if (StringUtils.isWhitespace(vendorName))
                            {
                                throw new InvalidMetaDataException("044160", vendorName, values[j].key().toString(), values[j].value().toString());
                            }
                            else if (vendorName.equalsIgnoreCase(MetaData.VENDOR_NAME))
                            {
                                extensions.put(values[j].key().toString(), values[j].value().toString());
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
                    if (!annName.equals(JDOAnnotationUtils.PERSISTENCE_CAPABLE) && !annName.equals(JDOAnnotationUtils.PERSISTENCE_AWARE) && 
                        !annName.equals(JDOAnnotationUtils.QUERIES) && !annName.equals(JDOAnnotationUtils.QUERY))
                    {
                        NucleusLogger.METADATA.debug(Localiser.msg("044203", cls.getName(), annotations[i].getName()));
                    }
                }
            }

            // Either PersistenceCapable, PersistenceAware or PersistentInterface so build up the metadata
            NucleusLogger.METADATA.debug(Localiser.msg("044200", cls.getName(), "JDO"));

            if (embeddedOnly)
            {
                cmd.setEmbeddedOnly(true);
            }
            if (idmd != null)
            {
                // Datastore identity
                idmd.setParent(cmd);
                cmd.setIdentityMetaData(idmd);
            }
            if (pkmd != null)
            {
                // Primary Key
                pkmd.setParent(cmd);
                cmd.setPrimaryKeyMetaData(pkmd);
            }

            if (vermd != null)
            {
                // Version
                vermd.setParent(cmd);
                cmd.setVersionMetaData(vermd);
            }

            if (inhmd != null)
            {
                // Inheritance
                if (dismd != null)
                {
                    inhmd.setDiscriminatorMetaData(dismd);
                }
                inhmd.setParent(cmd);
                cmd.setInheritanceMetaData(inhmd);
            }
            else if (dismd != null)
            {
                inhmd = new InheritanceMetaData();
                inhmd.setDiscriminatorMetaData(dismd);
                cmd.setInheritanceMetaData(inhmd);
            }

            if (joins != null && joins.length > 0)
            {
                // Joins
                for (int i = 0; i < joins.length; i++)
                {
                    cmd.addJoin(joins[i]);
                }
            }
            if (fetchGroups != null && fetchGroups.length > 0)
            {
                // Fetch Groups
                for (int i = 0; i < fetchGroups.length; i++)
                {
                    fetchGroups[i].setParent(cmd);
                    cmd.addFetchGroup(fetchGroups[i]);
                }
            }

            if (seqmd != null)
            {
                // Sequence - currently only allowing 1 per class (should really be on the package)
                cmd.getPackageMetaData().addSequence(seqmd);
            }

            if (indices != null)
            {
                Iterator iter = indices.iterator();
                while (iter.hasNext())
                {
                    IndexMetaData idxmd = (IndexMetaData) iter.next();
                    idxmd.setParent(cmd);
                    cmd.addIndex(idxmd);
                }
            }
            if (uniqueKeys != null)
            {
                Iterator iter = uniqueKeys.iterator();
                while (iter.hasNext())
                {
                    UniqueMetaData unimd = (UniqueMetaData) iter.next();
                    unimd.setParent(cmd);
                    cmd.addUniqueConstraint(unimd);
                }
            }
            if (fks != null)
            {
                Iterator iter = fks.iterator();
                while (iter.hasNext())
                {
                    ForeignKeyMetaData fkmd = (ForeignKeyMetaData) iter.next();
                    fkmd.setParent(cmd);
                    cmd.addForeignKey(fkmd);
                }
            }
            if (unmappedColumns != null)
            {
                for (int i = 0; i < unmappedColumns.length; i++)
                {
                    ColumnMetaData colmd = unmappedColumns[i];
                    colmd.setParent(cmd);
                    cmd.addUnmappedColumn(colmd);
                }
            }
            if (cacheable != null && cacheable.equalsIgnoreCase("false"))
            {
                cmd.setCacheable(false);
            }
            if (extensions != null)
            {
                cmd.addExtensions(extensions);
            }
        }

        return cmd;
    }

    private void processPersistenceCapableAnnotation(Class cls, AbstractClassMetaData cmd, Map<String, Object> annotationValues)
    {
        String identityType = JDOAnnotationUtils.getIdentityTypeString((javax.jdo.annotations.IdentityType) annotationValues.get("identityType"));
        cmd.setIdentityType(IdentityType.getIdentityType(identityType));

        Class idClass = (Class) annotationValues.get("objectIdClass");
        if (idClass != null && idClass != void.class)
        {
            cmd.setObjectIdClass(NucleusJDOHelper.getObjectIdClassForInputIdClass(idClass.getName()));
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
            for (int j = 0; j < members.length; j++)
            {
                String memberName = members[j].name();
                if (memberName.indexOf('.') > 0)
                {
                    memberName = memberName.substring(memberName.lastIndexOf('.') + 1);
                }
                boolean isField = isMemberOfClassAField(cls, memberName);
                AbstractMemberMetaData fmd = getFieldMetaDataForPersistent(cmd, members[j], isField);
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
        QueryMetaData[] queries = null;

        for (int i = 0; i < annotations.length; i++)
        {
            Map<String, Object> annotationValues = annotations[i].getNameValueMap();
            String annName = annotations[i].getName();

            if (annName.equals(JDOAnnotationUtils.QUERIES))
            {
                if (queries != null)
                {
                    NucleusLogger.METADATA.warn(Localiser.msg("044209", cmd.getFullClassName()));
                }
                Query[] qs = (Query[]) annotationValues.get("value");
                queries = new QueryMetaData[qs.length];
                for (int j = 0; j < queries.length; j++)
                {
                    String lang = JDOAnnotationUtils.getQueryLanguageName(qs[j].language());
                    if (!StringUtils.isWhitespace(lang))
                    {
                        if (lang.equals(JDOQuery.JDOQL_QUERY_LANGUAGE)) // Convert to JDOQL
                        {
                            lang = QueryLanguage.JDOQL.toString();
                        }
                        else if (lang.equals(JDOQuery.SQL_QUERY_LANGUAGE)) // Convert to SQL
                        {
                            lang = QueryLanguage.SQL.toString();
                        }
                        else if (lang.equals(JDOQuery.JPQL_QUERY_LANGUAGE)) // Convert to JPQL
                        {
                            lang = QueryLanguage.JPQL.toString();
                        }
                    }
                    String resultClassName = (qs[j].resultClass() != null && qs[j].resultClass() != void.class ? qs[j].resultClass().getName() : null);
                    if (StringUtils.isWhitespace(qs[j].name()))
                    {
                        throw new InvalidClassMetaDataException("044154", cmd.getFullClassName());
                    }
                    queries[j] = new QueryMetaData(qs[j].name());
                    queries[j].setScope(cls.getName());
                    queries[j].setLanguage(lang);
                    queries[j].setUnmodifiable(qs[j].unmodifiable());
                    queries[j].setResultClass(resultClassName);
                    queries[j].setUnique(qs[j].unique());
                    queries[j].setFetchPlanName(qs[j].fetchPlan());
                    queries[j].setQuery(qs[j].value());
                    JDOAnnotationUtils.addExtensionsToMetaData(queries[j], qs[j].extensions());
                }
            }
            else if (annName.equals(JDOAnnotationUtils.QUERY))
            {
                if (queries != null)
                {
                    NucleusLogger.METADATA.warn(Localiser.msg("044209", cmd.getFullClassName()));
                }
                queries = new QueryMetaData[1];
                String unmodifiable = "" + annotationValues.get("unmodifiable");
                Class resultClassValue = (Class) annotationValues.get("resultClass");
                String resultClassName = (resultClassValue != null && resultClassValue != void.class ? resultClassValue.getName() : null);
                String lang = JDOAnnotationUtils.getQueryLanguageName((String) annotationValues.get("language"));
                if (!StringUtils.isWhitespace(lang))
                {
                    if (lang.equals(JDOQuery.JDOQL_QUERY_LANGUAGE)) // Convert to JDOQL
                    {
                        lang = QueryLanguage.JDOQL.toString();
                    }
                    else if (lang.equals(JDOQuery.SQL_QUERY_LANGUAGE)) // Convert to SQL
                    {
                        lang = QueryLanguage.SQL.toString();
                    }
                    else if (lang.equals(JDOQuery.JPQL_QUERY_LANGUAGE)) // Convert to JPQL
                    {
                        lang = QueryLanguage.JPQL.toString();
                    }
                }
                if (StringUtils.isWhitespace((String) annotationValues.get("name")))
                {
                    throw new InvalidClassMetaDataException("044154", cmd.getFullClassName());
                }
                queries[0] = new QueryMetaData((String) annotationValues.get("name"));
                queries[0].setScope(cls.getName());
                queries[0].setLanguage(lang);
                queries[0].setUnmodifiable(unmodifiable);
                queries[0].setResultClass(resultClassName);
                queries[0].setUnique((String) annotationValues.get("unique"));
                queries[0].setFetchPlanName((String) annotationValues.get("fetchPlan"));
                queries[0].setQuery((String) annotationValues.get("value"));
                JDOAnnotationUtils.addExtensionsToMetaData(queries[0], (Extension[]) annotationValues.get("extensions"));
            }
        }

        if (queries != null && queries.length > 0)
        {
            // Named Queries
            for (int i = 0; i < queries.length; i++)
            {
                queries[i].setParent(cmd);
                cmd.addQuery(queries[i]);
            }
        }
    }

    /**
     * Convenience method to process the annotations for a field/property. The passed annotations may have
     * been specified on the field or on the getter methods.
     * @param cmd The ClassMetaData/InterfaceMetaData to update
     * @param member The field/property
     * @param annotations Annotations for the field/property
     * @param propertyAccessor if there are properties for this class
     * @return The FieldMetaData/PropertyMetaData that was added (if any)
     */
    protected AbstractMemberMetaData processMemberAnnotations(AbstractClassMetaData cmd, Member member, AnnotationObject[] annotations, boolean propertyAccessor)
    {
        AbstractMemberMetaData mmd = null;
        if (annotations != null && annotations.length > 0)
        {
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

            for (int i = 0; i < annotations.length; i++)
            {
                String annName = annotations[i].getName();
                Map<String, Object> annotationValues = annotations[i].getNameValueMap();
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
                    String valueStrategy = 
                            JDOAnnotationUtils.getValueGenerationStrategyString((IdGeneratorStrategy) annotationValues.get("valueStrategy"));
                    String customValueStrategy = (String) annotationValues.get("customValueStrategy");
                    if (!StringUtils.isWhitespace(customValueStrategy))
                    {
                        // User has provided an extension strategy
                        valueStrategy = customValueStrategy;
                    }
                    FieldPersistenceModifier modifier = 
                            JDOAnnotationUtils.getFieldPersistenceModifier((PersistenceModifier) annotationValues.get("persistenceModifier"));
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
                        for (int j = 0; j < fieldTypes.length; j++)
                        {
                            if (typeStr.length() > 0)
                            {
                                typeStr.append(',');
                            }
                            if (fieldTypes[j] != null && fieldTypes[j] != void.class)
                            {
                                typeStr.append(fieldTypes[j].getName());
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

                    if (member.isProperty())
                    {
                        // Property
                        mmd = new PropertyMetaData(cmd, member.getName());
                    }
                    else
                    {
                        // Field
                        mmd = new FieldMetaData(cmd, member.getName());
                    }

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
                                AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter((PersistenceNucleusContext) mmgr.getNucleusContext(), converterCls);
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
                    mmd.setRecursionDepth(recursionDepth);
                    mmd.setLoadFetchGroup(loadFetchGroup);
                    mmd.setValueStrategy(valueStrategy);
                    mmd.setSequence(sequence);
                    mmd.setFieldTypes(fieldTypeName);

                    // Add any columns defined on the @Persistent
                    Column[] columns = (Column[]) annotationValues.get("columns");
                    if (columns != null && columns.length > 0)
                    {
                        for (int j = 0; j < columns.length; j++)
                        {
                            mmd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(columns[j]));
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
                        for (int j = 0; j < joinColumns.length; j++)
                        {
                            joinmd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(joinColumns[j]));
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
                    String elementTable = (String) annotationValues.get("table");
                    String elementColumn = (String) annotationValues.get("column");
                    String elementDeleteAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("deleteAction"));
                    String elementUpdateAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("updateAction"));
                    String elementMappedBy = (String) annotationValues.get("mappedBy");
                    Column[] elementColumns = (Column[]) annotationValues.get("columns");
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
                    elemmd.setTable(elementTable);
                    elemmd.setColumnName(elementColumn);
                    elemmd.setDeleteAction(elementDeleteAction);
                    elemmd.setUpdateAction(elementUpdateAction);
                    elemmd.setIndexed(IndexedValue.getIndexedValue(indexed));
                    elemmd.setUnique(MetaDataUtils.getBooleanForString(unique, false));
                    elemmd.setMappedBy(elementMappedBy);
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
                    if (elementColumns != null && elementColumns.length > 0)
                    {
                        for (int j = 0; j < elementColumns.length; j++)
                        {
                            elemmd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(elementColumns[j]));
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
                                AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter((PersistenceNucleusContext) mmgr.getNucleusContext(), converterCls);
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
                                DiscriminatorMetaData dismd = embmd.newDiscriminatorMetadata();
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
                    String keyTable = (String) annotationValues.get("table");
                    String keyColumn = (String) annotationValues.get("column");
                    String keyDeleteAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("deleteAction"));
                    String keyUpdateAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("updateAction"));
                    String keyMappedBy = (String) annotationValues.get("mappedBy");
                    Column[] keyColumns = (Column[]) annotationValues.get("columns");
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
                    keymd.setTable(keyTable);
                    keymd.setColumnName(keyColumn);
                    keymd.setDeleteAction(keyDeleteAction);
                    keymd.setUpdateAction(keyUpdateAction);
                    keymd.setIndexed(IndexedValue.getIndexedValue(indexed));
                    keymd.setUnique(MetaDataUtils.getBooleanForString(unique, false));
                    keymd.setMappedBy(keyMappedBy);
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
                    if (keyColumns != null && keyColumns.length > 0)
                    {
                        for (int j = 0; j < keyColumns.length; j++)
                        {
                            keymd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(keyColumns[j]));
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
                                AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter((PersistenceNucleusContext) mmgr.getNucleusContext(), converterCls);
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
                        // Delay addition of embeddedKeyMembers til completion of this loop so we have the key
                        // type
                    }
                }
                else if (annName.equals(JDOAnnotationUtils.VALUE))
                {
                    // Value of a Map
                    valueTypes = (Class[]) annotationValues.get("types");
                    embeddedValue = (String) annotationValues.get("embedded");
                    serializedValue = (String) annotationValues.get("serialized");
                    dependentValue = (String) annotationValues.get("dependent");
                    String valueTable = (String) annotationValues.get("table");
                    String valueColumn = (String) annotationValues.get("column");
                    String valueDeleteAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("deleteAction"));
                    String valueUpdateAction = JDOAnnotationUtils.getForeignKeyActionString((ForeignKeyAction) annotationValues.get("updateAction"));
                    String valueMappedBy = (String) annotationValues.get("mappedBy");
                    Column[] valueColumns = (Column[]) annotationValues.get("columns");
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
                    valuemd.setTable(valueTable);
                    valuemd.setColumnName(valueColumn);
                    valuemd.setDeleteAction(valueDeleteAction);
                    valuemd.setUpdateAction(valueUpdateAction);
                    valuemd.setIndexed(IndexedValue.getIndexedValue(indexed));
                    valuemd.setUnique(MetaDataUtils.getBooleanForString(unique, false));
                    valuemd.setMappedBy(valueMappedBy);
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
                    if (valueColumns != null && valueColumns.length > 0)
                    {
                        for (int j = 0; j < valueColumns.length; j++)
                        {
                            valuemd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(valueColumns[j]));
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
                                AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter((PersistenceNucleusContext) mmgr.getNucleusContext(), converterCls);
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
                    String orderColumn = (String) annotationValues.get("column");
                    String orderMappedBy = (String) annotationValues.get("mappedBy");
                    Column[] orderColumns = (Column[]) annotationValues.get("columns");
                    ordermd = new OrderMetaData();
                    ordermd.setColumnName(orderColumn);
                    ordermd.setMappedBy(orderMappedBy);
                    if (orderColumns != null && orderColumns.length > 0)
                    {
                        for (int j = 0; j < orderColumns.length; j++)
                        {
                            ordermd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(orderColumns[j]));
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
                }
                else if (annName.equals(JDOAnnotationUtils.EXTENSIONS))
                {
                    Extension[] values = (Extension[]) annotationValues.get("value");
                    if (values != null && values.length > 0)
                    {
                        extensions = new HashMap<String,String>(values.length);
                        for (int j = 0; j < values.length; j++)
                        {
                            String vendorName = values[j].vendorName();
                            if (StringUtils.isWhitespace(vendorName))
                            {
                                throw new InvalidMetaDataException("044160", vendorName, values[j].key().toString(), values[j].value().toString());
                            }
                            else if (vendorName.equalsIgnoreCase(MetaData.VENDOR_NAME))
                            {
                                extensions.put(values[j].key().toString(), values[j].value().toString());
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
                    NucleusLogger.METADATA.debug(Localiser.msg("044211", cmd.getFullClassName(), member.getName(), annotations[i].getName()));
                }
            }

            if (mmd == null && (transactionalField || nonPersistentField || primaryKey || colmds != null || serialised || embeddedOwnerField != null || 
                embeddedNullIndicatorColumn != null || embeddedNullIndicatorValue != null || embeddedMembers != null || elemmd != null || keymd != null || valuemd != null || 
                ordermd != null || idxmd != null || unimd != null || fkmd != null || joinmd != null || extensions != null || convertConverterCls != null))
            {
                // @Persistent not supplied but other relevant annotations defined, so add default metadata
                // element
                if (member.isProperty())
                {
                    mmd = new PropertyMetaData(cmd, member.getName());
                }
                else
                {
                    mmd = new FieldMetaData(cmd, member.getName());
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
                            AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter((PersistenceNucleusContext) mmgr.getNucleusContext(), convertConverterCls);
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
                            for (int j = 0; j < embeddedMembers.length; j++)
                            {
                                // Add the metadata for the embedded field/property to the embedded metadata
                                String memberName = embeddedMembers[j].name();
                                if (memberName.indexOf('.') > 0)
                                {
                                    memberName = memberName.substring(memberName.lastIndexOf('.') + 1);
                                }
                                AbstractMemberMetaData embfmd = getFieldMetaDataForPersistent(embmd, embeddedMembers[j],
                                    isMemberOfClassAField(member.getType(), memberName));
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
                        for (int j = 0; j < elementTypes.length; j++)
                        {
                            if (elementTypeStr.length() > 0)
                            {
                                elementTypeStr.append(',');
                            }
                            elementTypeStr.append(elementTypes[j].getName());
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
                            for (int j = 0; j < embeddedElementMembers.length; j++)
                            {
                                // Add the metadata for the embedded element to the embedded metadata
                                String memberName = embeddedElementMembers[j].name();
                                if (memberName.indexOf('.') > 0)
                                {
                                    memberName = memberName.substring(memberName.lastIndexOf('.') + 1);
                                }
                                AbstractMemberMetaData embfmd = getFieldMetaDataForPersistent(embmd, embeddedElementMembers[j],
                                    isMemberOfClassAField(collectionElementType, memberName));
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
                        for (int j = 0; j < elementTypes.length; j++)
                        {
                            if (elementTypeStr.length() > 0)
                            {
                                elementTypeStr.append(',');
                            }
                            elementTypeStr.append(elementTypes[j].getName());
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
                            for (int j = 0; j < embeddedKeyMembers.length; j++)
                            {
                                // Add the metadata for the embedded key to the embedded metadata
                                String memberName = embeddedKeyMembers[j].name();
                                if (memberName.indexOf('.') > 0)
                                {
                                    memberName = memberName.substring(memberName.lastIndexOf('.') + 1);
                                }
                                AbstractMemberMetaData embfmd = getFieldMetaDataForPersistent(embmd, embeddedKeyMembers[j],
                                    isMemberOfClassAField(mapKeyType, memberName));
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
                            for (int j = 0; j < embeddedValueMembers.length; j++)
                            {
                                // Add the metadata for the embedded value to the embedded metadata
                                String memberName = embeddedValueMembers[j].name();
                                if (memberName.indexOf('.') > 0)
                                {
                                    memberName = memberName.substring(memberName.lastIndexOf('.') + 1);
                                }
                                AbstractMemberMetaData embfmd = getFieldMetaDataForPersistent(embmd, embeddedValueMembers[j],
                                    isMemberOfClassAField(mapValueType, memberName));
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
                    for (int i = 0; i < colmds.length; i++)
                    {
                        mmd.addColumn(colmds[i]);
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
        }

        return mmd;
    }

    /**
     * Method to take the passed in outline ClassMetaData and process the annotations for method adding any
     * necessary MetaData to the ClassMetaData.
     * @param cmd The ClassMetaData/InterfaceMetaData (to be updated)
     * @param method The method
     */
    protected void processMethodAnnotations(AbstractClassMetaData cmd, Method method)
    {
        // do nothing
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
        FieldPersistenceModifier modifier = JDOAnnotationUtils.getFieldPersistenceModifier(member.persistenceModifier());
        String nullValue = JDOAnnotationUtils.getNullValueString(member.nullValue());
        String valueStrategy = JDOAnnotationUtils.getValueGenerationStrategyString(member.valueStrategy());

        String fieldTypeName = null;
        Class[] fieldTypes = member.types();
        if (fieldTypes != null && fieldTypes.length > 0)
        {
            StringBuilder typeStr = new StringBuilder();
            for (int j = 0; j < fieldTypes.length; j++)
            {
                if (typeStr.length() > 0)
                {
                    typeStr.append(',');
                }
                if (fieldTypes[j] != null && fieldTypes[j] != void.class)
                {
                    typeStr.append(fieldTypes[j].getName());
                }
            }
            fieldTypeName = typeStr.toString();
        }

        AbstractMemberMetaData fmd = null;
        if (isField)
        {
            fmd = new FieldMetaData(parent, member.name());
        }
        else
        {
            fmd = new PropertyMetaData(parent, member.name());
        }
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
        fmd.setNullValue(org.datanucleus.metadata.NullValue.getNullValue(nullValue));
        fmd.setMappedBy(member.mappedBy());
        fmd.setColumn(member.column());
        fmd.setTable(member.table());
        fmd.setLoadFetchGroup(member.loadFetchGroup());
        fmd.setValueStrategy(valueStrategy);
        fmd.setSequence(member.sequence());
        fmd.setFieldTypes(fieldTypeName);

        // Add any columns defined on the @Persistent
        Column[] columns = member.columns();
        if (columns != null && columns.length > 0)
        {
            for (int j = 0; j < columns.length; j++)
            {
                fmd.addColumn(JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(columns[j]));
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
        for (int i = 0; i < annotations.length; i++)
        {
            String annClassName = annotations[i].getName();
            if (annClassName.equals(JDOAnnotationUtils.PERSISTENCE_CAPABLE))
            {
                return annotations[i];
            }
        }
        return null;
    }

    /**
     * Check if class is persistence aware, by looking at annotations.
     * @param annotations Annotations for the class
     * @return true if the class has @PersistenceAware
     */
    protected boolean isClassPersistenceAware(AnnotationObject[] annotations)
    {
        for (int i = 0; i < annotations.length; i++)
        {
            String annName = annotations[i].getName();
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
        for (int i = 0; i < annotations.length; i++)
        {
            String annClassName = annotations[i].getName();
            if (annClassName.equals(JDOAnnotationUtils.QUERIES) || annClassName.equals(JDOAnnotationUtils.QUERY))
            {
                return true;
            }
        }
        return false;
    }
}
