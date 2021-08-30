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

import javax.jdo.metadata.ClassMetadata;
import javax.jdo.metadata.FetchPlanMetadata;
import javax.jdo.metadata.InterfaceMetadata;
import javax.jdo.metadata.JDOMetadata;
import javax.jdo.metadata.PackageMetadata;
import javax.jdo.metadata.QueryMetadata;

import org.datanucleus.metadata.ClassMetaData;
import org.datanucleus.metadata.FetchPlanMetaData;
import org.datanucleus.metadata.FileMetaData;
import org.datanucleus.metadata.InterfaceMetaData;
import org.datanucleus.metadata.MetaDataFileType;
import org.datanucleus.metadata.PackageMetaData;
import org.datanucleus.metadata.QueryMetaData;
import org.datanucleus.util.ClassUtils;

/**
 * Implementation of JDOMetadata object.
 */
public class JDOMetadataImpl extends AbstractMetadataImpl implements JDOMetadata
{
    public JDOMetadataImpl()
    {
        super(new FileMetaData());
        getInternal().setType(MetaDataFileType.JDO_FILE);
    }

    public JDOMetadataImpl(FileMetaData filemd)
    {
        super(filemd);
    }

    public FileMetaData getInternal()
    {
        return (FileMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#getFetchPlans()
     */
    public FetchPlanMetadata[] getFetchPlans()
    {
        FetchPlanMetaData[] baseFps = getInternal().getFetchPlans();
        if (baseFps == null)
        {
            return null;
        }

        FetchPlanMetadataImpl[] fps = new FetchPlanMetadataImpl[baseFps.length];
        for (int i=0;i<fps.length;i++)
        {
            fps[i] = new FetchPlanMetadataImpl(baseFps[i]);
            fps[i].parent = this;
        }
        return fps;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#newFetchPlanMetadata(java.lang.String)
     */
    public FetchPlanMetadata newFetchPlanMetadata(String name)
    {
        FetchPlanMetaData internalFpmd = getInternal().newFetchPlanMetaData(name);
        FetchPlanMetadataImpl fpmd = new FetchPlanMetadataImpl(internalFpmd);
        fpmd.parent = this;
        return fpmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#getNumberOfFetchPlans()
     */
    public int getNumberOfFetchPlans()
    {
        return getInternal().getNoOfFetchPlans();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#getQueries()
     */
    public QueryMetadata[] getQueries()
    {
        QueryMetaData[] baseQueries = getInternal().getQueries();
        if (baseQueries == null)
        {
            return null;
        }

        QueryMetadataImpl[] queries = new QueryMetadataImpl[getInternal().getNoOfQueries()];
        for (int i=0;i<queries.length;i++)
        {
            queries[i] = new QueryMetadataImpl(baseQueries[i]);
            queries[i].parent = this;
        }
        return queries;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#getNumberOfQueries()
     */
    public int getNumberOfQueries()
    {
        return getInternal().getNoOfQueries();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#newQueryMetadata(java.lang.String)
     */
    public QueryMetadata newQueryMetadata(String name)
    {
        QueryMetaData internalQmd = getInternal().newQueryMetaData(name);
        QueryMetadataImpl qmd = new QueryMetadataImpl(internalQmd);
        qmd.parent = this;
        return qmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#getPackages()
     */
    public PackageMetadata[] getPackages()
    {
        PackageMetadataImpl[] pmds = new PackageMetadataImpl[getInternal().getNoOfPackages()];
        for (int i=0;i<pmds.length;i++)
        {
            pmds[i] = new PackageMetadataImpl(getInternal().getPackage(i));
            pmds[i].parent = this;
        }
        return pmds;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#getNumberOfPackages()
     */
    public int getNumberOfPackages()
    {
        return getInternal().getNoOfPackages();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#newPackageMetadata(java.lang.String)
     */
    public PackageMetadata newPackageMetadata(String name)
    {
        PackageMetaData internalPmd = getInternal().newPackageMetaData(name);
        PackageMetadataImpl pmd = new PackageMetadataImpl(internalPmd);
        pmd.parent = this;
        return pmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#newPackageMetadata(java.lang.Package)
     */
    public PackageMetadata newPackageMetadata(Package pkg)
    {
        PackageMetaData internalPmd = getInternal().newPackageMetaData(pkg.getName());
        PackageMetadataImpl pmd = new PackageMetadataImpl(internalPmd);
        pmd.parent = this;
        return pmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#newClassMetadata(java.lang.Class)
     */
    public ClassMetadata newClassMetadata(Class cls)
    {
        String packageName = ClassUtils.getPackageNameForClass(cls);
        PackageMetaData internalPmd = getInternal().newPackageMetaData(packageName); // Adds if necessary
        PackageMetadataImpl pmd = new PackageMetadataImpl(internalPmd);
        pmd.parent = this;

        String className = ClassUtils.getClassNameForClass(cls);
        ClassMetaData internalCmd = internalPmd.newClassMetaData(className);
        ClassMetadataImpl cmd = new ClassMetadataImpl(internalCmd);
        cmd.parent = pmd;
        return cmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#newInterfaceMetadata(java.lang.Class)
     */
    public InterfaceMetadata newInterfaceMetadata(Class cls)
    {
        String packageName = ClassUtils.getPackageNameForClass(cls);
        PackageMetaData internalPmd = getInternal().newPackageMetaData(packageName); // Adds if necessary
        PackageMetadataImpl pmd = new PackageMetadataImpl(internalPmd);
        pmd.parent = this;

        String className = ClassUtils.getClassNameForClass(cls);
        InterfaceMetaData internalImd = internalPmd.newInterfaceMetaData(className);
        InterfaceMetadataImpl imd = new InterfaceMetadataImpl(internalImd);
        imd.parent = pmd;
        return imd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#getCatalog()
     */
    public String getCatalog()
    {
        return getInternal().getCatalog();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#setCatalog(java.lang.String)
     */
    public JDOMetadata setCatalog(String cat)
    {
        getInternal().setCatalog(cat);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#getSchema()
     */
    public String getSchema()
    {
        return getInternal().getSchema();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.JDOMetadata#setSchema(java.lang.String)
     */
    public JDOMetadata setSchema(String sch)
    {
        getInternal().setSchema(sch);
        return this;
    }
}