/**********************************************************************
Copyright (c) 2007 Erik Bengtson and others. All rights reserved. 
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOException;
import javax.jdo.JDOQueryInterruptedException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.metadata.QueryMetaData;
import org.datanucleus.store.query.NoQueryResultsException;
import org.datanucleus.store.query.QueryInterruptedException;
import org.datanucleus.store.query.QueryTimeoutException;

/**
 * Wrapper for JDO Query class.
 * Stores the PM the query is executed against, the internal query, and the query language.
 * The language is stored since it is referenced by the JDO API and so we don't have to embody knowledge
 * of which internal query type is for which language (could be moved to the internal query).
 */
public class JDOQuery implements Query
{
    private static final long serialVersionUID = -204134873012573162L;

    /** PersistenceManager for the query. */
    transient PersistenceManager pm;

    /** Underlying query that will be executed. */
    org.datanucleus.store.query.Query query;

    /** Query language. */
    String language;

    /** JDO Fetch Plan. */
    JDOFetchPlan fetchPlan = null;

    /**
     * Constructor for a query used by JDO.
     * @param pm PersistenceManager
     * @param query Underlying query
     * @param language Query language
     */
    public JDOQuery(PersistenceManager pm, org.datanucleus.store.query.Query query, String language)
    {
        this.pm = pm;
        this.query = query;
        this.language = language;
    }

    public void close()
    {
        // TODO Improve cleanup, maybe by updating org.datanucleus.store.query.Query
        closeAll();
        this.fetchPlan.clearGroups();
        this.fetchPlan = null;
    }

    /**
     * Close the query result.
     * @param queryResult Query result
     */
    public void close(Object queryResult)
    {
        query.close(queryResult);
    }

    /**
     * Close all query results for this query.
     */
    public void closeAll()
    {
        query.closeAll();
    }

