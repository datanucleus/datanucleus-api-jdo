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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOUserException;
import javax.jdo.annotations.IdentityType;
import javax.jdo.metadata.ColumnMetadata;
import javax.jdo.metadata.DatastoreIdentityMetadata;
import javax.jdo.metadata.FetchGroupMetadata;
import javax.jdo.metadata.ForeignKeyMetadata;
import javax.jdo.metadata.IndexMetadata;
import javax.jdo.metadata.InheritanceMetadata;
import javax.jdo.metadata.JoinMetadata;
import javax.jdo.metadata.MemberMetadata;
import javax.jdo.metadata.PrimaryKeyMetadata;
import javax.jdo.metadata.PropertyMetadata;
import javax.jdo.metadata.QueryMetadata;
import javax.jdo.metadata.TypeMetadata;
import javax.jdo.metadata.UniqueMetadata;
import javax.jdo.metadata.VersionMetadata;

import org.datanucleus.api.jdo.DataNucleusHelperJDO;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.FetchGroupMetaData;
import org.datanucleus.metadata.FieldMetaData;
import org.datanucleus.metadata.ForeignKeyMetaData;
import org.datanucleus.metadata.IdentityMetaData;
import org.datanucleus.metadata.IndexMetaData;
import org.datanucleus.metadata.InheritanceMetaData;
import org.datanucleus.metadata.JoinMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.metadata.PrimaryKeyMetaData;
import org.datanucleus.metadata.PropertyMetaData;
import org.datanucleus.metadata.QueryMetaData;
import org.datanucleus.metadata.UniqueMetaData;
import org.datanucleus.metadata.VersionMetaData;
import org.datanucleus.util.StringUtils;

/**
 * Superclass for ClassMetadataImpl/InterfaceMetadataImpl so we don't duplicate.
 */
public abstract class TypeMetadataImpl extends AbstractMetadataImpl implements TypeMetadata
{
    public TypeMetadataImpl(MetaData internal)
    {
        super(internal);
    }

