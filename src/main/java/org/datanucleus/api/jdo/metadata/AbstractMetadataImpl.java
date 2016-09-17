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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.jdo.metadata.ExtensionMetadata;
import javax.jdo.metadata.Metadata;

import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.FileMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.metadata.PackageMetaData;

/**
 * Base for all implementations of JDO Metadata classes. Provides parentage, and the underlying
 * internal metadata component used by DataNucleus. Also provides extension handling.
 */
public abstract class AbstractMetadataImpl implements Metadata
{
    /** Link to parent wrapper. */
    AbstractMetadataImpl parent;

    /** DataNucleus internal MetaData object backing this. */
    MetaData internalMD;

    public AbstractMetadataImpl(MetaData internal)
    {
        this.internalMD = internal;
    }

    public String toString()
    {
        if (internalMD instanceof FileMetaData)
        {
            return new JDOMetaDataHelper().getXMLForMetaData((FileMetaData)internalMD, "", "    ");
        }
        else if (internalMD instanceof PackageMetaData)
        {
            return new JDOMetaDataHelper().getXMLForMetaData((PackageMetaData)internalMD, "", "    ");
        }
        else if (internalMD instanceof AbstractClassMetaData)
        {
            return new JDOMetaDataHelper().getXMLForMetaData((AbstractClassMetaData)internalMD, "", "    ");
        }
        return super.toString();
    }

    public ExtensionMetadata[] getExtensions()
    {
        Map<String, String> exts = internalMD.getExtensions();
        if (exts == null)
        {
            return null;
        }

        ExtensionMetadata[] extensions = new ExtensionMetadata[exts.size()];
        Iterator<Entry<String, String>> entryIter = exts.entrySet().iterator();
        int i = 0;
        while (entryIter.hasNext())
        {
            Entry<String, String> entry = entryIter.next();
            extensions[i++] = new ExtensionMetadataImpl(MetaData.VENDOR_NAME, entry.getKey(), entry.getValue());
        }
        return extensions;
    }

    public int getNumberOfExtensions()
    {
        return internalMD.getNoOfExtensions();
    }

    public ExtensionMetadata newExtensionMetadata(String vendor, String key, String value)
    {
        // Create new backing extension, and wrap it for returning
        ExtensionMetadata extmd = new ExtensionMetadataImpl(vendor, key, value);
        if (vendor.equals(MetaData.VENDOR_NAME))
        {
            internalMD.addExtension(key, value);
        }
        return extmd;
    }

    public AbstractMetadataImpl getParent()
    {
        return parent;
    }
}