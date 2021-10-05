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

import javax.jdo.metadata.MapMetadata;

import org.datanucleus.metadata.MapMetaData;

/**
 * Implementation of JDO MapMetadata object.
 */
public class MapMetadataImpl extends AbstractMetadataImpl implements MapMetadata
{
    public MapMetadataImpl(MapMetaData internal)
    {
        super(internal);
    }

    public MapMetaData getInternal()
    {
        return (MapMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#getDependentKey()
     */
    public Boolean getDependentKey()
    {
        return getInternal().isDependentKey();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#getDependentValue()
     */
    public Boolean getDependentValue()
    {
        return getInternal().isDependentValue();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#getEmbeddedKey()
     */
    public Boolean getEmbeddedKey()
    {
        return getInternal().isEmbeddedKey();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#getEmbeddedValue()
     */
    public Boolean getEmbeddedValue()
    {
        return getInternal().isEmbeddedValue();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#getKeyType()
     */
    public String getKeyType()
    {
        return getInternal().getKeyType();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#getSerializedKey()
     */
    public Boolean getSerializedKey()
    {
        return getInternal().isSerializedKey();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#getSerializedValue()
     */
    public Boolean getSerializedValue()
    {
        return getInternal().isSerializedValue();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#getValueType()
     */
    public String getValueType()
    {
        return getInternal().getValueType();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#setDependentKey(boolean)
     */
    public MapMetadata setDependentKey(boolean flag)
    {
        getInternal().setDependentKey(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#setDependentValue(boolean)
     */
    public MapMetadata setDependentValue(boolean flag)
    {
        getInternal().setDependentValue(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#setEmbeddedKey(boolean)
     */
    public MapMetadata setEmbeddedKey(boolean flag)
    {
        getInternal().setEmbeddedKey(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#setEmbeddedValue(boolean)
     */
    public MapMetadata setEmbeddedValue(boolean flag)
    {
        getInternal().setEmbeddedValue(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#setKeyType(java.lang.String)
     */
    public MapMetadata setKeyType(String type)
    {
        getInternal().setKeyType(type);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#setSerializedKey(boolean)
     */
    public MapMetadata setSerializedKey(boolean flag)
    {
        getInternal().setSerializedKey(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#setSerializedValue(boolean)
     */
    public MapMetadata setSerializedValue(boolean flag)
    {
        getInternal().setSerializedValue(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.MapMetadata#setValueType(java.lang.String)
     */
    public MapMetadata setValueType(String type)
    {
        getInternal().setValueType(type);
        return this;
    }
}