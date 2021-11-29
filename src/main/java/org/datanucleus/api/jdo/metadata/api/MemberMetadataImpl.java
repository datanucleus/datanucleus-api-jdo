/**********************************************************************
Copyright (c) 2009 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.api.jdo.metadata.api;

import javax.jdo.AttributeConverter;
import javax.jdo.annotations.ForeignKeyAction;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.metadata.ArrayMetadata;
import javax.jdo.metadata.CollectionMetadata;
import javax.jdo.metadata.ColumnMetadata;
import javax.jdo.metadata.ElementMetadata;
import javax.jdo.metadata.EmbeddedMetadata;
import javax.jdo.metadata.ForeignKeyMetadata;
import javax.jdo.metadata.IndexMetadata;
import javax.jdo.metadata.JoinMetadata;
import javax.jdo.metadata.KeyMetadata;
import javax.jdo.metadata.MapMetadata;
import javax.jdo.metadata.MemberMetadata;
import javax.jdo.metadata.OrderMetadata;
import javax.jdo.metadata.UniqueMetadata;
import javax.jdo.metadata.ValueMetadata;

import org.datanucleus.api.jdo.JDOTypeConverter;
import org.datanucleus.api.jdo.JDOTypeConverterUtils;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.ArrayMetaData;
import org.datanucleus.metadata.CollectionMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.ElementMetaData;
import org.datanucleus.metadata.EmbeddedMetaData;
import org.datanucleus.metadata.FieldPersistenceModifier;
import org.datanucleus.metadata.ForeignKeyMetaData;
import org.datanucleus.metadata.ValueGenerationStrategy;
import org.datanucleus.metadata.IndexMetaData;
import org.datanucleus.metadata.IndexedValue;
import org.datanucleus.metadata.JoinMetaData;
import org.datanucleus.metadata.KeyMetaData;
import org.datanucleus.metadata.MapMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.metadata.OrderMetaData;
import org.datanucleus.metadata.UniqueMetaData;
import org.datanucleus.metadata.ValueMetaData;
import org.datanucleus.store.types.TypeManager;

/**
 * Convenience implementation of MemberMetadata for use by FieldMetadataImpl/PropertyMetadataImpl
 */
public class MemberMetadataImpl extends AbstractMetadataImpl implements MemberMetadata
{
    public MemberMetadataImpl(MetaData internal)
    {
        super(internal);
    }

    public AbstractMemberMetaData getInternal()
    {
        return (AbstractMemberMetaData)internalMD;
    }

    public ArrayMetadata getArrayMetadata()
    {
        ArrayMetaData internalArrmd = getInternal().getArray();
        if (internalArrmd == null)
        {
            return null;
        }
        ArrayMetadataImpl arrmd = new ArrayMetadataImpl(internalArrmd);
        arrmd.parent = this;
        return arrmd;
    }

    public boolean getCacheable()
    {
        return getInternal().isCacheable();
    }

    public CollectionMetadata getCollectionMetadata()
    {
        CollectionMetaData internalCollmd = getInternal().getCollection();
        if (internalCollmd == null)
        {
            return null;
        }
        CollectionMetadataImpl collmd = new CollectionMetadataImpl(internalCollmd);
        collmd.parent = this;
        return collmd;
    }

    public String getColumn()
    {
        ColumnMetaData[] colmds = getInternal().getColumnMetaData();
        if (colmds != null && colmds.length > 0)
        {
            return colmds[0].getName();
        }
        return null;
    }

    public String getCustomStrategy()
    {
        ValueGenerationStrategy strategy = getInternal().getValueStrategy();
        if (strategy != ValueGenerationStrategy.IDENTITY &&
            strategy != ValueGenerationStrategy.INCREMENT &&
            strategy != ValueGenerationStrategy.NATIVE &&
            strategy != ValueGenerationStrategy.SEQUENCE &&
            strategy != ValueGenerationStrategy.UUIDHEX &&
            strategy != ValueGenerationStrategy.UUIDSTRING && 
            strategy != null)
        {
            return strategy.toString();
        }
        return null;
    }

