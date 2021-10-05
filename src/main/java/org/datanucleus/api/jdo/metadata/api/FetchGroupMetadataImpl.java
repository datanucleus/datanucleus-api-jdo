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

import java.util.Set;

import javax.jdo.metadata.FetchGroupMetadata;
import javax.jdo.metadata.FieldMetadata;
import javax.jdo.metadata.MemberMetadata;
import javax.jdo.metadata.PropertyMetadata;

import org.datanucleus.metadata.FetchGroupMemberMetaData;
import org.datanucleus.metadata.FetchGroupMetaData;

/**
 * Implementation of JDO FetchGroupMetadata object.
 */
public class FetchGroupMetadataImpl extends AbstractMetadataImpl implements FetchGroupMetadata
{
    public FetchGroupMetadataImpl(FetchGroupMetaData fgmd)
    {
        super(fgmd);
    }

    public FetchGroupMetaData getInternal()
    {
        return (FetchGroupMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchGroupMetadata#getName()
     */
    public String getName()
    {
        return getInternal().getName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchGroupMetadata#getMembers()
     */
    public MemberMetadata[] getMembers()
    {
        Set<FetchGroupMemberMetaData> internalMmds = getInternal().getMembers();
        if (internalMmds == null)
        {
            return null;
        }
        MemberMetadataImpl[] mmds = new MemberMetadataImpl[internalMmds.size()];
        int i = 0;
        for (FetchGroupMemberMetaData fgmmd : internalMmds)
        {
            if (fgmmd.isProperty())
            {
                mmds[i] = new PropertyMetadataImpl(fgmmd);
            }
            else
            {
                mmds[i] = new FieldMetadataImpl(fgmmd);
            }
            mmds[i].parent = this;
            i++;
        }
        return mmds;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchGroupMetadata#getNumberOfMembers()
     */
    public int getNumberOfMembers()
    {
        return getInternal().getNumberOfMembers();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchGroupMetadata#newFieldMetadata(java.lang.String)
     */
    public FieldMetadata newFieldMetadata(String name)
    {
        FetchGroupMemberMetaData internalFgMmd = getInternal().newMemberMetaData(name);
        FieldMetadataImpl fmd = new FieldMetadataImpl(internalFgMmd);
        fmd.parent = this;
        return fmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchGroupMetadata#newPropertyMetadata(java.lang.String)
     */
    public PropertyMetadata newPropertyMetadata(String name)
    {
        FetchGroupMemberMetaData internalFgMmd = getInternal().newMemberMetaData(name);
        PropertyMetadataImpl pmd = new PropertyMetadataImpl(internalFgMmd);
        internalFgMmd.setProperty();
        pmd.parent = this;
        return pmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchGroupMetadata#getPostLoad()
     */
    public Boolean getPostLoad()
    {
        return getInternal().getPostLoad();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchGroupMetadata#setPostLoad(boolean)
     */
    public FetchGroupMetadata setPostLoad(boolean load)
    {
        getInternal().setPostLoad(load);
        return this;
    }
}