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

import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.metadata.ColumnMetadata;
import javax.jdo.metadata.DiscriminatorMetadata;
import javax.jdo.metadata.IndexMetadata;
import javax.jdo.metadata.Indexed;

import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.DiscriminatorMetaData;
import org.datanucleus.metadata.IndexMetaData;
import org.datanucleus.metadata.IndexedValue;

/**
 * Implementation of JDO DiscriminatorMetadata object.
 */
public class DiscriminatorMetadataImpl extends AbstractMetadataImpl implements DiscriminatorMetadata
{
    public DiscriminatorMetadataImpl(DiscriminatorMetaData internal)
    {
        super(internal);
    }

    public DiscriminatorMetaData getInternal()
    {
        return (DiscriminatorMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DiscriminatorMetadata#getColumn()
     */
    public String getColumn()
    {
        return getInternal().getColumnName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DiscriminatorMetadata#getColumns()
     */
    public ColumnMetadata[] getColumns()
    {
        ColumnMetaData internalColmd = getInternal().getColumnMetaData();
        if (internalColmd == null)
        {
            return null;
        }
        ColumnMetadataImpl[] colmds = new ColumnMetadataImpl[1];
        colmds[0] = new ColumnMetadataImpl(internalColmd);
        colmds[0].parent = this;
        return colmds;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DiscriminatorMetadata#getIndexMetadata()
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
     * @see javax.jdo.metadata.DiscriminatorMetadata#getIndexed()
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
     * @see javax.jdo.metadata.DiscriminatorMetadata#getNumberOfColumns()
     */
    public int getNumberOfColumns()
    {
        return 1;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DiscriminatorMetadata#getStrategy()
     */
    public DiscriminatorStrategy getStrategy()
    {
        org.datanucleus.metadata.DiscriminatorStrategy str = getInternal().getStrategy();
        if (str == org.datanucleus.metadata.DiscriminatorStrategy.CLASS_NAME)
        {
            return DiscriminatorStrategy.CLASS_NAME;
        }
        else if (str == org.datanucleus.metadata.DiscriminatorStrategy.VALUE_MAP)
        {
            return DiscriminatorStrategy.VALUE_MAP;
        }
        else if (str == org.datanucleus.metadata.DiscriminatorStrategy.NONE)
        {
            return DiscriminatorStrategy.NONE;
        }
        return DiscriminatorStrategy.UNSPECIFIED;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DiscriminatorMetadata#getValue()
     */
    public String getValue()
    {
        return getInternal().getValue();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DiscriminatorMetadata#newColumnMetadata()
     */
    public ColumnMetadata newColumnMetadata()
    {
        ColumnMetaData internalColmd = getInternal().newColumnMetaData();
        ColumnMetadataImpl colmd = new ColumnMetadataImpl(internalColmd);
        colmd.parent = this;
        return colmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DiscriminatorMetadata#newIndexMetadata()
     */
    public IndexMetadata newIndexMetadata()
    {
        IndexMetaData internalIdxmd = getInternal().newIndexMetaData();
        IndexMetadataImpl idxmd = new IndexMetadataImpl(internalIdxmd);
        idxmd.parent = this;
        return idxmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DiscriminatorMetadata#setColumn(java.lang.String)
     */
    public DiscriminatorMetadata setColumn(String name)
    {
        getInternal().setColumnName(name);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DiscriminatorMetadata#setIndexed(javax.jdo.metadata.Indexed)
     */
    public DiscriminatorMetadata setIndexed(Indexed idx)
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
     * @see javax.jdo.metadata.DiscriminatorMetadata#setStrategy(javax.jdo.annotations.DiscriminatorStrategy)
     */
    public DiscriminatorMetadata setStrategy(DiscriminatorStrategy str)
    {
        if (str == DiscriminatorStrategy.CLASS_NAME)
        {
            getInternal().setStrategy(org.datanucleus.metadata.DiscriminatorStrategy.CLASS_NAME);
        }
        else if (str == DiscriminatorStrategy.VALUE_MAP)
        {
            getInternal().setStrategy(org.datanucleus.metadata.DiscriminatorStrategy.VALUE_MAP);
        }
        else if (str == DiscriminatorStrategy.NONE)
        {
            getInternal().setStrategy(org.datanucleus.metadata.DiscriminatorStrategy.NONE);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DiscriminatorMetadata#setValue(java.lang.String)
     */
    public DiscriminatorMetadata setValue(String val)
    {
        getInternal().setValue(val);
        return this;
    }
}