    public Boolean getDefaultFetchGroup()
    {
        return getInternal().isDefaultFetchGroup();
    }

    public ForeignKeyAction getDeleteAction()
    {
        ForeignKeyMetaData fkmd = getInternal().getForeignKeyMetaData();
        if (fkmd != null)
        {
            org.datanucleus.metadata.ForeignKeyAction fk = fkmd.getDeleteAction();
            if (fk == org.datanucleus.metadata.ForeignKeyAction.CASCADE)
            {
                return ForeignKeyAction.CASCADE;
            }
            else if (fk == org.datanucleus.metadata.ForeignKeyAction.DEFAULT)
            {
                return ForeignKeyAction.DEFAULT;
            }
            else if (fk == org.datanucleus.metadata.ForeignKeyAction.NONE)
            {
                return ForeignKeyAction.NONE;
            }
            else if (fk == org.datanucleus.metadata.ForeignKeyAction.NULL)
            {
                return ForeignKeyAction.NULL;
            }
            else if (fk == org.datanucleus.metadata.ForeignKeyAction.RESTRICT)
            {
                return ForeignKeyAction.RESTRICT;
            }
        }
        return ForeignKeyAction.UNSPECIFIED;
    }

    public Boolean getDependent()
    {
        return getInternal().isDependent();
    }

    public ElementMetadata getElementMetadata()
    {
        ElementMetaData internalElemmd = getInternal().getElementMetaData();
        if (internalElemmd == null)
        {
            return null;
        }
        ElementMetadataImpl elemmd = new ElementMetadataImpl(internalElemmd);
        elemmd.parent = this;
        return elemmd;
    }

    public Boolean getEmbedded()
    {
        return getInternal().isEmbedded();
    }

    public EmbeddedMetadata getEmbeddedMetadata()
    {
        EmbeddedMetaData internalEmbmd = getInternal().getEmbeddedMetaData();
        EmbeddedMetadataImpl embmd = new EmbeddedMetadataImpl(internalEmbmd);
        embmd.parent = this;
        return embmd;
    }

    public OrderMetadata getOrderMetadata()
    {
        OrderMetaData internalOrdmd = getInternal().getOrderMetaData();
        OrderMetadataImpl ordmd = new OrderMetadataImpl(internalOrdmd);
        ordmd.parent = this;
        return ordmd;
    }

    public String getFieldType()
    {
        return getInternal().getTypeName();
    }

    public ForeignKeyMetadata getForeignKeyMetadata()
    {
        ForeignKeyMetaData internalFkmd = getInternal().getForeignKeyMetaData();
        if (internalFkmd == null)
        {
            return null;
        }
        ForeignKeyMetadataImpl fkmd = new ForeignKeyMetadataImpl(internalFkmd);
        fkmd.parent = this;
        return fkmd;
    }

    public IndexMetadata getIndexMetadata()
    {
        IndexMetaData internalIdxmd = getInternal().getIndexMetaData();
        if (internalIdxmd == null)
        {
            return null;
        }
        IndexMetadataImpl idxmd = new IndexMetadataImpl(internalIdxmd);
        idxmd.parent = this;
        return idxmd;
    }

    public Boolean getIndexed()
    {
        IndexedValue val = getInternal().getIndexed();
        if (val == IndexedValue.TRUE)
        {
            return true;
        }
        else if (val == IndexedValue.FALSE)
        {
            return false;
        }
        return null;
    }

    public JoinMetadata getJoinMetadata()
    {
        JoinMetaData internalJoinmd = getInternal().getJoinMetaData();
        if (internalJoinmd == null)
        {
            return null;
        }
        JoinMetadataImpl joinmd = new JoinMetadataImpl(internalJoinmd);
        joinmd.parent = this;
        return joinmd;
    }

    public KeyMetadata getKeyMetadata()
    {
        KeyMetaData internalKeymd = getInternal().getKeyMetaData();
        if (internalKeymd == null)
        {
            return null;
        }
        KeyMetadataImpl keymd = new KeyMetadataImpl(internalKeymd);
        keymd.parent = this;
        return keymd;
    }

