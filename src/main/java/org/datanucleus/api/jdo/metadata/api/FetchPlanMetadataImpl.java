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

import javax.jdo.metadata.FetchGroupMetadata;
import javax.jdo.metadata.FetchPlanMetadata;

import org.datanucleus.metadata.FetchGroupMetaData;
import org.datanucleus.metadata.FetchPlanMetaData;

/**
 * Implementation of JDO FetchPlanMetadata object.
 */
public class FetchPlanMetadataImpl extends AbstractMetadataImpl implements FetchPlanMetadata
{
    public FetchPlanMetadataImpl(FetchPlanMetaData fpmd)
    {
        super(fpmd);
    }

    public FetchPlanMetaData getInternal()
    {
        return (FetchPlanMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchPlanMetadata#getFetchGroups()
     */
    public FetchGroupMetadata[] getFetchGroups()
    {
        FetchGroupMetaData[] baseData = getInternal().getFetchGroupMetaData();
        if (baseData == null)
        {
            return null;
        }

        FetchGroupMetadataImpl[] fgs = new FetchGroupMetadataImpl[baseData.length];
        for (int i=0;i<fgs.length;i++)
        {
            fgs[i] = new FetchGroupMetadataImpl(baseData[i]);
            fgs[i].parent = this;
        }
        return fgs;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchPlanMetadata#getNumberOfFetchGroups()
     */
    public int getNumberOfFetchGroups()
    {
        return getInternal().getNumberOfFetchGroups();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchPlanMetadata#newFetchGroupMetadata(java.lang.String)
     */
    public FetchGroupMetadata newFetchGroupMetadata(String name)
    {
        FetchGroupMetaData internalFgmd = getInternal().newFetchGroupMetaData(name);
        FetchGroupMetadataImpl fgmd = new FetchGroupMetadataImpl(internalFgmd);
        fgmd.parent = this;
        return fgmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchPlanMetadata#getName()
     */
    public String getName()
    {
        return getInternal().getName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchPlanMetadata#getFetchSize()
     */
    public int getFetchSize()
    {
        return getInternal().getFetchSize();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchPlanMetadata#setFetchSize(int)
     */
    public FetchPlanMetadata setFetchSize(int size)
    {
        getInternal().setFetchSize(size);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchPlanMetadata#getMaxFetchDepth()
     */
    public int getMaxFetchDepth()
    {
        return getInternal().getMaxFetchDepth();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.FetchPlanMetadata#setMaxFetchDepth(int)
     */
    public FetchPlanMetadata setMaxFetchDepth(int depth)
    {
        getInternal().setMaxFetchDepth(depth);
        return this;
    }
}