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

import javax.jdo.metadata.CollectionMetadata;

import org.datanucleus.metadata.CollectionMetaData;

/**
 * Implementation of JDO CollectionMetadata object.
 */
public class CollectionMetadataImpl extends AbstractMetadataImpl implements CollectionMetadata
{
    public CollectionMetadataImpl(CollectionMetaData internal)
    {
        super(internal);
    }

    public CollectionMetaData getInternal()
    {
        return (CollectionMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.CollectionMetadata#getDependentElement()
     */
    public Boolean getDependentElement()
    {
        return getInternal().isDependentElement();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.CollectionMetadata#getElementType()
     */
    public String getElementType()
    {
        return getInternal().getElementType();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.CollectionMetadata#getEmbeddedElement()
     */
    public Boolean getEmbeddedElement()
    {
        return getInternal().isEmbeddedElement();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.CollectionMetadata#getSerializedElement()
     */
    public Boolean getSerializedElement()
    {
        return getInternal().isSerializedElement();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.CollectionMetadata#setDependentElement(boolean)
     */
    public CollectionMetadata setDependentElement(boolean flag)
    {
        getInternal().setDependentElement(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.CollectionMetadata#setElementType(java.lang.String)
     */
    public CollectionMetadata setElementType(String type)
    {
        getInternal().setElementType(type);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.CollectionMetadata#setEmbeddedElement(boolean)
     */
    public CollectionMetadata setEmbeddedElement(boolean flag)
    {
        getInternal().setEmbeddedElement(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.CollectionMetadata#setSerializedElement(boolean)
     */
    public CollectionMetadata setSerializedElement(boolean flag)
    {
        getInternal().setSerializedElement(flag);
        return this;
    }
}