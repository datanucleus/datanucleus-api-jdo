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

import javax.jdo.annotations.VersionStrategy;
import javax.jdo.metadata.ColumnMetadata;
import javax.jdo.metadata.IndexMetadata;
import javax.jdo.metadata.Indexed;
import javax.jdo.metadata.VersionMetadata;

import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.IndexMetaData;
import org.datanucleus.metadata.IndexedValue;
import org.datanucleus.metadata.VersionMetaData;

/**
 * Implementation of JDO VersionMetadata object.
 */
public class VersionMetadataImpl extends AbstractMetadataImpl implements VersionMetadata
{
    public VersionMetadataImpl(VersionMetaData internal)
    {
        super(internal);
    }

    public VersionMetaData getInternal()
    {
        return (VersionMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.VersionMetadata#getColumn()
     */
    public String getColumn()
    {
        return getInternal().getColumnName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.VersionMetadata#getColumns()
     */
    public ColumnMetadata[] getColumns()
    {
        ColumnMetaData internalColmd = getInternal().getColumnMetaData();
        if (internalColmd == null)
        {
            return null;
        }
        ColumnMetadataImpl[] colmds = new ColumnMetadataImpl[1];
        for (int i=0;i<colmds.length;i++)
        {
            colmds[i] = new ColumnMetadataImpl(internalColmd);
            colmds[i].parent = this;
        }
        return colmds;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.VersionMetadata#getIndexMetadata()
     */
    public IndexMetadata getIndexMetadata()
    {
        IndexMetaData internalIdxmd = getInternal().getIndexMetaData();
        IndexMetadataImpl idxmd = new IndexMetadataImpl(internalIdxmd);
        idxmd.parent = this;
        return idxmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.VersionMetadata#getIndexed()
     */
    public Indexed getIndexed()
    {
        IndexedValue val = getInternal().getIndexed();
        if (val == IndexedValue.FALSE)
        {
            return Indexed.FALSE;
        }
        else if (val == IndexedValue.TRUE)
        {
            return Indexed.TRUE;
        }
        else if (val == IndexedValue.UNIQUE)
        {
            return Indexed.UNIQUE;
        }
        else
        {
            return Indexed.UNSPECIFIED;
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.VersionMetadata#getNumberOfColumns()
     */
    public int getNumberOfColumns()
    {
        ColumnMetaData colmd = getInternal().getColumnMetaData();
        return (colmd != null ? 1 : 0);
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.VersionMetadata#getStrategy()
     */
    public VersionStrategy getStrategy()
    {
        org.datanucleus.metadata.VersionStrategy strategy = getInternal().getStrategy();
        if (strategy == org.datanucleus.metadata.VersionStrategy.DATE_TIME)
        {
            return VersionStrategy.DATE_TIME;
        }
        else if (strategy == org.datanucleus.metadata.VersionStrategy.VERSION_NUMBER)
        {
            return VersionStrategy.VERSION_NUMBER;
        }
        else if (strategy == org.datanucleus.metadata.VersionStrategy.STATE_IMAGE)
        {
            return VersionStrategy.STATE_IMAGE;
        }
        else if (strategy == org.datanucleus.metadata.VersionStrategy.NONE)
        {
            return VersionStrategy.NONE;
        }
        else
        {
            return VersionStrategy.UNSPECIFIED;
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.VersionMetadata#newColumnMetadata()
     */
    public ColumnMetadata newColumnMetadata()
    {
        ColumnMetaData internalColmd = getInternal().newColumnMetaData();
        ColumnMetadataImpl colmd = new ColumnMetadataImpl(internalColmd);
        colmd.parent = this;
        return colmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.VersionMetadata#newIndexMetadata()
     */
    public IndexMetadata newIndexMetadata()
    {
        IndexMetaData internalIdxmd = getInternal().newIndexMetaData();
        IndexMetadataImpl idxmd = new IndexMetadataImpl(internalIdxmd);
        idxmd.parent = this;
        return idxmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.VersionMetadata#setColumn(java.lang.String)
     */
    public VersionMetadata setColumn(String name)
    {
        getInternal().setColumnName(name);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.VersionMetadata#setIndexed(javax.jdo.metadata.Indexed)
     */
    public VersionMetadata setIndexed(Indexed idx)
    {
        if (idx == Indexed.FALSE)
        {
            getInternal().setIndexed(IndexedValue.FALSE);
        }
        else if (idx == Indexed.TRUE)
        {
            getInternal().setIndexed(IndexedValue.TRUE);
        }
        else if (idx == Indexed.UNIQUE)
        {
            getInternal().setIndexed(IndexedValue.UNIQUE);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.VersionMetadata#setStrategy(javax.jdo.annotations.VersionStrategy)
     */
    public VersionMetadata setStrategy(VersionStrategy str)
    {
        if (str == VersionStrategy.DATE_TIME)
        {
            getInternal().setStrategy(org.datanucleus.metadata.VersionStrategy.DATE_TIME);
        }
        else if (str == VersionStrategy.VERSION_NUMBER)
        {
            getInternal().setStrategy(org.datanucleus.metadata.VersionStrategy.VERSION_NUMBER);
        }
        else if (str == VersionStrategy.STATE_IMAGE)
        {
            getInternal().setStrategy(org.datanucleus.metadata.VersionStrategy.STATE_IMAGE);
        }
        else if (str == VersionStrategy.NONE)
        {
            getInternal().setStrategy(org.datanucleus.metadata.VersionStrategy.NONE);
        }
        return this;
    }
}