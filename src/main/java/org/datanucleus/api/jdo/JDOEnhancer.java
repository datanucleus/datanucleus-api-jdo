/**********************************************************************
Copyright (c) 2008 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.api.jdo;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Properties;

import org.datanucleus.api.jdo.metadata.JDOMetadataImpl;
import org.datanucleus.enhancer.DataNucleusEnhancer;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.metadata.FileMetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.util.NucleusLogger;

/**
 * Implementation of a JDO-compliant Enhancer.
 */
public class JDOEnhancer implements javax.jdo.JDOEnhancer
{
    DataNucleusEnhancer enhancer;

    /**
     * Constructor for a JDO enhancer.
     */
    public JDOEnhancer()
    {
        enhancer = new DataNucleusEnhancer("JDO", null);
    }

    /**
     * Constructor for a JDO enhancer specifying optional properties.
     * @param props Properties
     */
    public JDOEnhancer(Properties props)
    {
        enhancer = new DataNucleusEnhancer("JDO", props);
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#newMetadata()
     */
    public javax.jdo.metadata.JDOMetadata newMetadata()
    {
        return new JDOMetadataImpl();
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#registerMetadata(javax.jdo.metadata.JDOMetadata)
     */
    public void registerMetadata(javax.jdo.metadata.JDOMetadata metadata)
    {
        MetaDataManager mmgr = enhancer.getMetaDataManager();
        FileMetaData filemd = ((JDOMetadataImpl)metadata).getInternal();
        mmgr.loadUserMetaData(filemd, enhancer.getClassLoader());
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#addClass(java.lang.String, byte[])
     */
    public JDOEnhancer addClass(String className, byte[] bytes)
    {
        enhancer.addClass(className, bytes);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#addClasses(java.lang.String[])
     */
    public JDOEnhancer addClasses(String... classNames)
    {
        enhancer.addClasses(classNames);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#addFiles(java.lang.String[])
     */
    public JDOEnhancer addFiles(String... metadataFiles)
    {
        enhancer.addFiles(metadataFiles);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#addJar(java.lang.String)
     */
    public JDOEnhancer addJar(String jarFileName)
    {
        enhancer.addJar(jarFileName);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#addPersistenceUnit(java.lang.String)
     */
    public JDOEnhancer addPersistenceUnit(String persistenceUnitName)
    {
        enhancer.addPersistenceUnit(persistenceUnitName);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#enhance()
     */
    public int enhance()
    {
        try
        {
            return enhancer.enhance();
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#getEnhancedBytes(java.lang.String)
     */
    public byte[] getEnhancedBytes(String className)
    {
        try
        {
            return enhancer.getEnhancedBytes(className);
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * DataNucleus extension returning the bytes of the generated primary key class (if any)
     * for the specified persistent class.
     * @param className Name of the persistent class
     * @return Bytes of generated PK class (if any)
     */
    public byte[] getPkClassBytes(String className)
    {
        try
        {
            return enhancer.getPkClassBytes(className);
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#getProperties()
     */
    public Properties getProperties()
    {
        return enhancer.getProperties();
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#setClassLoader(java.lang.ClassLoader)
     */
    public JDOEnhancer setClassLoader(ClassLoader loader)
    {
        enhancer.setClassLoader(loader);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#setOutputDirectory(java.lang.String)
     */
    public JDOEnhancer setOutputDirectory(String dir)
    {
        enhancer.setOutputDirectory(dir);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#setVerbose(boolean)
     */
    public JDOEnhancer setVerbose(boolean verbose)
    {
        enhancer.setVerbose(verbose);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.JDOEnhancer#validate()
     */
    public int validate()
    {
        try
        {
            return enhancer.validate();
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader, java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
     */
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) 
    throws IllegalClassFormatException
    {
        NucleusLogger.GENERAL.warn("JDOEnhancer.transform not implemented. Report where this was called from", new Exception());
        return null;
    }
}