    public String getLoadFetchGroup()
    {
        return getInternal().getLoadFetchGroup();
    }

    public MapMetadata getMapMetadata()
    {
        MapMetaData internalMapmd = getInternal().getMap();
        if (internalMapmd == null)
        {
            return null;
        }
        MapMetadataImpl mapmd = new MapMetadataImpl(internalMapmd);
        mapmd.parent = this;
        return mapmd;
    }

    public String getMappedBy()
    {
        return getInternal().getMappedBy();
    }

    public String getName()
    {
        return getInternal().getName();
    }

    public NullValue getNullValue()
    {
        org.datanucleus.metadata.NullValue val = getInternal().getNullValue();
        if (val == null)
        {
            return null;
        }
        if (val == org.datanucleus.metadata.NullValue.DEFAULT)
        {
            return NullValue.DEFAULT;
        }
        else if (val == org.datanucleus.metadata.NullValue.EXCEPTION)
        {
            return NullValue.EXCEPTION;
        }
        else if (val == org.datanucleus.metadata.NullValue.NONE)
        {
            return NullValue.NONE;
        }
        return null;
    }

    public PersistenceModifier getPersistenceModifier()
    {
        FieldPersistenceModifier mod = getInternal().getPersistenceModifier();
        if (mod == FieldPersistenceModifier.NONE)
        {
            return PersistenceModifier.NONE;
        }
        else if (mod == FieldPersistenceModifier.TRANSACTIONAL)
        {
            return PersistenceModifier.TRANSACTIONAL;
        }
        else if (mod == FieldPersistenceModifier.PERSISTENT)
        {
            return PersistenceModifier.PERSISTENT;
        }
        return PersistenceModifier.UNSPECIFIED;
    }

    public boolean getPrimaryKey()
    {
        return getInternal().isPrimaryKey();
    }

    public int getRecursionDepth()
    {
        Integer recDepth = getInternal().getRecursionDepth();
        return (recDepth == null) ? 1 : recDepth;
    }

    public String getSequence()
    {
        return getInternal().getSequence();
    }

    public Boolean getSerialized()
    {
        return getInternal().isSerialized();
    }

    public String getTable()
    {
        return getInternal().getTable();
    }

    public Boolean getUnique()
    {
        return getInternal().isUnique();
    }

    public UniqueMetadata getUniqueMetadata()
    {
        UniqueMetaData internalUnimd = getInternal().getUniqueMetaData();
        if (internalUnimd == null)
        {
            return null;
        }
        UniqueMetadataImpl unimd = new UniqueMetadataImpl(internalUnimd);
        unimd.parent = this;
        return unimd;
    }

    public ValueMetadata getValueMetadata()
    {
        ValueMetaData internalValmd = getInternal().getValueMetaData();
        if (internalValmd == null)
        {
            return null;
        }
        ValueMetadataImpl valmd = new ValueMetadataImpl(internalValmd);
        valmd.parent = this;
        return valmd;
    }

    public IdGeneratorStrategy getValueStrategy()
    {
        ValueGenerationStrategy strategy = getInternal().getValueStrategy();
        if (strategy == ValueGenerationStrategy.IDENTITY)
        {
            return IdGeneratorStrategy.IDENTITY;
        }
        else if (strategy == ValueGenerationStrategy.INCREMENT)
        {
            return IdGeneratorStrategy.INCREMENT;
        }
        else if (strategy == ValueGenerationStrategy.NATIVE)
        {
            return IdGeneratorStrategy.NATIVE;
        }
        else if (strategy == ValueGenerationStrategy.SEQUENCE)
        {
            return IdGeneratorStrategy.SEQUENCE;
        }
        else if (strategy == ValueGenerationStrategy.UUIDHEX)
        {
            return IdGeneratorStrategy.UUIDHEX;
        }
        else if (strategy == ValueGenerationStrategy.UUIDSTRING)
        {
            return IdGeneratorStrategy.UUIDSTRING;
        }
        return IdGeneratorStrategy.UNSPECIFIED;
    }

