/**********************************************************************
Copyright (c) 2010 Andy Jefferson and others. All rights reserved. 
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
package org.datanucleus.api.jdo.query;

import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.query.OrderExpression.OrderDirection;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ExecutionContext;
import org.datanucleus.api.jdo.JDOPersistenceManager;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.query.compiler.JDOQLSymbolResolver;
import org.datanucleus.query.compiler.QueryCompilation;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.symbol.PropertySymbol;
import org.datanucleus.query.symbol.SymbolTable;

/**
 * Abstract base for a typesafe query. Extended by JDOTypesafeQuery and JDOTypesafeSubquery.
 */
public abstract class AbstractJDOQLTypedQuery<T>
{
    enum QueryType
    {
        SELECT,
        BULK_UPDATE,
        BULK_DELETE;
    }

    protected QueryType type = QueryType.SELECT;

    /** Candidate class for the query. */
    protected Class candidateCls;

    /** Alias for the candidate of this query. */
    protected String candidateAlias = null;

    protected List<ExpressionImpl> updateExprs;

    protected List<ExpressionImpl> updateVals;

    /** Result expression(s). */
    protected List<ExpressionImpl> result;

    /** Whether the results are distinct (no dups). */
    protected Boolean resultDistinct = null;

    /** Filter expression. */
    protected BooleanExpressionImpl filter;

    /** Grouping expression(s). */
    protected List<ExpressionImpl> grouping;

    /** Having expression. */
    protected ExpressionImpl having;

    /** Ordering expression(s). */
    protected List<OrderExpressionImpl> ordering;

    protected PersistenceManager pm;

    protected ExecutionContext ec;

    /** The generic query compilation that this equates to (cached). */
    protected QueryCompilation compilation = null;

    /** The single-string query that this equates to (cached). */
    protected String queryString = null;

    public AbstractJDOQLTypedQuery(PersistenceManager pm, Class<T> cls, String alias)
    {
        this.pm = pm;
        this.ec = ((JDOPersistenceManager)pm).getExecutionContext();
        this.candidateCls = cls;
        this.candidateAlias = alias;
    }

    /**
     * Called when something is set on the query making any compilation invalid.
     */
    protected void discardCompiled()
    {
        compilation = null;
    }

    /**
     * Method to compile the query as it is currently defined.
     * @param mmgr Metadata manager
     * @param clr ClassLoader resolver
     * @return The generic compilation
     */
    protected QueryCompilation compile(MetaDataManager mmgr, ClassLoaderResolver clr)
    {
        SymbolTable symtbl = new SymbolTable();
        symtbl.setSymbolResolver(new JDOQLSymbolResolver(mmgr, clr, symtbl, candidateCls, candidateAlias));
        symtbl.addSymbol(new PropertySymbol(candidateAlias, candidateCls));

        org.datanucleus.query.expression.Expression[] resultExprs = null;
        if (result != null && !result.isEmpty())
        {
            resultExprs = new org.datanucleus.query.expression.Expression[result.size()];
            Iterator iter = result.iterator();
            int i=0;
            while (iter.hasNext())
            {
                ExpressionImpl result = (ExpressionImpl)iter.next();
                org.datanucleus.query.expression.Expression resultExpr = result.getQueryExpression();
                resultExpr.bind(symtbl);
                resultExprs[i++] = resultExpr;
            }

            if (resultExprs.length == 1 && resultExprs[0] instanceof PrimaryExpression)
            {
                // Check for special case of "Object(p)" in result, which means no special result
                String resultExprId = ((PrimaryExpression)resultExprs[0]).getId();
                if (resultExprId.equalsIgnoreCase(candidateAlias))
                {
                    resultExprs = null;
                }
            }
        }

        org.datanucleus.query.expression.Expression filterExpr = null;
        if (filter != null)
        {
            filterExpr = filter.getQueryExpression();
            if (filterExpr != null)
            {
                filterExpr.bind(symtbl);
            }
        }

        org.datanucleus.query.expression.Expression[] groupingExprs = null;
        if (grouping != null && !grouping.isEmpty())
        {
            groupingExprs = new org.datanucleus.query.expression.Expression[grouping.size()];
            Iterator iter = grouping.iterator();
            int i=0;
            while (iter.hasNext())
            {
                ExpressionImpl grp = (ExpressionImpl)iter.next();
                org.datanucleus.query.expression.Expression groupingExpr = grp.getQueryExpression();
                groupingExpr.bind(symtbl);
                groupingExprs[i++] = groupingExpr;
            }
        }

        org.datanucleus.query.expression.Expression havingExpr = null;
        if (having != null)
        {
            havingExpr = having.getQueryExpression();
            havingExpr.bind(symtbl);
        }

        org.datanucleus.query.expression.Expression[] orderExprs = null;
        if (ordering != null && !ordering.isEmpty())
        {
            orderExprs = new org.datanucleus.query.expression.Expression[ordering.size()];
            Iterator<OrderExpressionImpl> iter = ordering.iterator();
            int i=0;
            while (iter.hasNext())
            {
                OrderExpressionImpl order = iter.next();
                org.datanucleus.query.expression.OrderExpression orderExpr =
                    new org.datanucleus.query.expression.OrderExpression(
                        ((ExpressionImpl)order.getExpression()).getQueryExpression(), 
                        order.getDirection() == OrderDirection.ASC ? "ascending" : "descending");
                orderExpr.bind(symtbl);
                orderExprs[i++] = orderExpr;
            }
        }

        org.datanucleus.query.expression.Expression[] updateExprs = null;
        if (this.updateExprs != null)
        {
            Iterator<ExpressionImpl> expIter = this.updateExprs.iterator();
            Iterator<ExpressionImpl> valIter = this.updateVals.iterator();
            updateExprs = new Expression[this.updateExprs.size()];
            int i = 0;
            while (expIter.hasNext())
            {
                ExpressionImpl updateExpr = expIter.next();
                ExpressionImpl updateVal  = valIter.next();
                updateExprs[i++] = new DyadicExpression(updateExpr.getQueryExpression(), Expression.OP_EQ, 
                    updateVal.getQueryExpression());
            }
        }

        compilation = new QueryCompilation(candidateCls, candidateAlias, symtbl, resultExprs,
            null, filterExpr, groupingExprs, havingExpr, orderExprs, updateExprs);
        compilation.setQueryLanguage("JDOQL");

        return compilation;
    }

    /**
     * Accessor for the generic compilation that this criteria query equates to.
     * @return The generic compilation
     */
    public QueryCompilation getCompilation()
    {
        if (compilation == null)
        {
            // Not yet compiled, so compile it
            compilation = compile(ec.getMetaDataManager(), ec.getClassLoaderResolver());
        }
        return compilation;
    }
}