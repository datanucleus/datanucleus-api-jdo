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

import java.lang.reflect.Field;

import javax.jdo.metadata.ClassMetadata;
import javax.jdo.metadata.ClassPersistenceModifier;
import javax.jdo.metadata.FieldMetadata;

import org.datanucleus.metadata.ClassMetaData;
import org.datanucleus.metadata.FieldMetaData;

/**
 * Implementation of JDO ClassMetadata object.
 */
public class ClassMetadataImpl extends TypeMetadataImpl implements ClassMetadata
{
    public ClassMetadataImpl(ClassMetaData internal)
    {
        super(internal);
    }

    public ClassMetaData getInternal()
    {
        return (ClassMetaData)internalMD;
    }

    public ClassPersistenceModifier getPersistenceModifier()
    {
        org.datanucleus.metadata.ClassPersistenceModifier mod = getInternal().getPersistenceModifier();
        if (mod == org.datanucleus.metadata.ClassPersistenceModifier.PERSISTENCE_CAPABLE)
        {
            return ClassPersistenceModifier.PERSISTENCE_CAPABLE;
        }
        else if (mod == org.datanucleus.metadata.ClassPersistenceModifier.PERSISTENCE_AWARE)
        {
            return ClassPersistenceModifier.PERSISTENCE_AWARE;
        }
        else
        {
            return ClassPersistenceModifier.NON_PERSISTENT;
        }
    }

    public ClassMetadata setPersistenceModifier(ClassPersistenceModifier mod)
    {
        if (mod == ClassPersistenceModifier.PERSISTENCE_CAPABLE)
        {
            getInternal().setPersistenceModifier(org.datanucleus.metadata.ClassPersistenceModifier.PERSISTENCE_CAPABLE);
        }
        else if (mod == ClassPersistenceModifier.PERSISTENCE_AWARE)
        {
            getInternal().setPersistenceModifier(org.datanucleus.metadata.ClassPersistenceModifier.PERSISTENCE_AWARE);
        }
        else if (mod == ClassPersistenceModifier.NON_PERSISTENT)
        {
            getInternal().setPersistenceModifier(org.datanucleus.metadata.ClassPersistenceModifier.NON_PERSISTENT);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ClassMetadata#newFieldMetadata(java.lang.String)
     */
    public FieldMetadata newFieldMetadata(String name)
    {
        FieldMetaData internalFmd = getInternal().newFieldMetaData(name);
        FieldMetadataImpl fmd = new FieldMetadataImpl(internalFmd);
        fmd.parent = this;
        return fmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ClassMetadata#newFieldMetadata(java.lang.reflect.Field)
     */
    public FieldMetadata newFieldMetadata(Field fld)
    {
        FieldMetaData internalFmd = getInternal().newFieldMetaData(fld.getName());
        FieldMetadataImpl fmd = new FieldMetadataImpl(internalFmd);
        fmd.parent = this;
        return fmd;
    }

    public AbstractMetadataImpl getParent()
    {
        if (parent == null)
        {
            parent = new PackageMetadataImpl(((ClassMetaData)internalMD).getPackageMetaData());
        }
        return super.getParent();
    }
}