    public ArrayMetadata newArrayMetadata()
    {
        ArrayMetaData internalArrmd = getInternal().newArrayMetaData();
        ArrayMetadataImpl arrmd = new ArrayMetadataImpl(internalArrmd);
        arrmd.parent = this;
        return arrmd;
    }

    public CollectionMetadata newCollectionMetadata()
    {
        CollectionMetaData internalCollmd = getInternal().newCollectionMetaData();
        CollectionMetadataImpl collmd = new CollectionMetadataImpl(internalCollmd);
        collmd.parent = this;
        return collmd;
    }

    public ElementMetadata newElementMetadata()
    {
        ElementMetaData internalElemmd = getInternal().newElementMetaData();
        ElementMetadataImpl elemmd = new ElementMetadataImpl(internalElemmd);
        elemmd.parent = this;
        return elemmd;
    }

    public EmbeddedMetadata newEmbeddedMetadata()
    {
        EmbeddedMetaData internalEmbmd = getInternal().newEmbeddedMetaData();
        EmbeddedMetadataImpl embmd = new EmbeddedMetadataImpl(internalEmbmd);
        embmd.parent = this;
        return embmd;
    }

    public ForeignKeyMetadata newForeignKeyMetadata()
    {
        ForeignKeyMetaData internalFkmd = getInternal().newForeignKeyMetaData();
        ForeignKeyMetadataImpl fkmd = new ForeignKeyMetadataImpl(internalFkmd);
        fkmd.parent = this;
        return fkmd;
    }

    public IndexMetadata newIndexMetadata()
    {
        IndexMetaData internalIdxmd = getInternal().newIndexMetaData();
        IndexMetadataImpl idxmd = new IndexMetadataImpl(internalIdxmd);
        idxmd.parent = this;
        return idxmd;
    }

    public JoinMetadata newJoinMetadata()
    {
        JoinMetaData internalJoinmd = getInternal().newJoinMetaData();
        JoinMetadataImpl joinmd = new JoinMetadataImpl(internalJoinmd);
        joinmd.parent = this;
        return joinmd;
    }

    public KeyMetadata newKeyMetadata()
    {
        KeyMetaData internalKeymd = getInternal().newKeyMetaData();
        KeyMetadataImpl keymd = new KeyMetadataImpl(internalKeymd);
        keymd.parent = this;
        return keymd;
    }

    public MapMetadata newMapMetadata()
    {
        MapMetaData internalMapmd = getInternal().newMapMetaData();
        MapMetadataImpl mapmd = new MapMetadataImpl(internalMapmd);
        mapmd.parent = this;
        return mapmd;
    }

    public OrderMetadata newOrderMetadata()
    {
        OrderMetaData internalOrdmd = getInternal().newOrderMetaData();
        OrderMetadataImpl ordmd = new OrderMetadataImpl(internalOrdmd);
        ordmd.parent = this;
        return ordmd;
    }

    public UniqueMetadata newUniqueMetadata()
    {
        UniqueMetaData internalUnimd = getInternal().newUniqueMetaData();
        UniqueMetadataImpl unimd = new UniqueMetadataImpl(internalUnimd);
        unimd.parent = this;
        return unimd;
    }

    public ValueMetadata newValueMetadata()
    {
        ValueMetaData internalValmd = getInternal().newValueMetaData();
        ValueMetadataImpl valmd = new ValueMetadataImpl(internalValmd);
        valmd.parent = this;
        return valmd;
    }

    public MemberMetadata setCacheable(boolean cache)
    {
        getInternal().setCacheable(cache);
        return this;
    }

    public MemberMetadata setColumn(String name)
    {
        getInternal().setColumn(name);
        return this;
    }

    public MemberMetadata setCustomStrategy(String strategy)
    {
        getInternal().setValueStrategy(ValueGenerationStrategy.getIdentityStrategy(strategy));
        return this;
    }

    public MemberMetadata setDefaultFetchGroup(boolean dfg)
    {
        getInternal().setDefaultFetchGroup(dfg);
        return this;
    }

