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

import javax.jdo.annotations.ForeignKeyAction;
import javax.jdo.metadata.ColumnMetadata;
import javax.jdo.metadata.FieldMetadata;
import javax.jdo.metadata.ForeignKeyMetadata;
import javax.jdo.metadata.MemberMetadata;
import javax.jdo.metadata.PropertyMetadata;

import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.FieldMetaData;
import org.datanucleus.metadata.ForeignKeyMetaData;
import org.datanucleus.metadata.PropertyMetaData;

/**
 * Implementation of JDO ForeignKeyMetadata object.
 */
public class ForeignKeyMetadataImpl extends AbstractMetadataImpl implements ForeignKeyMetadata
{
    public ForeignKeyMetadataImpl(ForeignKeyMetaData internal)
    {
        super(internal);
    }

    public ForeignKeyMetaData getInternal()
    {
        return (ForeignKeyMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#getColumns()
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
     * @see javax.jdo.metadata.ForeignKeyMetadata#getDeferred()
     */
    public Boolean getDeferred()
    {
        return getInternal().isDeferred();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#getDeleteAction()
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
     * @see javax.jdo.metadata.ForeignKeyMetadata#getName()
     */
    public String getName()
    {
        return getInternal().getName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#getNumberOfColumns()
     */
    public int getNumberOfColumns()
    {
        ColumnMetaData[] colmds = getInternal().getColumnMetaData();
        return (colmds != null ? colmds.length : 0);
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#getNumberOfMembers()
     */
    public int getNumberOfMembers()
    {
        String[] internalMemberNames = getInternal().getMemberNames();
        return (internalMemberNames != null ? internalMemberNames.length : 0);
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#getMembers()
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
     * @see javax.jdo.metadata.ForeignKeyMetadata#getTable()
     */
    public String getTable()
    {
        return getInternal().getTable();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#getUnique()
     */
    public Boolean getUnique()
    {
        return getInternal().isUnique();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#getUpdateAction()
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
     * @see javax.jdo.metadata.ForeignKeyMetadata#newColumnMetadata()
     */
    public ColumnMetadata newColumnMetadata()
    {
        ColumnMetaData internalColmd = getInternal().newColumnMetaData();
        ColumnMetadataImpl colmd = new ColumnMetadataImpl(internalColmd);
        colmd.parent = this;
        return colmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#newFieldMetadata(java.lang.String)
     */
    public FieldMetadata newFieldMetadata(String name)
    {
        FieldMetaData internalFmd = new FieldMetaData(getInternal(), name);
        FieldMetadataImpl fmd = new FieldMetadataImpl(internalFmd);
        fmd.parent = this;
        return fmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#newPropertyMetadata(java.lang.String)
     */
    public PropertyMetadata newPropertyMetadata(String name)
    {
        PropertyMetaData internalPmd = new PropertyMetaData(getInternal(), name);
        PropertyMetadataImpl pmd = new PropertyMetadataImpl(internalPmd);
        pmd.parent = this;
        return pmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#setDeferred(boolean)
     */
    public ForeignKeyMetadata setDeferred(boolean flag)
    {
        getInternal().setDeferred(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#setDeleteAction(javax.jdo.annotations.ForeignKeyAction)
     */
    public ForeignKeyMetadata setDeleteAction(ForeignKeyAction fk)
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
     * @see javax.jdo.metadata.ForeignKeyMetadata#setName(java.lang.String)
     */
    public ForeignKeyMetadata setName(String name)
    {
        getInternal().setName(name);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#setTable(java.lang.String)
     */
    public ForeignKeyMetadata setTable(String name)
    {
        getInternal().setTable(name);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#setUnique(boolean)
     */
    public ForeignKeyMetadata setUnique(boolean flag)
    {
        getInternal().setUnique(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ForeignKeyMetadata#setUpdateAction(javax.jdo.annotations.ForeignKeyAction)
     */
    public ForeignKeyMetadata setUpdateAction(ForeignKeyAction fk)
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
}