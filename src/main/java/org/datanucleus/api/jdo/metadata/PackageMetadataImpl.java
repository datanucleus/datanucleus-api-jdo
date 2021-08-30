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

import javax.jdo.JDOUserException;
import javax.jdo.annotations.SequenceStrategy;
import javax.jdo.metadata.ClassMetadata;
import javax.jdo.metadata.InterfaceMetadata;
import javax.jdo.metadata.PackageMetadata;
import javax.jdo.metadata.SequenceMetadata;

import org.datanucleus.metadata.ClassMetaData;
import org.datanucleus.metadata.InterfaceMetaData;
import org.datanucleus.metadata.PackageMetaData;
import org.datanucleus.metadata.SequenceMetaData;
import org.datanucleus.util.ClassUtils;

/**
 * Implementation of JDO PackageMetadata object.
 */
public class PackageMetadataImpl extends AbstractMetadataImpl implements PackageMetadata
{
    public PackageMetadataImpl(PackageMetaData pmd)
    {
        super(pmd);
    }

    public PackageMetaData getInternal()
    {
        return (PackageMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#getName()
     */
    public String getName()
    {
        return getInternal().getName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#getCatalog()
     */
    public String getCatalog()
    {
        return getInternal().getCatalog();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#setCatalog(java.lang.String)
     */
    public PackageMetadata setCatalog(String cat)
    {
        getInternal().setCatalog(cat);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#getSchema()
     */
    public String getSchema()
    {
        return getInternal().getSchema();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#setSchema(java.lang.String)
     */
    public PackageMetadata setSchema(String sch)
    {
        getInternal().setSchema(sch);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#getClasses()
     */
    public ClassMetadata[] getClasses()
    {
        ClassMetadataImpl[] classes = new ClassMetadataImpl[getNumberOfClasses()];
        for (int i=0;i<classes.length;i++)
        {
            classes[i] = new ClassMetadataImpl(getInternal().getClass(i));
            classes[i].parent = this;
        }
        return classes;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#getNumberOfClasses()
     */
    public int getNumberOfClasses()
    {
        return getInternal().getNoOfClasses();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#newClassMetadata(java.lang.String)
     */
    public ClassMetadata newClassMetadata(String name)
    {
        ClassMetaData internalCmd = getInternal().newClassMetaData(name);
        ClassMetadataImpl cmd = new ClassMetadataImpl(internalCmd);
        cmd.parent = this;
        return cmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#newClassMetadata(java.lang.Class)
     */
    public ClassMetadata newClassMetadata(Class cls)
    {
        if (cls.isInterface())
        {
            throw new JDOUserException("Canot create new class metadata for " + cls.getName() + " since it is an interface!");
        }
        ClassMetaData internalCmd = getInternal().newClassMetaData(ClassUtils.getClassNameForClass(cls));
        ClassMetadataImpl cmd = new ClassMetadataImpl(internalCmd);
        cmd.parent = this;
        return cmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#getInterfaces()
     */
    public InterfaceMetadata[] getInterfaces()
    {
        InterfaceMetadataImpl[] interfaces = new InterfaceMetadataImpl[getNumberOfInterfaces()];
        for (int i=0;i<interfaces.length;i++)
        {
            interfaces[i] = new InterfaceMetadataImpl(getInternal().getInterface(i));
            interfaces[i].parent = this;
        }
        return interfaces;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#getNumberOfInterfaces()
     */
    public int getNumberOfInterfaces()
    {
        return getInternal().getNoOfInterfaces();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#newInterfaceMetadata(java.lang.String)
     */
    public InterfaceMetadata newInterfaceMetadata(String name)
    {
        InterfaceMetaData internalImd = getInternal().newInterfaceMetaData(name);
        InterfaceMetadataImpl imd = new InterfaceMetadataImpl(internalImd);
        imd.parent = this;
        return imd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#newInterfaceMetadata(java.lang.Class)
     */
    public InterfaceMetadata newInterfaceMetadata(Class cls)
    {
        if (!cls.isInterface())
        {
            throw new JDOUserException("Canot create new interface metadata for " + cls.getName() + " since not interface!");
        }
        InterfaceMetaData internalImd = getInternal().newInterfaceMetaData(ClassUtils.getClassNameForClass(cls));
        InterfaceMetadataImpl imd = new InterfaceMetadataImpl(internalImd);
        imd.parent = this;
        return imd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#getSequences()
     */
    public SequenceMetadata[] getSequences()
    {
        SequenceMetaData[] internalSeqmds = getInternal().getSequences();
        if (internalSeqmds == null)
        {
            return null;
        }

        SequenceMetadataImpl[] seqmds = new SequenceMetadataImpl[internalSeqmds.length];
        for (int i=0;i<seqmds.length;i++)
        {
            seqmds[i] = new SequenceMetadataImpl(internalSeqmds[i]);
            seqmds[i].parent = this;
        }
        return seqmds;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#getNumberOfSequences()
     */
    public int getNumberOfSequences()
    {
        return getInternal().getNoOfSequences();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.PackageMetadata#newSequenceMetadata(java.lang.String, javax.jdo.annotations.SequenceStrategy)
     */
    public SequenceMetadata newSequenceMetadata(String name, SequenceStrategy strategy)
    {
        String str = null;
        if (strategy == SequenceStrategy.CONTIGUOUS)
        {
            str = org.datanucleus.metadata.SequenceStrategy.CONTIGUOUS.toString();
        }
        else if (strategy == SequenceStrategy.NONCONTIGUOUS)
        {
            str = org.datanucleus.metadata.SequenceStrategy.NONCONTIGUOUS.toString();
        }
        else if (strategy == SequenceStrategy.NONTRANSACTIONAL)
        {
            str = org.datanucleus.metadata.SequenceStrategy.NONTRANSACTIONAL.toString();
        }
        SequenceMetaData internalSeqmd = getInternal().newSequenceMetaData(name, str);
        SequenceMetadataImpl seqmd = new SequenceMetadataImpl(internalSeqmd);
        seqmd.parent = this;
        return seqmd;
    }

    public AbstractMetadataImpl getParent()
    {
        if (parent == null)
        {
            parent = new JDOMetadataImpl(((PackageMetaData)internalMD).getFileMetaData());
        }
        return super.getParent();
    }
}