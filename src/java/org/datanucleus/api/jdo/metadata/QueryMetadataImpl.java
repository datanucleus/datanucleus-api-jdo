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

import javax.jdo.metadata.QueryMetadata;

import org.datanucleus.metadata.QueryLanguage;
import org.datanucleus.metadata.QueryMetaData;
import org.datanucleus.util.StringUtils;

/**
 * Implementation of JDO QueryMetadata object.
 */
public class QueryMetadataImpl extends AbstractMetadataImpl implements QueryMetadata
{
    public QueryMetadataImpl(QueryMetaData querymd)
    {
        super(querymd);
    }

    public QueryMetaData getInternal()
    {
        return (QueryMetaData)internalMD;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#getFetchPlan()
     */
    public String getFetchPlan()
    {
        return getInternal().getFetchPlanName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#getLanguage()
     */
    public String getLanguage()
    {
        return getInternal().getLanguage();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#getName()
     */
    public String getName()
    {
        return getInternal().getName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#getQuery()
     */
    public String getQuery()
    {
        return getInternal().getQuery();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#getResultClass()
     */
    public String getResultClass()
    {
        return getInternal().getResultClass();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#getUnique()
     */
    public Boolean getUnique()
    {
        return getInternal().isUnique();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#getUnmodifiable()
     */
    public boolean getUnmodifiable()
    {
        return getInternal().isUnmodifiable();
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#setFetchPlan(java.lang.String)
     */
    public QueryMetadata setFetchPlan(String fpName)
    {
        getInternal().setFetchPlanName(fpName);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#setLanguage(java.lang.String)
     */
    public QueryMetadata setLanguage(String lang)
    {
        if (!StringUtils.isWhitespace(lang))
        {
            if (lang.equals("javax.jdo.query.JDOQL")) // Convert to JDOQL
            {
                lang = QueryLanguage.JDOQL.toString();
            }
            else if (lang.equals("javax.jdo.query.SQL")) // Convert to SQL
            {
                lang = QueryLanguage.SQL.toString();
            }
            else if (lang.equals("javax.jdo.query.JPQL")) // Convert to JPQL
            {
                lang = QueryLanguage.JPQL.toString();
            }
        }
        getInternal().setLanguage(lang);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#setQuery(java.lang.String)
     */
    public QueryMetadata setQuery(String query)
    {
        getInternal().setQuery(query);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#setResultClass(java.lang.String)
     */
    public QueryMetadata setResultClass(String resultClass)
    {
        getInternal().setResultClass(resultClass);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#setUnique(boolean)
     */
    public QueryMetadata setUnique(boolean unique)
    {
        getInternal().setUnique(unique);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.metadata.QueryMetadata#setUnmodifiable()
     */
    public QueryMetadata setUnmodifiable()
    {
        getInternal().setUnmodifiable(true);
        return this;
    }
}