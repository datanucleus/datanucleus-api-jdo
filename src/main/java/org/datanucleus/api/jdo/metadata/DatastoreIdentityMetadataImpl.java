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

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.metadata.ColumnMetadata;
import javax.jdo.metadata.DatastoreIdentityMetadata;

import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.IdentityMetaData;
import org.datanucleus.metadata.ValueGenerationStrategy;

/**
 * Implementation of JDO DatastoreIdentityMetadata object.
 */
public class DatastoreIdentityMetadataImpl extends AbstractMetadataImpl implements DatastoreIdentityMetadata
{
    public DatastoreIdentityMetadataImpl(IdentityMetaData idmd)
    {
        super(idmd);
    }

    public IdentityMetaData getInternal()
    {
        return (IdentityMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DatastoreIdentityMetadata#getColumn()
     */
    public String getColumn()
    {
        return getInternal().getColumnName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DatastoreIdentityMetadata#getColumns()
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
     * @see javax.jdo.metadata.DatastoreIdentityMetadata#getCustomStrategy()
     */
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

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DatastoreIdentityMetadata#getNumberOfColumns()
     */
    public int getNumberOfColumns()
    {
        ColumnMetaData colmds = getInternal().getColumnMetaData();
        return (colmds != null ? 1 : 0);
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DatastoreIdentityMetadata#getSequence()
     */
    public String getSequence()
    {
        return getInternal().getSequence();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DatastoreIdentityMetadata#getStrategy()
     */
    public IdGeneratorStrategy getStrategy()
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

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DatastoreIdentityMetadata#newColumnMetadata()
     */
    public ColumnMetadata newColumnMetadata()
    {
        ColumnMetaData internalColmd = getInternal().newColumnMetaData();
        ColumnMetadataImpl colmd = new ColumnMetadataImpl(internalColmd);
        colmd.parent = this;
        return colmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DatastoreIdentityMetadata#setColumn(java.lang.String)
     */
    public DatastoreIdentityMetadata setColumn(String name)
    {
        getInternal().setColumnName(name);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DatastoreIdentityMetadata#setCustomStrategy(java.lang.String)
     */
    public DatastoreIdentityMetadata setCustomStrategy(String strategy)
    {
        getInternal().setValueStrategy(ValueGenerationStrategy.getIdentityStrategy(strategy));
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DatastoreIdentityMetadata#setSequence(java.lang.String)
     */
    public DatastoreIdentityMetadata setSequence(String seq)
    {
        getInternal().setSequence(seq);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.DatastoreIdentityMetadata#setStrategy(javax.jdo.annotations.VersionStrategy)
     */
    public DatastoreIdentityMetadata setStrategy(IdGeneratorStrategy strategy)
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
}