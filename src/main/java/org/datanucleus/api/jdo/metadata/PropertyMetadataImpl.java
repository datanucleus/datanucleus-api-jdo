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

import javax.jdo.metadata.PropertyMetadata;

import org.datanucleus.metadata.FetchGroupMemberMetaData;
import org.datanucleus.metadata.PropertyMetaData;

/**
 * Implementation of JDO PropertyMetadata object.
 */
public class PropertyMetadataImpl extends MemberMetadataImpl implements PropertyMetadata
{
    public PropertyMetadataImpl(PropertyMetaData internal)
    {
        super(internal);
    }

    public PropertyMetadataImpl(FetchGroupMemberMetaData internal)
    {
        super(internal);
    }

    public PropertyMetaData getInternal()
    {
        return (PropertyMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PropertyMetadata#getFieldName()
     */
    public String getFieldName()
    {
        return getInternal().getFieldName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PropertyMetadata#setFieldName(java.lang.String)
     */
    public PropertyMetadata setFieldName(String name)
    {
        getInternal().setFieldName(name);
        return this;
    }
}