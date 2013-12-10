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

import javax.jdo.metadata.ExtensionMetadata;

import org.datanucleus.metadata.ExtensionMetaData;

/**
 * Implementation of JDO ExtensionMetadata object.
 */
public class ExtensionMetadataImpl implements ExtensionMetadata
{
    ExtensionMetaData extmd;

    public ExtensionMetadataImpl(String vendor, String key, String value)
    {
        extmd = new ExtensionMetaData(vendor, key, value);
    }

    public ExtensionMetadataImpl(ExtensionMetaData extmd)
    {
        this.extmd = extmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ExtensionMetadata#getKey()
     */
    public String getKey()
    {
        return extmd.getKey();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ExtensionMetadata#getValue()
     */
    public String getValue()
    {
        return extmd.getValue();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ExtensionMetadata#getVendorName()
     */
    public String getVendorName()
    {
        return extmd.getVendorName();
    }
}