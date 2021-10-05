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

import javax.jdo.metadata.ArrayMetadata;

import org.datanucleus.metadata.ArrayMetaData;

/**
 * Implementation of JDO ArrayMetadata object.
 */
public class ArrayMetadataImpl extends AbstractMetadataImpl implements ArrayMetadata
{
    public ArrayMetadataImpl(ArrayMetaData internal)
    {
        super(internal);
    }

    public ArrayMetaData getInternal()
    {
        return (ArrayMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ArrayMetadata#getDependentElement()
     */
    public Boolean getDependentElement()
    {
        return getInternal().isDependentElement();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ArrayMetadata#getElementType()
     */
    public String getElementType()
    {
        return getInternal().getElementType();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ArrayMetadata#getEmbeddedElement()
     */
    public Boolean getEmbeddedElement()
    {
        return getInternal().isEmbeddedElement();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ArrayMetadata#getSerializedElement()
     */
    public Boolean getSerializedElement()
    {
        return getInternal().isSerializedElement();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ArrayMetadata#setDependentElement(boolean)
     */
    public ArrayMetadata setDependentElement(boolean flag)
    {
        getInternal().setDependentElement(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ArrayMetadata#setElementType(java.lang.String)
     */
    public ArrayMetadata setElementType(String type)
    {
        getInternal().setElementType(type);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ArrayMetadata#setEmbeddedElement(boolean)
     */
    public ArrayMetadata setEmbeddedElement(boolean flag)
    {
        getInternal().setEmbeddedElement(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ArrayMetadata#setSerializedElement(boolean)
     */
    public ArrayMetadata setSerializedElement(boolean flag)
    {
        getInternal().setSerializedElement(flag);
        return this;
    }
}