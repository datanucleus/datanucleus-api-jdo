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

import javax.jdo.metadata.ColumnMetadata;
import javax.jdo.metadata.FieldMetadata;
import javax.jdo.metadata.MemberMetadata;
import javax.jdo.metadata.PropertyMetadata;
import javax.jdo.metadata.UniqueMetadata;

import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.FieldMetaData;
import org.datanucleus.metadata.PropertyMetaData;
import org.datanucleus.metadata.UniqueMetaData;

/**
 * Implementation of JDO UniqueMetadata object.
 */
public class UniqueMetadataImpl extends AbstractMetadataImpl implements UniqueMetadata
{
    public UniqueMetadataImpl(UniqueMetaData internal)
    {
        super(internal);
    }

    public UniqueMetaData getInternal()
    {
        return (UniqueMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#getColumns()
     */
    public ColumnMetadata[] getColumns()
    {
        String[] internalColumnNames = getInternal().getColumnNames();
        if (internalColumnNames == null)
        {
            return null;
        }
        ColumnMetadataImpl[] colmds = new ColumnMetadataImpl[internalColumnNames.length];
        for (int i=0;i<colmds.length;i++)
        {
            ColumnMetaData internalColmd = new ColumnMetaData();
            internalColmd.setName(internalColumnNames[i]);
            colmds[i] = new ColumnMetadataImpl(internalColmd);
            colmds[i].parent = this;
        }
        return colmds;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#getDeferred()
     */
    public Boolean getDeferred()
    {
        return getInternal().isDeferred();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#getMembers()
     */
    public MemberMetadata[] getMembers()
    {
        String[] internalMemberNames = getInternal().getMemberNames();
        if (internalMemberNames == null)
        {
            return null;
        }
        MemberMetadataImpl[] mmds = new MemberMetadataImpl[internalMemberNames.length];
        for (int i=0;i<mmds.length;i++)
        {
            FieldMetaData fmd = new FieldMetaData(getInternal(), internalMemberNames[i]);
            mmds[i] = new FieldMetadataImpl(fmd);
            mmds[i].parent = this;
        }
        return mmds;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#getName()
     */
    public String getName()
    {
        return getInternal().getName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#getNumberOfColumns()
     */
    public int getNumberOfColumns()
    {
        return getInternal().getNumberOfColumns();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#getNumberOfMembers()
     */
    public int getNumberOfMembers()
    {
        return getInternal().getNumberOfMembers();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#getTable()
     */
    public String getTable()
    {
        return getInternal().getTable();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#newColumnMetadata()
     */
    public ColumnMetadata newColumnMetadata()
    {
        ColumnMetaData internalColmd = new ColumnMetaData();
        internalColmd.setParent(getInternal());
        ColumnMetadataImpl colmd = new ColumnMetadataImpl(internalColmd);
        colmd.parent = this;
        return colmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#newFieldMetadata(java.lang.String)
     */
    public FieldMetadata newFieldMetadata(String name)
    {
        FieldMetaData internalFmd = new FieldMetaData(getInternal(), name);
        FieldMetadataImpl fmd = new FieldMetadataImpl(internalFmd);
        fmd.parent = this;
        return fmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#newPropertyMetadata(java.lang.String)
     */
    public PropertyMetadata newPropertyMetadata(String name)
    {
        PropertyMetaData internalPmd = new PropertyMetaData(getInternal(), name);
        PropertyMetadataImpl pmd = new PropertyMetadataImpl(internalPmd);
        pmd.parent = this;
        return pmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#setDeferred(boolean)
     */
    public UniqueMetadata setDeferred(boolean flag)
    {
        getInternal().setDeferred(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#setName(java.lang.String)
     */
    public UniqueMetadata setName(String name)
    {
        getInternal().setName(name);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.UniqueMetadata#setTable(java.lang.String)
     */
    public UniqueMetadata setTable(String name)
    {
        getInternal().setTable(name);
        return this;
    }
}