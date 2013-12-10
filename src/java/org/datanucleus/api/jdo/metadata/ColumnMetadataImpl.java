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

import javax.jdo.metadata.ColumnMetadata;

import org.datanucleus.metadata.ColumnMetaData;

/**
 * Implementation of JDO ColumnMetadata object.
 */
public class ColumnMetadataImpl extends AbstractMetadataImpl implements ColumnMetadata
{
    public ColumnMetadataImpl(ColumnMetaData internal)
    {
        super(internal);
    }

    public ColumnMetaData getInternal()
    {
        return (ColumnMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#getAllowsNull()
     */
    public Boolean getAllowsNull()
    {
        return getInternal().getAllowsNull();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#getDefaultValue()
     */
    public String getDefaultValue()
    {
        return getInternal().getDefaultValue();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#getInsertValue()
     */
    public String getInsertValue()
    {
        return getInternal().getInsertValue();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#getJDBCType()
     */
    public String getJDBCType()
    {
        return getInternal().getJdbcType();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#getLength()
     */
    public Integer getLength()
    {
        return getInternal().getLength();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#getName()
     */
    public String getName()
    {
        return getInternal().getName();
    }

    public Integer getPosition()
    {
        return getInternal().getPosition();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#getSQLType()
     */
    public String getSQLType()
    {
        return getInternal().getSqlType();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#getScale()
     */
    public Integer getScale()
    {
        return getInternal().getScale();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#getTarget()
     */
    public String getTarget()
    {
        return getInternal().getTarget();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#getTargetField()
     */
    public String getTargetField()
    {
        return getInternal().getTargetMember();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#setAllowsNull(boolean)
     */
    public ColumnMetadata setAllowsNull(boolean flag)
    {
        getInternal().setAllowsNull(flag);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#setDefaultValue(java.lang.String)
     */
    public ColumnMetadata setDefaultValue(String val)
    {
        getInternal().setDefaultValue(val);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#setInsertValue(java.lang.String)
     */
    public ColumnMetadata setInsertValue(String val)
    {
        getInternal().setInsertValue(val);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#setJDBCType(java.lang.String)
     */
    public ColumnMetadata setJDBCType(String type)
    {
        getInternal().setJdbcType(type);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#setLength(int)
     */
    public ColumnMetadata setLength(int len)
    {
        getInternal().setLength(len);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#setName(java.lang.String)
     */
    public ColumnMetadata setName(String name)
    {
        getInternal().setName(name);
        return this;
    }

    public ColumnMetadata setPosition(int pos)
    {
        getInternal().setPosition(pos);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#setSQLType(java.lang.String)
     */
    public ColumnMetadata setSQLType(String type)
    {
        getInternal().setSqlType(type);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#setScale(int)
     */
    public ColumnMetadata setScale(int scale)
    {
        getInternal().setScale(scale);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#setTarget(java.lang.String)
     */
    public ColumnMetadata setTarget(String tgt)
    {
        getInternal().setTarget(tgt);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.ColumnMetadata#setTargetField(java.lang.String)
     */
    public ColumnMetadata setTargetField(String tgt)
    {
        getInternal().setTargetMember(tgt);
        return this;
    }
}