    public MemberMetadata setDeleteAction(ForeignKeyAction fk)
    {
        ForeignKeyMetaData fkmd = getInternal().getForeignKeyMetaData();
        if (fk == ForeignKeyAction.CASCADE)
        {
            fkmd.setDeleteAction(org.datanucleus.metadata.ForeignKeyAction.CASCADE);
        }
        else if (fk == ForeignKeyAction.DEFAULT)
        {
            fkmd.setDeleteAction(org.datanucleus.metadata.ForeignKeyAction.DEFAULT);
        }
        else if (fk == ForeignKeyAction.NONE)
        {
            fkmd.setDeleteAction(org.datanucleus.metadata.ForeignKeyAction.NONE);
        }
        else if (fk == ForeignKeyAction.NULL)
        {
            fkmd.setDeleteAction(org.datanucleus.metadata.ForeignKeyAction.NULL);
        }
        else if (fk == ForeignKeyAction.RESTRICT)
        {
            fkmd.setDeleteAction(org.datanucleus.metadata.ForeignKeyAction.RESTRICT);
        }
        return this;
    }

    public MemberMetadata setDependent(boolean flag)
    {
        getInternal().setDependent(flag);
        return this;
    }

    public MemberMetadata setEmbedded(boolean flag)
    {
        getInternal().setEmbedded(flag);
        return this;
    }

    public MemberMetadata setFieldType(String types)
    {
        getInternal().setFieldTypes(types);
        return this;
    }

    public MemberMetadata setIndexed(boolean flag)
    {
        if (flag)
        {
            getInternal().setIndexed(IndexedValue.TRUE);
        }
        else
        {
            getInternal().setIndexed(IndexedValue.FALSE);
        }
        return this;
    }

    public MemberMetadata setLoadFetchGroup(String load)
    {
        getInternal().setLoadFetchGroup(load);
        return this;
    }

    public MemberMetadata setMappedBy(String mappedBy)
    {
        getInternal().setMappedBy(mappedBy);
        return this;
    }

    public MemberMetadata setName(String name)
    {
        // Set at construction
        return this;
    }

    public MemberMetadata setNullValue(NullValue val)
    {
        if (val == NullValue.DEFAULT)
        {
            getInternal().setNullValue(org.datanucleus.metadata.NullValue.DEFAULT);
        }
        else if (val == NullValue.EXCEPTION)
        {
            getInternal().setNullValue(org.datanucleus.metadata.NullValue.EXCEPTION);
        }
        else if (val == NullValue.NONE)
        {
            getInternal().setNullValue(org.datanucleus.metadata.NullValue.NONE);
        }
        return this;
    }

    public MemberMetadata setPersistenceModifier(PersistenceModifier val)
    {
        if (val == PersistenceModifier.NONE)
        {
            getInternal().setPersistenceModifier(FieldPersistenceModifier.NONE);
        }
        else if (val == PersistenceModifier.PERSISTENT)
        {
            getInternal().setPersistenceModifier(FieldPersistenceModifier.PERSISTENT);
        }
        else if (val == PersistenceModifier.TRANSACTIONAL)
        {
            getInternal().setPersistenceModifier(FieldPersistenceModifier.TRANSACTIONAL);
        }
        return this;
    }

    public MemberMetadata setPrimaryKey(boolean flag)
    {
        getInternal().setPrimaryKey(flag);
        return this;
    }

    public MemberMetadata setRecursionDepth(int depth)
    {
        getInternal().setRecursionDepth(depth);
        return this;
    }

    public MemberMetadata setSequence(String seq)
    {
        getInternal().setSequence(seq);
        return this;
    }

    public MemberMetadata setSerialized(boolean flag)
    {
        getInternal().setSerialised(flag);
        return this;
    }

    public MemberMetadata setTable(String table)
    {
        getInternal().setTable(table);
        return this;
    }

    public MemberMetadata setUnique(boolean flag)
    {
        getInternal().setUnique(flag);
        return this;
    }