    public AbstractClassMetaData getInternal()
    {
        return (AbstractClassMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ComponentMetadata#getNumberOfMembers()
     */
    public int getNumberOfMembers()
    {
        return getInternal().getNoOfMembers();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ComponentMetadata#getMembers()
     */
    public MemberMetadata[] getMembers()
    {
        AbstractMemberMetaData[] internalMmds = getInternal().getManagedMembers();
        if (internalMmds == null)
        {
            return null;
        }
        MemberMetadataImpl[] mmds = new MemberMetadataImpl[internalMmds.length];
        for (int i=0;i<mmds.length;i++)
        {
            if (internalMmds[i] instanceof FieldMetaData)
            {
                mmds[i] = new FieldMetadataImpl((FieldMetaData)internalMmds[i]);
            }
            else
            {
                mmds[i] = new PropertyMetadataImpl((PropertyMetaData)internalMmds[i]);
            }
            mmds[i].parent = this;
        }
        return mmds;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ComponentMetadata#newPropertyMetadata(java.lang.String)
     */
    public PropertyMetadata newPropertyMetadata(String name)
    {
        PropertyMetaData internalPmd = getInternal().newPropertyMetadata(name);
        PropertyMetadataImpl pmd = new PropertyMetadataImpl(internalPmd);
        pmd.parent = this;
        return pmd;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ComponentMetadata#newPropertyMetadata(java.lang.reflect.Method)
     */
    public PropertyMetadata newPropertyMetadata(Method method)
    {
        String methodName = method.getName();
        String name = null;
        if (methodName.startsWith("set"))
        {
            name = methodName.substring(3);
        }
        else if (methodName.startsWith("get"))
        {
            name = methodName.substring(3);
        }
        else if (methodName.startsWith("is"))
        {
            name = methodName.substring(2);
        }
        else
        {
            throw new JDOUserException("Method " + methodName + " is not a Java-bean method");
        }
        String propertyName = name.substring(0,1).toLowerCase() + name.substring(1);
        PropertyMetaData internalPmd = getInternal().newPropertyMetadata(propertyName);
        PropertyMetadataImpl pmd = new PropertyMetadataImpl(internalPmd);
        pmd.parent = this;
        return pmd;
    }

    public boolean getCacheable()
    {
        return getInternal().isCacheable();
    }

    public String getCatalog()
    {
        return getInternal().getCatalog();
    }

    public DatastoreIdentityMetadata getDatastoreIdentityMetadata()
    {
        IdentityMetaData internalIdmd = getInternal().getIdentityMetaData();
        DatastoreIdentityMetadataImpl idmd = new DatastoreIdentityMetadataImpl(internalIdmd);
        idmd.parent = this;
        return idmd;
    }

    public boolean getDetachable()
    {
        return getInternal().isDetachable();
    }

    public Boolean getEmbeddedOnly()
    {
        return getInternal().isEmbeddedOnly();
    }

    public boolean getSerializeRead()
    {
        return getInternal().isSerializeRead();
    }

    public FetchGroupMetadata[] getFetchGroups()
    {
        Set<FetchGroupMetaData> internalFgmds = getInternal().getFetchGroupMetaData();
        if (internalFgmds == null)
        {
            return null;
        }
        FetchGroupMetadataImpl[] fgmds = new FetchGroupMetadataImpl[internalFgmds.size()];
        int i = 0;
        for (FetchGroupMetaData fgmd : internalFgmds)
        {
            fgmds[i] = new FetchGroupMetadataImpl(fgmd);
            fgmds[i].parent = this;
            i++;
        }
        return fgmds;
    }

    public IdentityType getIdentityType()
    {
        org.datanucleus.metadata.IdentityType idType = getInternal().getIdentityType();
        if (idType == org.datanucleus.metadata.IdentityType.APPLICATION)
        {
            return IdentityType.APPLICATION;
        }
        else if (idType == org.datanucleus.metadata.IdentityType.DATASTORE)
        {
            return IdentityType.DATASTORE;
        }
        else
        {
            return IdentityType.NONDURABLE;
        }
    }

    public InheritanceMetadata getInheritanceMetadata()
    {
        InheritanceMetaData internalInhmd = getInternal().getInheritanceMetaData();
        InheritanceMetadataImpl inhmd = new InheritanceMetadataImpl(internalInhmd);
        inhmd.parent = this;
        return inhmd;
    }

    public String getName()
    {
        return getInternal().getName();
    }

    public int getNumberOfFetchGroups()
    {
        Set<FetchGroupMetaData> fgmds = getInternal().getFetchGroupMetaData();
        return (fgmds != null ? fgmds.size() : 0);
    }

    public ForeignKeyMetadata[] getForeignKeys()
    {
        List<ForeignKeyMetaData> internalFKs = getInternal().getForeignKeyMetaData();
        if (internalFKs == null)
        {
            return null;
        }

        ForeignKeyMetadata[] fkmds = new ForeignKeyMetadataImpl[internalFKs.size()];
        int i=0;
        for (ForeignKeyMetaData internalFKMD : internalFKs)
        {
            ForeignKeyMetadataImpl fkmd = new ForeignKeyMetadataImpl(internalFKMD);
            fkmds[i++] = fkmd;
            fkmd.parent = this;
        }
        return fkmds;
    }

    public int getNumberOfForeignKeys()
    {
        List<ForeignKeyMetaData> fkmds = getInternal().getForeignKeyMetaData();
        return (fkmds != null ? fkmds.size() : 0);
    }

    public IndexMetadata[] getIndices()
    {
        List<IndexMetaData> internalIdxmds = getInternal().getIndexMetaData();
        if (internalIdxmds == null)
        {
            return null;
        }

        IndexMetadataImpl[] idxmds = new IndexMetadataImpl[internalIdxmds.size()];
        int i = 0;
        for (IndexMetaData internalIdxmd : internalIdxmds)
        {
            IndexMetadataImpl idxmd = new IndexMetadataImpl(internalIdxmd);
            idxmds[i++] = idxmd;
            idxmd.parent = this;
        }
        return idxmds;
    }

    public int getNumberOfIndices()
    {
        List<IndexMetaData> indexmds = getInternal().getIndexMetaData();
        return (indexmds != null ? indexmds.size() : 0);
    }

    public JoinMetadata[] getJoins()
    {
        List<JoinMetaData> internalJoins = getInternal().getJoinMetaData();
        if (internalJoins == null)
        {
            return null;
        }

        JoinMetadataImpl[] joins = new JoinMetadataImpl[internalJoins.size()];
        int i = 0;
        for (JoinMetaData internalJoinMD : internalJoins)
        {
            JoinMetadataImpl joinmd = new JoinMetadataImpl(internalJoinMD);
            joins[i++] = joinmd;
            joinmd.parent = this;
        }
        return joins;
    }

    public int getNumberOfJoins()
    {
        List<JoinMetaData> joinmds = getInternal().getJoinMetaData();
        return (joinmds != null ? joinmds.size() : 0);
    }

    public int getNumberOfQueries()
    {
        return getInternal().getNoOfQueries();
    }

    public UniqueMetadata[] getUniques()
    {
        List<UniqueMetaData> internalUnimds = getInternal().getUniqueMetaData();
        if (internalUnimds == null)
        {
            return null;
        }

        UniqueMetadataImpl[] unimds = new UniqueMetadataImpl[internalUnimds.size()];
        int i = 0;
        for (UniqueMetaData internalUniMD : internalUnimds)
        {
            UniqueMetadataImpl unimd = new UniqueMetadataImpl(internalUniMD);
            unimds[i++] = unimd;
            unimd.parent = this;
        }
        return unimds;
    }

    public int getNumberOfUniques()
    {
        List<UniqueMetaData> uniquemds = getInternal().getUniqueMetaData();
        return (uniquemds != null ? uniquemds.size() : 0);
    }

    public String getObjectIdClass()
    {
        return getInternal().getObjectidClass();
    }

    public PrimaryKeyMetadata getPrimaryKeyMetadata()
    {
        PrimaryKeyMetaData internalPkmd = getInternal().getPrimaryKeyMetaData();
        PrimaryKeyMetadataImpl pkmd = new PrimaryKeyMetadataImpl(internalPkmd);
        pkmd.parent = this;
        return pkmd;
    }

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

    public boolean getRequiresExtent()
    {
        return getInternal().isRequiresExtent();
    }

    public String getSchema()
    {
        return getInternal().getSchema();
    }

    public String getTable()
    {
        return getInternal().getTable();
    }

    public VersionMetadata getVersionMetadata()
    {
        VersionMetaData internalVermd = getInternal().getVersionMetaData();
        VersionMetadataImpl vermd = new VersionMetadataImpl(internalVermd);
        vermd.parent = this;
        return vermd;
    }

    public DatastoreIdentityMetadata newDatastoreIdentityMetadata()
    {
        IdentityMetaData idmd = getInternal().newIdentityMetadata();
        DatastoreIdentityMetadataImpl dimd = new DatastoreIdentityMetadataImpl(idmd);
        dimd.parent = this;
        return dimd;
    }

    public FetchGroupMetadata newFetchGroupMetadata(String name)
    {
        FetchGroupMetaData internalFgmd = getInternal().newFetchGroupMetaData(name);
        FetchGroupMetadataImpl fgmd = new FetchGroupMetadataImpl(internalFgmd);
        fgmd.parent = this;
        return fgmd;
    }

    public ForeignKeyMetadata newForeignKeyMetadata()
    {
        ForeignKeyMetaData internalFkmd = getInternal().newForeignKeyMetadata();
        ForeignKeyMetadataImpl fkmd = new ForeignKeyMetadataImpl(internalFkmd);
        fkmd.parent = this;
        return fkmd;
    }

    public IndexMetadata newIndexMetadata()
    {
        IndexMetaData internalIdxmd = getInternal().newIndexMetadata();
        IndexMetadataImpl idxmd = new IndexMetadataImpl(internalIdxmd);
        idxmd.parent = this;
        return idxmd;
    }

    public InheritanceMetadata newInheritanceMetadata()
    {
        InheritanceMetaData internalInhmd = getInternal().newInheritanceMetadata();
        InheritanceMetadataImpl inhmd = new InheritanceMetadataImpl(internalInhmd);
        inhmd.parent = this;
        return inhmd;
    }

    public JoinMetadata newJoinMetadata()
    {
        JoinMetaData internalJoinmd = getInternal().newJoinMetaData();
        JoinMetadataImpl joinmd = new JoinMetadataImpl(internalJoinmd);
        joinmd.parent = this;
        return joinmd;
    }

    public PrimaryKeyMetadata newPrimaryKeyMetadata()
    {
        PrimaryKeyMetaData internalPkmd = getInternal().newPrimaryKeyMetadata();
        PrimaryKeyMetadataImpl pkmd = new PrimaryKeyMetadataImpl(internalPkmd);
        pkmd.parent = this;
        return pkmd;
    }

    public QueryMetadata newQueryMetadata(String name)
    {
        QueryMetaData internalQmd = getInternal().newQueryMetadata(name);
        QueryMetadataImpl qmd = new QueryMetadataImpl(internalQmd);
        qmd.parent = this;
        return qmd;
    }

    public UniqueMetadata newUniqueMetadata()
    {
        UniqueMetaData internalUnimd = getInternal().newUniqueMetadata();
        UniqueMetadataImpl unimd = new UniqueMetadataImpl(internalUnimd);
        unimd.parent = this;
        return unimd;
    }

    public VersionMetadata newVersionMetadata()
    {
        VersionMetaData internalVermd = getInternal().newVersionMetadata();
        VersionMetadataImpl vermd = new VersionMetadataImpl(internalVermd);
        vermd.parent = this;
        return vermd;
    }

    public TypeMetadata setCacheable(boolean cache)
    {
        getInternal().setCacheable(cache);
        return this;
    }

    public TypeMetadata setCatalog(String cat)
    {
        getInternal().setCatalog(cat);
        return this;
    }

    public TypeMetadata setDetachable(boolean flag)
    {
        getInternal().setDetachable(flag);
        return this;
    }

    public TypeMetadata setSerializeRead(boolean flag)
    {
        getInternal().setSerializeRead(flag);
        return this;
    }

    public TypeMetadata setEmbeddedOnly(boolean flag)
    {
        getInternal().setEmbeddedOnly(flag);
        return this;
    }

    public TypeMetadata setIdentityType(IdentityType type)
    {
        if (type == IdentityType.APPLICATION)
        {
            getInternal().setIdentityType(org.datanucleus.metadata.IdentityType.APPLICATION);
        }
        else if (type == IdentityType.DATASTORE)
        {
            getInternal().setIdentityType(org.datanucleus.metadata.IdentityType.DATASTORE);
        }
        else if (type == IdentityType.NONDURABLE)
        {
            getInternal().setIdentityType(org.datanucleus.metadata.IdentityType.NONDURABLE);
        }
        return this;
    }

    public TypeMetadata setObjectIdClass(String clsName)
    {
        if (!StringUtils.isWhitespace(clsName))
        {
            getInternal().setObjectIdClass(DataNucleusHelperJDO.getObjectIdClassForInputIdClass(clsName));
        }
        return this;
    }

    public TypeMetadata setRequiresExtent(boolean flag)
    {
        getInternal().setRequiresExtent(flag);
        return this;
    }

    public TypeMetadata setSchema(String schema)
    {
        getInternal().setSchema(schema);
        return this;
    }

    public TypeMetadata setTable(String table)
    {
        getInternal().setTable(table);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ComponentMetadata#getColumns()
     */
    public ColumnMetadata[] getColumns()
    {
        List internalColmds = getInternal().getUnmappedColumns();
        if (internalColmds == null)
        {
            return null;
        }
        ColumnMetadataImpl[] colmds = new ColumnMetadataImpl[internalColmds.size()];
        for (int i=0;i<colmds.length;i++)
        {
            colmds[i] = new ColumnMetadataImpl((ColumnMetaData)internalColmds.get(i));
            colmds[i].parent = this;
        }
        return colmds;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ComponentMetadata#getNumberOfColumns()
     */
    public int getNumberOfColumns()
    {
        List colmds = getInternal().getUnmappedColumns();
        return (colmds != null ? colmds.size() : 0);
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ComponentMetadata#newColumnMetadata()
     */
    public ColumnMetadata newColumnMetadata()
    {
        ColumnMetaData internalColmd = getInternal().newUnmappedColumnMetaData();
        ColumnMetadataImpl colmd = new ColumnMetadataImpl(internalColmd);
        colmd.parent = this;
        return colmd;
    }
}