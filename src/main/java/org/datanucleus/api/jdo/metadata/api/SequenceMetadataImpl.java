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

import javax.jdo.annotations.SequenceStrategy;
import javax.jdo.metadata.SequenceMetadata;

import org.datanucleus.metadata.SequenceMetaData;

/**
 * Implementation of JDO SequenceMetadata object.
 */
public class SequenceMetadataImpl extends AbstractMetadataImpl implements SequenceMetadata
{
    public SequenceMetadataImpl(SequenceMetaData internal)
    {
        super(internal);
    }

    public SequenceMetaData getInternal()
    {
        return (SequenceMetaData)internalMD;
    }

    public Integer getAllocationSize()
    {
        return getInternal().getAllocationSize();
    }

    public Integer getInitialValue()
    {
        return getInternal().getInitialValue();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.SequenceMetadata#getDatastoreSequence()
     */
    public String getDatastoreSequence()
    {
        return getInternal().getDatastoreSequence();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.SequenceMetadata#getFactoryClass()
     */
    public String getFactoryClass()
    {
        return getInternal().getFactoryClass();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.SequenceMetadata#getName()
     */
    public String getName()
    {
        return getInternal().getName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.SequenceMetadata#getSequenceStrategy()
     */
    public SequenceStrategy getSequenceStrategy()
    {
        org.datanucleus.metadata.SequenceStrategy strategy = getInternal().getStrategy();
        if (strategy == org.datanucleus.metadata.SequenceStrategy.CONTIGUOUS)
        {
            return SequenceStrategy.CONTIGUOUS;
        }
        else if (strategy == org.datanucleus.metadata.SequenceStrategy.NONCONTIGUOUS)
        {
            return SequenceStrategy.NONCONTIGUOUS;
        }
        else if (strategy == org.datanucleus.metadata.SequenceStrategy.NONTRANSACTIONAL)
        {
            return SequenceStrategy.NONTRANSACTIONAL;
        }
        return null;
    }

    public SequenceMetadata setAllocationSize(int size)
    {
        getInternal().setAllocationSize(size);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.SequenceMetadata#setDatastoreSequence(java.lang.String)
     */
    public SequenceMetadata setDatastoreSequence(String seq)
    {
        getInternal().setDatastoreSequence(seq);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.SequenceMetadata#setFactoryClass(java.lang.String)
     */
    public SequenceMetadata setFactoryClass(String cls)
    {
        getInternal().setFactoryClass(cls);
        return this;
    }

    public SequenceMetadata setInitialValue(int value)
    {
        getInternal().setInitialValue(value);
        return this;
    }
}