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
package org.datanucleus.api.jdo.metadata;

import javax.jdo.AttributeConverter;
import javax.jdo.annotations.ForeignKeyAction;
import javax.jdo.metadata.ColumnMetadata;
import javax.jdo.metadata.EmbeddedMetadata;
import javax.jdo.metadata.ForeignKeyMetadata;
import javax.jdo.metadata.IndexMetadata;
import javax.jdo.metadata.KeyMetadata;
import javax.jdo.metadata.UniqueMetadata;

import org.datanucleus.api.jdo.JDOTypeConverter;
import org.datanucleus.api.jdo.JDOTypeConverterUtils;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.EmbeddedMetaData;
import org.datanucleus.metadata.ForeignKeyMetaData;
import org.datanucleus.metadata.IndexMetaData;
import org.datanucleus.metadata.KeyMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.metadata.UniqueMetaData;
import org.datanucleus.store.types.TypeManager;

/**
 * Implementation of JDO KeyMetadata object.
 */
public class KeyMetadataImpl extends AbstractMetadataImpl implements KeyMetadata
{
    public KeyMetadataImpl(KeyMetaData internal)
    {
        super(internal);
    }

    public KeyMetaData getInternal()
    {
        return (KeyMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#getColumn()
     */
    public String getColumn()
    {
        return getInternal().getColumnName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#getColumns()
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
     * @see javax.jdo.metadata.KeyMetadata#getNumberOfColumns()
     */
    public int getNumberOfColumns()
    {
        ColumnMetaData[] colmds = getInternal().getColumnMetaData();
        return (colmds != null ? colmds.length : 0);
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#setColumn(java.lang.String)
     */
    public KeyMetadata setColumn(String name)
    {
        getInternal().setColumnName(name);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#newColumnMetadata()
     */
    public ColumnMetadata newColumnMetadata()
    {
        ColumnMetaData internalColmd = getInternal().newColumnMetaData();
        ColumnMetadataImpl colmd = new ColumnMetadataImpl(internalColmd);
        colmd.parent = this;
        return colmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#getDeleteAction()
     */
    public ForeignKeyAction getDeleteAction()
    {
        org.datanucleus.metadata.ForeignKeyAction fk = getInternal().getDeleteAction();
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
        return ForeignKeyAction.UNSPECIFIED;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#setDeleteAction(javax.jdo.annotations.ForeignKeyAction)
     */
    public KeyMetadata setDeleteAction(ForeignKeyAction fk)
    {
        if (fk == ForeignKeyAction.CASCADE)
        {
            getInternal().setDeleteAction(org.datanucleus.metadata.ForeignKeyAction.CASCADE);
        }
        else if (fk == ForeignKeyAction.DEFAULT)
        {
            getInternal().setDeleteAction(org.datanucleus.metadata.ForeignKeyAction.DEFAULT);
        }
        else if (fk == ForeignKeyAction.NONE)
        {
            getInternal().setDeleteAction(org.datanucleus.metadata.ForeignKeyAction.NONE);
        }
        else if (fk == ForeignKeyAction.NULL)
        {
            getInternal().setDeleteAction(org.datanucleus.metadata.ForeignKeyAction.NULL);
        }
        else if (fk == ForeignKeyAction.RESTRICT)
        {
            getInternal().setDeleteAction(org.datanucleus.metadata.ForeignKeyAction.RESTRICT);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#getUpdateAction()
     */
    public ForeignKeyAction getUpdateAction()
    {
        org.datanucleus.metadata.ForeignKeyAction fk = getInternal().getUpdateAction();
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
        return ForeignKeyAction.UNSPECIFIED;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#setUpdateAction(javax.jdo.annotations.ForeignKeyAction)
     */
    public KeyMetadata setUpdateAction(ForeignKeyAction fk)
    {
        if (fk == ForeignKeyAction.CASCADE)
        {
            getInternal().setUpdateAction(org.datanucleus.metadata.ForeignKeyAction.CASCADE);
        }
        else if (fk == ForeignKeyAction.DEFAULT)
        {
            getInternal().setUpdateAction(org.datanucleus.metadata.ForeignKeyAction.DEFAULT);
        }
        else if (fk == ForeignKeyAction.NONE)
        {
            getInternal().setUpdateAction(org.datanucleus.metadata.ForeignKeyAction.NONE);
        }
        else if (fk == ForeignKeyAction.NULL)
        {
            getInternal().setUpdateAction(org.datanucleus.metadata.ForeignKeyAction.NULL);
        }
        else if (fk == ForeignKeyAction.RESTRICT)
        {
            getInternal().setUpdateAction(org.datanucleus.metadata.ForeignKeyAction.RESTRICT);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#getForeignKeyMetadata()
     */
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

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#newForeignKeyMetadata()
     */
    public ForeignKeyMetadata newForeignKeyMetadata()
    {
        ForeignKeyMetaData internalFkmd = getInternal().newForeignKeyMetaData();
        ForeignKeyMetadataImpl fkmd = new ForeignKeyMetadataImpl(internalFkmd);
        fkmd.parent = this;
        return fkmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#getIndexMetadata()
     */
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

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#newIndexMetadata()
     */
    public IndexMetadata newIndexMetadata()
    {
        IndexMetaData internalIdxmd = getInternal().newIndexMetaData();
        IndexMetadataImpl idxmd = new IndexMetadataImpl(internalIdxmd);
        idxmd.parent = this;
        return idxmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#getUniqueMetadata()
     */
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

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#newUniqueMetadata()
     */
    public UniqueMetadata newUniqueMetadata()
    {
        UniqueMetaData internalUnimd = getInternal().newUniqueMetaData();
        UniqueMetadataImpl unimd = new UniqueMetadataImpl(internalUnimd);
        unimd.parent = this;
        return unimd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#getEmbeddedMetadata()
     */
    public EmbeddedMetadata getEmbeddedMetadata()
    {
        EmbeddedMetaData internalEmbmd = getInternal().getEmbeddedMetaData();
        if (internalEmbmd == null)
        {
            return null;
        }
        EmbeddedMetadataImpl embmd = new EmbeddedMetadataImpl(internalEmbmd);
        embmd.parent = this;
        return embmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#newEmbeddedMetadata()
     */
    public EmbeddedMetadata newEmbeddedMetadata()
    {
        EmbeddedMetaData internalEmbmd = getInternal().newEmbeddedMetaData();
        EmbeddedMetadataImpl embmd = new EmbeddedMetadataImpl(internalEmbmd);
        embmd.parent = this;
        return embmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#getTable()
     */
    public String getTable()
    {
        return getInternal().getTable();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#setTable(java.lang.String)
     */
    public KeyMetadata setTable(String name)
    {
        getInternal().setTable(name);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#getConverter()
     */
    @Override
    public AttributeConverter<?, ?> getConverter()
    {
        KeyMetaData keymd = getInternal();
        if (keymd.hasExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_NAME))
        {
            String typeConverterName = keymd.getValueForExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_NAME);
            if (typeConverterName != null)
            {
                JDOTypeConverter typeConv = (JDOTypeConverter)getInternal().getMetaDataManager().getNucleusContext().getTypeManager().getTypeConverterForName(typeConverterName);
                return typeConv.getAttributeConverter();
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#setConverter(javax.jdo.AttributeConverter)
     */
    @Override
    public KeyMetadata setConverter(AttributeConverter<?, ?> conv)
    {
        String keyType = ((AbstractMemberMetaData)getInternal().getParent()).getMap().getKeyType();
        Class keyCls = getInternal().getMetaDataManager().getNucleusContext().getClassLoaderResolver(null).classForName(keyType);
        Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(conv.getClass(), keyCls);
        Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(conv.getClass(), attrType, null);

        // Register the TypeConverter under the name of the AttributeConverter class
        JDOTypeConverter typeConv = new JDOTypeConverter(conv, attrType, dbType);
        TypeManager typeMgr = getInternal().getMetaDataManager().getNucleusContext().getTypeManager();
        typeMgr.registerConverter(conv.getClass().getName(), typeConv);

        getInternal().addExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_NAME, conv.getClass().getName());

        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#getUseDefaultConversion()
     */
    @Override
    public Boolean getUseDefaultConversion()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.KeyMetadata#setUseDefaultConversion(boolean)
     */
    @Override
    public KeyMetadata setUseDefaultConversion(Boolean disable)
    {
        // TODO Auto-generated method stub
        return null;
    }
}