    public MemberMetadata setValueStrategy(IdGeneratorStrategy strategy)
    {
        if (strategy == IdGeneratorStrategy.IDENTITY)
        {
            getInternal().setValueStrategy(ValueGenerationStrategy.IDENTITY);
        }
        else if (strategy == IdGeneratorStrategy.INCREMENT)
        {
            getInternal().setValueStrategy(ValueGenerationStrategy.INCREMENT);
        }
        else if (strategy == IdGeneratorStrategy.NATIVE)
        {
            getInternal().setValueStrategy(ValueGenerationStrategy.NATIVE);
        }
        else if (strategy == IdGeneratorStrategy.SEQUENCE)
        {
            getInternal().setValueStrategy(ValueGenerationStrategy.SEQUENCE);
        }
        else if (strategy == IdGeneratorStrategy.UUIDHEX)
        {
            getInternal().setValueStrategy(ValueGenerationStrategy.UUIDHEX);
        }
        else if (strategy == IdGeneratorStrategy.UUIDSTRING)
        {
            getInternal().setValueStrategy(ValueGenerationStrategy.UUIDSTRING);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MemberMetadata#getColumns()
     */
    public ColumnMetadata[] getColumns()
    {
        ColumnMetaData[] internalColmds = getInternal().getColumnMetaData();
        if (internalColmds == null)
        {
            return null;
        }
        ColumnMetadataImpl[] colmds = new ColumnMetadataImpl[internalColmds.length];
        for (int i=0;i<colmds.length;i++)
        {
            colmds[i] = new ColumnMetadataImpl(internalColmds[i]);
            colmds[i].parent = this;
        }
        return colmds;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MemberMetadata#getNumberOfColumns()
     */
    public int getNumberOfColumns()
    {
        ColumnMetaData[] colmds = getInternal().getColumnMetaData();
        return (colmds != null ? colmds.length : 0);
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MemberMetadata#newColumnMetadata()
     */
    public ColumnMetadata newColumnMetadata()
    {
        ColumnMetaData internalColmd = getInternal().newColumnMetaData();
        ColumnMetadataImpl colmd = new ColumnMetadataImpl(internalColmd);
        colmd.parent = this;
        return colmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MemberMetadata#getConverter()
     */
    @Override
    public AttributeConverter<?, ?> getConverter()
    {
        String typeConverterName = getInternal().getTypeConverterName();
        if (typeConverterName != null)
        {
            JDOTypeConverter typeConv = (JDOTypeConverter)getInternal().getMetaDataManager().getNucleusContext().getTypeManager().getTypeConverterForName(typeConverterName);
            return typeConv.getAttributeConverter();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MemberMetadata#setConverter(javax.jdo.AttributeConverter)
     */
    @Override
    public MemberMetadata setConverter(AttributeConverter<?, ?> conv)
    {
        Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(conv.getClass(), getInternal().getType());
        Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(conv.getClass(), attrType, null);

        // Register the TypeConverter under the name of the AttributeConverter class
        JDOTypeConverter typeConv = new JDOTypeConverter(conv);
        TypeManager typeMgr = getInternal().getMetaDataManager().getNucleusContext().getTypeManager();
        typeMgr.registerConverter(conv.getClass().getName(), typeConv, attrType, dbType, false, null);

        getInternal().setTypeConverterName(conv.getClass().getName());

        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MemberMetadata#getUseDefaultConversion()
     */
    @Override
    public Boolean getUseDefaultConversion()
    {
        if (getInternal().hasExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_DISABLED))
        {
            String val = getInternal().getValueForExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_DISABLED);
            return Boolean.valueOf(val);
        }
        else if (getInternal().hasExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_NAME))
        {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MemberMetadata#setUseDefaultConversion(Boolean)
     */
    @Override
    public MemberMetadata setUseDefaultConversion(Boolean flag)
    {
        if (flag)
        {
            getInternal().addExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_DISABLED, "true");
        }
        else
        {
            getInternal().removeExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_DISABLED);
        }

        return this;
    }
}