    /**
     * Compile the query.
     */
    public void compile()
    {
        try
        {
            query.compile();
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Declare any imports for the query.
     * @param imports The imports
     */
    public void declareImports(String imports)
    {
        try
        {
            query.declareImports(imports);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Declare any parameters for the query.
     * @param parameters The parameters
     */
    public void declareParameters(String parameters)
    {
        try
        {
            query.declareExplicitParameters(parameters);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Declare any variables for the query.
     * @param variables The variables
     */
    public void declareVariables(String variables)
    {
        try
        {
            query.declareExplicitVariables(variables);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Execute the query deleting all instances found.
     * @return Number of deleted instances
     */
    public long deletePersistentAll()
    {
        try
        {
            return query.deletePersistentAll();
        }
        catch (NoQueryResultsException nqre)
        {
            return 0;
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Execute the query deleting all instances found.
     * @param parameters Parameters to use when executing
     * @return Number of deleted instances
     */
    public long deletePersistentAll(Object... parameters)
    {
        try
        {
            return query.deletePersistentAll(parameters);
        }
        catch (NoQueryResultsException nqre)
        {
            return 0;
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Execute the query deleting all instances found.
     * @param parameters Parameters to use when executing
     * @return Number of deleted instances
     */
    public long deletePersistentAll(Map parameters)
    {
        try
        {
            return query.deletePersistentAll(parameters);
        }
        catch (NoQueryResultsException nqre)
        {
            return 0;
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Execute the query.
     * @return The results
     */
    public Object execute()
    {
        try
        {
            return query.execute();
        }
        catch (NoQueryResultsException nqre)
        {
            return null;
        }
        catch (NucleusException jpe)
        {
            // Convert any exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Execute the query.
     * @param p1 First param value
     * @return The results
     */
    public Object execute(Object p1)
    {
        try
        {
            return query.executeWithArray(new Object[] {p1});
        }
        catch (NoQueryResultsException nqre)
        {
            return null;
        }
        catch (NucleusException jpe)
        {
            // Convert any exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Execute the query.
     * @param p1 First param value
     * @param p2 Second param value
     * @return The results
     */
    public Object execute(Object p1, Object p2)
    {
        try
        {
            return query.executeWithArray(new Object[] {p1, p2});
        }
        catch (NoQueryResultsException nqre)
        {
            return null;
        }
        catch (NucleusException jpe)
        {
            // Convert any exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Execute the query.
     * @param p1 First param value
     * @param p2 Second param value
     * @param p3 Third param value
     * @return The results
     */
    public Object execute(Object p1, Object p2, Object p3)
    {
        try
        {
            return query.executeWithArray(new Object[] {p1, p2, p3});
        }
        catch (NoQueryResultsException nqre)
        {
            return null;
        }
        catch (NucleusException jpe)
        {
            // Convert any exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }            
    }

    /**
     * Execute the query.
     * @param parameterValues Param values
     * @return The results
     */
    public Object executeWithArray(Object... parameterValues)
    {
        try
        {
            return query.executeWithArray(parameterValues);
        }
        catch (NoQueryResultsException nqre)
        {
            return null;
        }
        catch (NucleusException jpe)
        {
            // Convert any exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Execute the query.
     * @param parameters Param values
     * @return The results
     */
    public Object executeWithMap(Map parameters)
    {
        try
        {
            return query.executeWithMap(parameters);
        }
        catch (NoQueryResultsException nqre)
        {
            return null;
        }
        catch (QueryTimeoutException qte)
        {
            throw new JDODataStoreException("Query has timed out : " + qte.getMessage());
        }
        catch (QueryInterruptedException qie)
        {
            throw new JDOQueryInterruptedException("Query has been cancelled : " + qie.getMessage());
        }
        catch (NucleusException jpe)
        {
            // Convert any exceptions into what JDO expects
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Method to cancel any executing queries.
     * @throws JDOUnsupportedOptionException if the datastore doesn't support cancellation of queries
     */
    public void cancelAll()
    {
        try
        {
            query.cancel();
        }
        catch (NucleusException ne)
        {
            throw new JDOException("Error in calling Query.cancelAll. See the nested exception", ne);
        }
        catch (UnsupportedOperationException uoe)
        {
            throw new JDOUnsupportedOptionException();
        }
    }

    /**
     * Method to cancel the executing query for the supplied thread.
     * @throws JDOUnsupportedOptionException if the datastore doesn't support cancellation of queries
     */
    public void cancel(Thread thr)
    {
        try
        {
            query.cancel(thr);
        }
        catch (NucleusException ne)
        {
            throw new JDOException("Error in calling Query.cancelAll. See the nested exception", ne);
        }
        catch (UnsupportedOperationException uoe)
        {
            throw new JDOUnsupportedOptionException();
        }
    }

    /**
     * Set the candidates for the query.
     * @param extent Extent defining the candidates
     */
    public void setCandidates(Extent extent)
    {
        try
        {
            if (extent == null)
            {
                query.setCandidates((org.datanucleus.store.Extent)null);
            }
            else
            {
                query.setCandidates(((JDOExtent)extent).getExtent());
            }
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Set the candidates for the query.
     * @param pcs PC candidates
     */
    public void setCandidates(Collection pcs)
    {
        try
        {
            query.setCandidates(pcs);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Set the candidate class for the query.
     * @param candidateClass Candidate class
     */
    public void setClass(Class candidateClass)
    {
        try
        {
            query.setCandidateClass(candidateClass);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Method to add an extension to the query.
     * @param key Key for the extension
     * @param value Value for the extension
     */
    public void addExtension(String key, Object value)
    {
        query.addExtension(key, value);
    }

    /**
     * Set the extensions for the query.
     * @param extensions The extensions
     */
    public void setExtensions(Map extensions)
    {
        query.setExtensions(extensions);
    }

    /**
     * Accessor for the fetch plan to use.
     * @return The fetch plan
     */
    public FetchPlan getFetchPlan()
    {
        if (fetchPlan == null)
        {
            // Not yet assigned so give a JDO wrapper to the underlying FetchPlan
            fetchPlan = new JDOFetchPlan(query.getFetchPlan());
        }
        return fetchPlan;
    }

    /**
     * Set the filter for the query.
     * @param filter The query filter
     */
    public void setFilter(String filter)
    {
        try
        {
            query.setFilter(filter);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Set the grouping for the query.
     * @param grouping The grouping
     */
    public void setGrouping(String grouping)
    {
        try
        {
            query.setGrouping(grouping);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Accessor for the ignore cache setting
     * @return Ignore cache
     */
    public boolean getIgnoreCache()
    {
        return query.getIgnoreCache();
    }

    /**
     * Set the ignore cache setting for the query.
     * @param ignoreCache The ignore cache setting
     */
    public void setIgnoreCache(boolean ignoreCache)
    {
        query.setIgnoreCache(ignoreCache);
    }

    /**
     * Set the ordering for the query.
     * @param ordering The ordering
     */
    public void setOrdering(String ordering)
    {
        try
        {
            query.setOrdering(ordering);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Accessor for the PersistenceManager.
     * @return PM
     */
    public PersistenceManager getPersistenceManager()
    {
        return pm;
    }

    /**
     * Set the range for the query.
     * @param range The range specification
     */
    public void setRange(String range)
    {
        try
        {
            query.setRange(range);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Set the range for the query.
     * @param fromIncl From range inclusive
     * @param toExcl To range exclusive
     */
    public void setRange(long fromIncl, long toExcl)
    {
        try
        {
            query.setRange(fromIncl, toExcl);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Set the result for the query.
     * @param result Result clause
     */
    public void setResult(String result)
    {
        try
        {
            query.setResult(result);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Set the result class for the query.
     * @param result_cls Result class
     */
    public void setResultClass(Class result_cls)
    {
        try
        {
            query.setResultClass(result_cls);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Set the datastore read timeout.
     * @param timeout Timeout interval (millisecs)
     */
    public void setDatastoreReadTimeoutMillis(Integer timeout)
    {
        try
        {
            query.setDatastoreReadTimeoutMillis(timeout);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Accessor for the datastore read timeout.
     * @return Timeout (if defined)
     */
    public Integer getDatastoreReadTimeoutMillis()
    {
        return query.getDatastoreReadTimeoutMillis();
    }

    /**
     * Set the datastore write timeout.
     * @param timeout Timeout interval (millisecs)
     */
    public void setDatastoreWriteTimeoutMillis(Integer timeout)
    {
        try
        {
            query.setDatastoreWriteTimeoutMillis(timeout);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Accessor for the datastore write timeout.
     * @return Timeout (if defined)
     */
    public Integer getDatastoreWriteTimeoutMillis()
    {
        return query.getDatastoreWriteTimeoutMillis();
    }

    /**
     * Set whether to expect a unique result.
     * @param unique Whether results are unique
     */
    public void setUnique(boolean unique)
    {
        try
        {
            query.setUnique(unique);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Accessor for whether the query is modifiable.
     * @return Whether it is modifiable
     */
    public boolean isUnmodifiable()
    {
        return query.isUnmodifiable();
    }

    /**
     * Set the query to be unmodifiable.
     */
    public void setUnmodifiable()
    {
        query.setUnmodifiable();
    }

    /**
     * Add a subquery to this query.
     * @param sub the subquery to add to this Query
     * @param variableDecl the name of the variable in the outer query to bind the results of the subquery
     * @param candidateExpr the candidate collection of the subquery as an expression using terms of the outer query
     */
    public void addSubquery(Query sub, String variableDecl, String candidateExpr)
    {
        addSubquery(sub, variableDecl, candidateExpr, (Map)null);
    }

    /**
     * Add a subquery to this query.
     * The String version of the method binds the named expression to the parameter implictly or explicitly 
     * declared in the subquery.
     * @param sub the subquery to add to this Query
     * @param variableDecl the name of the variable to be used in this Query
     * @param candidateExpr the candidate collection to apply to the subquery
     * @param parameter the expression from the outer query to bind the parameter in the subquery
     */
    public void addSubquery(Query sub, String variableDecl, String candidateExpr, String parameter)
    {
        Map paramMap = new HashMap();
        if (parameter != null)
        {
            // Positional parameter so key by Integer
            paramMap.put(Integer.valueOf(0), parameter);
        }
        addSubquery(sub, variableDecl, candidateExpr, paramMap);
    }

    /**
     * Add a subquery to this query.
     * The String version of the method binds the named expression to the parameter implictly or explicitly 
     * declared in the subquery.
     * @param sub the subquery to add to this Query
     * @param variableDecl the name of the variable to be used in this Query
     * @param candidateExpr the candidate collection to apply to the subquery
     * @param parameters the expressions from the outer query to bind the parameter in the subquery
     */
    public void addSubquery(Query sub, String variableDecl, String candidateExpr, String... parameters)
    {
        Map paramMap = new HashMap();
        if (parameters != null)
        {
            for (int i=0;i<parameters.length;i++)
            {
                // Positional parameter so key by Integer
                paramMap.put(Integer.valueOf(i), parameters[i]);
            }
        }
        addSubquery(sub, variableDecl, candidateExpr, paramMap);
    }

    /**
     * Add a subquery to this query.
     * The String version of the method binds the named expression to the parameter implictly or explicitly 
     * declared in the subquery.
     * @param sub the subquery to add to this Query
     * @param variableDecl the name of the variable to be used in this Query
     * @param candidateExpr the candidate collection to apply to the subquery
     * @param parameters the expressions from the outer query to bind the parameter in the subquery
     */
    public void addSubquery(Query sub, String variableDecl, String candidateExpr, Map parameters)
    {
        try
        {
            org.datanucleus.store.query.Query subquery = null;
            if (sub != null)
            {
                subquery = ((JDOQuery)sub).query;
            }
            query.addSubquery(subquery, variableDecl, candidateExpr, parameters);
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Accessor for whether to serialise any read objects in this query.
     * @return The setting for whether to serialise any read objects
     */
    public Boolean getSerializeRead()
    {
        return query.getSerializeRead();
    }

    /**
     * Mutator for whether to serialise any read objects.
     * @param serialize Whether to serialise any read objects in this query.
     */
    public void setSerializeRead(Boolean serialize)
    {
        query.setSerializeRead(serialize);
    }

    /**
     * Accessor for the internal query.
     * @return Internal query
     */
    public org.datanucleus.store.query.Query getInternalQuery()
    {
        return query;
    }

    /**
     * Accessor for the query language.
     * @return Query language
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Save this query as a named query with the specified name.
     * @param name The name to refer to it under
     * @return This query
     */
    public Query saveAsNamedQuery(String name)
    {
        QueryMetaData qmd = new QueryMetaData(name);
        qmd.setLanguage(language);
        qmd.setQuery(query.toString());
        qmd.setResultClass(query.getResultClassName());
        qmd.setUnique(query.isUnique());
        Map<String, Object> queryExts = query.getExtensions();
        if (queryExts != null && !queryExts.isEmpty())
        {
            Iterator<Map.Entry<String, Object>> queryExtsIter = queryExts.entrySet().iterator();
            while (queryExtsIter.hasNext())
            {
                Map.Entry<String, Object> queryExtEntry = queryExtsIter.next();
                qmd.addExtension(queryExtEntry.getKey(), "" + queryExtEntry.getValue());
            }
        }

        query.getExecutionContext().getMetaDataManager().registerNamedQuery(qmd);

        return this;
    }

    /**
     * Convenience method to return the query in string form.
     * @return Stringifier method
     */
    public String toString()
    {
        return query.toString();
    }

    /**
     * Accessor for the native query invoked by this query (if known at this time and supported by the store plugin).
     * @return The native query (e.g for RDBMS this is the SQL).
     */
    public Object getNativeQuery()
    {
        return query.getNativeQuery();
    }

    public Query filter(String filter)
    {
        setFilter(filter);
        return this;
    }

    public Query groupBy(String grouping)
    {
        setGrouping(grouping);
        return this;
    }

    public Query having(String having)
    {
        // TODO Handle this
        return this;
    }

    public Query orderBy(String ordering)
    {
        setOrdering(ordering);
        return this;
    }

    public Query range(long fromIncl, long toExcl)
    {
        setRange(fromIncl, toExcl);
        return this;
    }

    public Query range(String range)
    {
        setRange(range);
        return this;
    }
}