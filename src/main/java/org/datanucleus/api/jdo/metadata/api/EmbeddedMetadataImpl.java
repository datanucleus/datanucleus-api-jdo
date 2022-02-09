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

import java.util.List;

import javax.jdo.metadata.DiscriminatorMetadata;
import javax.jdo.metadata.EmbeddedMetadata;
import javax.jdo.metadata.FieldMetadata;
import javax.jdo.metadata.MemberMetadata;
import javax.jdo.metadata.PropertyMetadata;

import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.DiscriminatorMetaData;
import org.datanucleus.metadata.EmbeddedMetaData;
import org.datanucleus.metadata.FieldMetaData;
import org.datanucleus.metadata.PropertyMetaData;

/**
 * Implementation of JDO EmbeddedMetadata object.
 */
public class EmbeddedMetadataImpl extends AbstractMetadataImpl implements EmbeddedMetadata
{
    public EmbeddedMetadataImpl(EmbeddedMetaData internal)
    {
        super(internal);
    }

    public EmbeddedMetaData getInternal()
    {
        return (EmbeddedMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.EmbeddedMetadata#getMembers()
     */
    public MemberMetadata[] getMembers()
    {
        List<AbstractMemberMetaData> internalMmds = getInternal().getMemberMetaData();
        if (internalMmds == null)
        {
            return null;
        }

        MemberMetadataImpl[] mmds = new MemberMetadataImpl[internalMmds.size()];
        int i = 0;
        for (AbstractMemberMetaData internalMmd : internalMmds)
        {
            if (internalMmd instanceof FieldMetaData)
            {
                mmds[i++] = new FieldMetadataImpl((FieldMetaData)internalMmd);
            }
            else
            {
                mmds[i++] = new PropertyMetadataImpl((PropertyMetaData)internalMmd);
            }
        }
        return mmds;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.EmbeddedMetadata#getNullIndicatorColumn()
     */
    public String getNullIndicatorColumn()
    {
        return getInternal().getNullIndicatorColumn();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.EmbeddedMetadata#getNullIndicatorValue()
     */
    public String getNullIndicatorValue()
    {
        return getInternal().getNullIndicatorValue();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.EmbeddedMetadata#getNumberOfMembers()
     */
    public int getNumberOfMembers()
    {
        List<AbstractMemberMetaData> mmds = getInternal().getMemberMetaData();
        return (mmds != null ? mmds.size() : 0);
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.EmbeddedMetadata#getOwnerMember()
     */
    public String getOwnerMember()
    {
        return getInternal().getOwnerMember();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.EmbeddedMetadata#newFieldMetadata(java.lang.String)
     */
    public FieldMetadata newFieldMetadata(String name)
    {
        FieldMetaData internalFmd = getInternal().newFieldMetaData(name);
        FieldMetadataImpl fmd = new FieldMetadataImpl(internalFmd);
        fmd.parent = this;
        return fmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.EmbeddedMetadata#newPropertyMetadata(java.lang.String)
     */
    public PropertyMetadata newPropertyMetadata(String name)
    {
        PropertyMetaData internalPmd = getInternal().newPropertyMetaData(name);
        PropertyMetadataImpl pmd = new PropertyMetadataImpl(internalPmd);
        pmd.parent = this;
        return pmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.EmbeddedMetadata#setNullIndicatorColumn(java.lang.String)
     */
    public EmbeddedMetadata setNullIndicatorColumn(String col)
    {
        getInternal().setNullIndicatorColumn(col);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.EmbeddedMetadata#setNullIndicatorValue(java.lang.String)
     */
    public EmbeddedMetadata setNullIndicatorValue(String value)
    {
        getInternal().setNullIndicatorValue(value);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.EmbeddedMetadata#setOwnerMember(java.lang.String)
     */
    public EmbeddedMetadata setOwnerMember(String member)
    {
        getInternal().setOwnerMember(member);
        return this;
    }

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

    public DiscriminatorMetadata newDiscriminatorMetadata()
    {
        DiscriminatorMetaData internalDismd = getInternal().newDiscriminatorMetaData();
        DiscriminatorMetadataImpl dismd = new DiscriminatorMetadataImpl(internalDismd);
        dismd.parent = this;
        return dismd;
    }
}