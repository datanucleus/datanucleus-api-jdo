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

import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.metadata.DiscriminatorMetadata;
import javax.jdo.metadata.InheritanceMetadata;
import javax.jdo.metadata.JoinMetadata;

import org.datanucleus.metadata.DiscriminatorMetaData;
import org.datanucleus.metadata.InheritanceMetaData;
import org.datanucleus.metadata.JoinMetaData;

/**
 * Implementation of JDO InheritanceMetadata object.
 */
public class InheritanceMetadataImpl extends AbstractMetadataImpl implements InheritanceMetadata
{
    public InheritanceMetadataImpl(InheritanceMetaData internal)
    {
        super(internal);
    }

    public InheritanceMetaData getInternal()
    {
        return (InheritanceMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.InheritanceMetadata#getCustomStrategy()
     */
    public String getCustomStrategy()
    {
        org.datanucleus.metadata.InheritanceStrategy str = getInternal().getStrategy();
        if (str != org.datanucleus.metadata.InheritanceStrategy.NEW_TABLE &&
            str != org.datanucleus.metadata.InheritanceStrategy.SUBCLASS_TABLE &&
            str != org.datanucleus.metadata.InheritanceStrategy.SUPERCLASS_TABLE &&
            str != null)
        {
            return str.toString();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.InheritanceMetadata#getDiscriminatorMetaData()
     */
    public DiscriminatorMetadata getDiscriminatorMetadata()
    {
        DiscriminatorMetaData internalDismd = getInternal().getDiscriminatorMetaData();
        if (internalDismd == null)
        {
            return null;
        }
        DiscriminatorMetadataImpl dismd = new DiscriminatorMetadataImpl(internalDismd);
        dismd.parent = this;
        return dismd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.InheritanceMetadata#getJoinMetaData()
     */
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

    /* (non-Javadoc)
     * @see javax.jdo.metadata.InheritanceMetadata#getStrategy()
     */
    public InheritanceStrategy getStrategy()
    {
        org.datanucleus.metadata.InheritanceStrategy str = getInternal().getStrategy();
        if (str == org.datanucleus.metadata.InheritanceStrategy.NEW_TABLE)
        {
            return InheritanceStrategy.NEW_TABLE;
        }
        else if (str == org.datanucleus.metadata.InheritanceStrategy.SUBCLASS_TABLE)
        {
            return InheritanceStrategy.SUBCLASS_TABLE;
        }
        else if (str == org.datanucleus.metadata.InheritanceStrategy.SUPERCLASS_TABLE)
        {
            return InheritanceStrategy.SUPERCLASS_TABLE;
        }
        return InheritanceStrategy.UNSPECIFIED;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.InheritanceMetadata#newDiscriminatorMetadata()
     */
    public DiscriminatorMetadata newDiscriminatorMetadata()
    {
        DiscriminatorMetaData internalDismd = getInternal().newDiscriminatorMetaData();
        DiscriminatorMetadataImpl dismd = new DiscriminatorMetadataImpl(internalDismd);
        dismd.parent = this;
        return dismd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.InheritanceMetadata#newJoinMetadata()
     */
    public JoinMetadata newJoinMetadata()
    {
        JoinMetaData internalJoinmd = getInternal().newJoinMetaData();
        JoinMetadataImpl joinmd = new JoinMetadataImpl(internalJoinmd);
        joinmd.parent = this;
        return joinmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.InheritanceMetadata#setCustomStrategy(java.lang.String)
     */
    public InheritanceMetadata setCustomStrategy(String str)
    {
        getInternal().setStrategy(str);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.InheritanceMetadata#setStrategy(javax.jdo.annotations.InheritanceStrategy)
     */
    public InheritanceMetadata setStrategy(InheritanceStrategy str)
    {
        if (str == InheritanceStrategy.NEW_TABLE)
        {
            getInternal().setStrategy(org.datanucleus.metadata.InheritanceStrategy.NEW_TABLE);
        }
        else if (str == InheritanceStrategy.SUBCLASS_TABLE)
        {
            getInternal().setStrategy(org.datanucleus.metadata.InheritanceStrategy.SUBCLASS_TABLE);
        }
        else if (str == InheritanceStrategy.SUPERCLASS_TABLE)
        {
            getInternal().setStrategy(org.datanucleus.metadata.InheritanceStrategy.SUPERCLASS_TABLE);
        }
        return this;
    }
}