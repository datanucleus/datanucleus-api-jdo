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
import javax.jdo.metadata.ForeignKeyMetadata;
import javax.jdo.metadata.IndexMetadata;
import javax.jdo.metadata.Indexed;
import javax.jdo.metadata.JoinMetadata;
import javax.jdo.metadata.PrimaryKeyMetadata;
import javax.jdo.metadata.UniqueMetadata;

import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.ForeignKeyMetaData;
import org.datanucleus.metadata.IndexMetaData;
import org.datanucleus.metadata.IndexedValue;
import org.datanucleus.metadata.JoinMetaData;
import org.datanucleus.metadata.PrimaryKeyMetaData;
import org.datanucleus.metadata.UniqueMetaData;

/**
 * Implementation of JDO JoinMetadata object.
 */
public class JoinMetadataImpl extends AbstractMetadataImpl implements JoinMetadata
{
    public JoinMetadataImpl(JoinMetaData internal)
    {
        super(internal);
    }

    public JoinMetaData getInternal()
    {
        return (JoinMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#getColumn()
     */
    public String getColumn()
    {
        return getInternal().getColumnName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#getColumns()
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
     * @see javax.jdo.metadata.JoinMetadata#getDeleteAction()
     */
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

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#getForeignKeyMetadata()
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
     * @see javax.jdo.metadata.JoinMetadata#getIndexMetadata()
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
     * @see javax.jdo.metadata.JoinMetadata#getIndexed()
     */
    public Indexed getIndexed()
    {
        IndexedValue idxVal = getInternal().getIndexed();
        if (idxVal == IndexedValue.TRUE)
        {
            return Indexed.TRUE;
        }
        else if (idxVal == IndexedValue.FALSE)
        {
            return Indexed.FALSE;
        }
        else if (idxVal == IndexedValue.UNIQUE)
        {
            return Indexed.UNIQUE;
        }
        return Indexed.UNSPECIFIED;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#getNumberOfColumns()
     */
    public int getNumberOfColumns()
    {
        ColumnMetaData[] colmds = getInternal().getColumnMetaData();
        return (colmds != null ? colmds.length : 0);
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#getOuter()
     */
    public boolean getOuter()
    {
        return getInternal().isOuter();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#getPrimaryKeyMetadata()
     */
    public PrimaryKeyMetadata getPrimaryKeyMetadata()
    {
        PrimaryKeyMetaData internalPkmd = getInternal().getPrimaryKeyMetaData();
        if (internalPkmd == null)
        {
            return null;
        }
        PrimaryKeyMetadataImpl pkmd = new PrimaryKeyMetadataImpl(internalPkmd);
        pkmd.parent = this;
        return pkmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#getTable()
     */
    public String getTable()
    {
        return getInternal().getTable();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#getUnique()
     */
    public Boolean getUnique()
    {
        return getInternal().isUnique();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#getUniqueMetadata()
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
     * @see javax.jdo.metadata.JoinMetadata#newColumnMetadata()
     */
    public ColumnMetadata newColumnMetadata()
    {
        ColumnMetaData internalColmd = getInternal().newColumnMetaData();
        ColumnMetadataImpl colmd = new ColumnMetadataImpl(internalColmd);
        colmd.parent = this;
        return colmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#newForeignKeyMetadata()
     */
    public ForeignKeyMetadata newForeignKeyMetadata()
    {
        ForeignKeyMetaData internalFkmd = getInternal().newForeignKeyMetaData();
        ForeignKeyMetadataImpl fkmd = new ForeignKeyMetadataImpl(internalFkmd);
        fkmd.parent = this;
        return fkmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#newIndexMetadata()
     */
    public IndexMetadata newIndexMetadata()
    {
        IndexMetaData internalIdxmd = getInternal().newIndexMetaData();
        IndexMetadataImpl idxmd = new IndexMetadataImpl(internalIdxmd);
        idxmd.parent = this;
        return idxmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#newPrimaryKeyMetadata()
     */
    public PrimaryKeyMetadata newPrimaryKeyMetadata()
    {
        PrimaryKeyMetaData internalPkmd = getInternal().newPrimaryKeyMetaData();
        PrimaryKeyMetadataImpl pkmd = new PrimaryKeyMetadataImpl(internalPkmd);
        pkmd.parent = this;
        return pkmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#newUniqueMetadata()
     */
    public UniqueMetadata newUniqueMetadata()
    {
        UniqueMetaData internalUnimd = getInternal().newUniqueMetaData();
        UniqueMetadataImpl unimd = new UniqueMetadataImpl(internalUnimd);
        unimd.parent = this;
        return unimd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#setColumn(java.lang.String)
     */
    public JoinMetadata setColumn(String name)
    {
        getInternal().setColumnName(name);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#setDeleteAction(javax.jdo.annotations.ForeignKeyAction)
     */
    public JoinMetadata setDeleteAction(ForeignKeyAction fk)
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

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#setIndexed(javax.jdo.metadata.Indexed)
     */
    public JoinMetadata setIndexed(Indexed val)
    {
        if (val == Indexed.TRUE)
        {
            getInternal().setIndexed(IndexedValue.TRUE);
        }
        else if (val == Indexed.FALSE)
        {
            getInternal().setIndexed(IndexedValue.FALSE);
        }
        else if (val == Indexed.UNIQUE)
        {
            getInternal().setIndexed(IndexedValue.UNIQUE);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#setOuter(boolean)
     */
    public JoinMetadata setOuter(boolean flag)
    {
        getInternal().setOuter(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#setTable(java.lang.String)
     */
    public JoinMetadata setTable(String table)
    {
        getInternal().setTable(table);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JoinMetadata#setUnique(boolean)
     */
    public JoinMetadata setUnique(boolean flag)
    {
        getInternal().setUnique(flag);
        return this;
    }
}