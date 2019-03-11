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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.query.OrderExpression.OrderDirection;
import javax.jdo.query.OrderExpression.OrderNullsPosition;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ExecutionContext;
import org.datanucleus.api.jdo.JDOPersistenceManager;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.query.compiler.JDOQLSymbolResolver;
import org.datanucleus.query.compiler.PropertySymbol;
import org.datanucleus.query.compiler.QueryCompilation;
import org.datanucleus.query.compiler.SymbolTable;
import org.datanucleus.query.expression.CaseExpression;
import org.datanucleus.query.expression.CaseExpression.ExpressionPair;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.store.query.Query;
import org.datanucleus.store.query.Query.QueryType;

/**
 * Abstract base for a typesafe query. Extended by JDOTypesafeQuery and JDOTypesafeSubquery.
 */
public abstract class AbstractJDOQLTypedQuery<T>
{
    protected AbstractJDOQLTypedQuery parentQuery;

    protected QueryType type = QueryType.SELECT;

    /** Candidate class for the query. */
    protected Class candidateCls;

    /** Whether to include subclasses of the candidate in the query. */
    protected boolean subclasses = true;

    /** Alias for the candidate of this query. */
    protected String candidateAlias = null;

    protected List<ExpressionImpl> updateExprs;

    protected List<ExpressionImpl> updateVals;

    /** Result expression(s). */
    protected List<ExpressionImpl> result;

    /** Whether the results are distinct (no dups). */
    protected Boolean resultDistinct = null;

    /** Whether the result is unique (single row). */
    protected boolean unique = false;

    protected Class resultClass = null;

    /** Filter expression. */
    protected BooleanExpressionImpl filter;

    /** Grouping expression(s). */
    protected List<ExpressionImpl> grouping;

    /** Having expression. */
    protected ExpressionImpl having;

    /** Ordering expression(s). */
    protected List<OrderExpressionImpl> ordering;

    /** Range : lower limit expression. */
    protected ExpressionImpl rangeLowerExpr;

    /** Range : upper limit expression. */
    protected ExpressionImpl rangeUpperExpr;

    protected PersistenceManager pm;

    protected ExecutionContext ec;

    /** The generic query compilation that this equates to (cached). */
    protected QueryCompilation compilation = null;

    /** The single-string query that this equates to (cached). */
    protected String queryString = null;

    public AbstractJDOQLTypedQuery(PersistenceManager pm, Class<T> cls, String alias, AbstractJDOQLTypedQuery parentQuery)
    {
        this.pm = pm;
        this.ec = ((JDOPersistenceManager)pm).getExecutionContext();
        this.candidateCls = cls;
        this.candidateAlias = alias;
        this.parentQuery = parentQuery;
    }

    /**
     * Called when something is set on the query making any compilation invalid.
     */
    protected void discardCompiled()
    {
        compilation = null;
        queryString = null;
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
        if (parentQuery != null)
        {
            symtbl.setParentSymbolTable(parentQuery.compilation.getSymbolTable());
        }
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
        else if (resultClass != null && resultClass != Object[].class)
        {
            // When result class specified and no result then default the result to "DISTINCT this". See also AbstractJDOQLQuery in datanucleus-core
            resultExprs = new Expression[1];
            List<String> primExprTuples = new ArrayList<>();
            primExprTuples.add("this");
            resultExprs[0] = new DyadicExpression(Expression.OP_DISTINCT, new PrimaryExpression(primExprTuples));
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
                org.datanucleus.query.expression.OrderExpression orderExpr;
                OrderNullsPosition nullsPos = order.getNullsPosition();
                if (nullsPos != null)
                {
                    orderExpr = new org.datanucleus.query.expression.OrderExpression(((ExpressionImpl)order.getExpression()).getQueryExpression(), 
                        order.getDirection() == OrderDirection.ASC ? "ascending" : "descending",
                        nullsPos == OrderNullsPosition.FIRST ? "nulls first" : "nulls last");
                }
                else
                {
                    orderExpr = new org.datanucleus.query.expression.OrderExpression(((ExpressionImpl)order.getExpression()).getQueryExpression(), 
                        order.getDirection() == OrderDirection.ASC ? "ascending" : "descending");
                }

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
                updateExprs[i++] = new DyadicExpression(updateExpr.getQueryExpression(), Expression.OP_EQ, updateVal.getQueryExpression());
            }
        }

        compilation = new QueryCompilation(candidateCls, candidateAlias, symtbl, resultExprs, null, filterExpr, groupingExprs, havingExpr, orderExprs, updateExprs);
        if (resultDistinct != null && resultDistinct.booleanValue())
        {
            compilation.setResultDistinct();
        }
        compilation.setQueryLanguage(Query.LANGUAGE_JDOQL);

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

    /**
     * Method to return the single-string form of this JDOQL query.
     * @return Single-string form of the query
     */
    public String toString()
    {
        // TODO Replace any variables that correspond to subqueries by the toString() of the subquery
        if (queryString == null)
        {
            StringBuilder str = null;
            if (type == QueryType.BULK_UPDATE)
            {
                str = new StringBuilder("UPDATE");
            }
            else if (type == QueryType.BULK_DELETE)
            {
                str = new StringBuilder("DELETE");
            }
            else
            {
                str = new StringBuilder("SELECT");
            }

            if (type == QueryType.SELECT)
            {
                if (unique)
                {
                    str.append(" UNIQUE");
                }

                // Result
                if (result != null && !result.isEmpty())
                {
                    if (resultDistinct != null && resultDistinct.booleanValue())
                    {
                        str.append(" DISTINCT");
                    }
                    str.append(" ");
                    Iterator<ExpressionImpl> iter = result.iterator();
                    while (iter.hasNext())
                    {
                        ExpressionImpl resultExpr = iter.next();
                        str.append(getJDOQLForExpression(resultExpr.getQueryExpression()));
                        if (iter.hasNext())
                        {
                            str.append(",");
                        }
                    }
                }

                // Result class
                if (resultClass != null)
                {
                    str.append(" INTO ").append(resultClass.getName());
                }
            }

            // Candidate
            if (type == QueryType.SELECT || type == QueryType.BULK_DELETE)
            {
                str.append(" FROM ").append(candidateCls.getName());
            }
            else
            {
                str.append(" " + candidateCls.getName());
            }
            if (this instanceof JDOQLTypedSubqueryImpl)
            {
                str.append(" " + candidateAlias);
            }

            if (!subclasses)
            {
                str.append(" EXCLUDE SUBCLASSES");
            }

            if (type == QueryType.BULK_UPDATE)
            {
                str.append(" SET");
                Iterator<ExpressionImpl> exprIter = updateExprs.iterator();
                Iterator<ExpressionImpl> valIter = updateVals.iterator();
                while (exprIter.hasNext())
                {
                    ExpressionImpl expr = exprIter.next();
                    ExpressionImpl val = valIter.next();
                    str.append(" ").append(getJDOQLForExpression(expr.getQueryExpression()));
                    str.append(" = ").append(getJDOQLForExpression(val.getQueryExpression()));
                    if (exprIter.hasNext())
                    {
                        str.append(",");
                    }
                }
            }

            // Filter
            if (filter != null)
            {
                str.append(" WHERE ");
                str.append(getJDOQLForExpression(filter.getQueryExpression()));
            }

            if (type == QueryType.SELECT)
            {
                // Grouping
                if (grouping != null && !grouping.isEmpty())
                {
                    str.append(" GROUP BY ");
                    Iterator<ExpressionImpl> iter = grouping.iterator();
                    while (iter.hasNext())
                    {
                        ExpressionImpl groupExpr = iter.next();
                        str.append(getJDOQLForExpression(groupExpr.getQueryExpression()));
                        if (iter.hasNext())
                        {
                            str.append(",");
                        }
                    }
                }

                // Having
                if (having != null)
                {
                    str.append(" HAVING ");
                    str.append(getJDOQLForExpression(having.getQueryExpression()));
                }

                // Ordering
                if (ordering != null && !ordering.isEmpty())
                {
                    str.append(" ORDER BY ");
                    Iterator<OrderExpressionImpl> iter = ordering.iterator();
                    while (iter.hasNext())
                    {
                        OrderExpressionImpl orderExpr = iter.next();
                        str.append(getJDOQLForExpression(((ExpressionImpl)orderExpr.getExpression()).getQueryExpression()));
                        str.append(" " + (orderExpr.getDirection() == OrderDirection.ASC ? "ASCENDING" : "DESCENDING"));
                        OrderNullsPosition nullsPos = orderExpr.getNullsPosition();
                        if (nullsPos != null)
                        {
                            str.append(" " + (nullsPos == OrderNullsPosition.FIRST ? "NULLS FIRST" : "NULLS LAST"));
                        }
                        if (iter.hasNext())
                        {
                            str.append(",");
                        }
                    }
                }

                // Range
                if (rangeLowerExpr != null && rangeUpperExpr != null)
                {
                    str.append(" RANGE ");
                    str.append(getJDOQLForExpression(rangeLowerExpr.getQueryExpression()));
                    str.append(",");
                    str.append(getJDOQLForExpression(rangeUpperExpr.getQueryExpression()));
                }
            }

            queryString = str.toString();
        }
        return queryString;
    }

    public String getJDOQLForExpression(Expression expr)
    {
        if (expr instanceof DyadicExpression)
        {
            DyadicExpression dyExpr = (DyadicExpression)expr;
            Expression left = dyExpr.getLeft();
            Expression right = dyExpr.getRight();
            StringBuilder str = new StringBuilder("(");
            if (dyExpr.getOperator() == Expression.OP_DISTINCT)
            {
                // Distinct goes in front of the left expression
                str.append("DISTINCT ");
            }

            if (left != null && dyExpr.getOperator() == Expression.OP_NOT)
            {
                str.append("!").append(getJDOQLForExpression(left));
            }
            else if (dyExpr.getOperator() == Expression.OP_CAST)
            {
                str.append("(").append(((Literal)right).getLiteral()).append(")").append(getJDOQLForExpression(left));
            }
            else
            {
                if (left != null)
                {
                    str.append(getJDOQLForExpression(left));
                }

                // Special cases
                if (dyExpr.getOperator() == Expression.OP_AND)
                {
                    str.append(" && ");
                }
                else if (dyExpr.getOperator() == Expression.OP_OR)
                {
                    str.append(" || ");
                }
                else if (dyExpr.getOperator() == Expression.OP_ADD)
                {
                    str.append(" + ");
                }
                else if (dyExpr.getOperator() == Expression.OP_SUB)
                {
                    str.append(" - ");
                }
                else if (dyExpr.getOperator() == Expression.OP_MUL)
                {
                    str.append(" * ");
                }
                else if (dyExpr.getOperator() == Expression.OP_DIV)
                {
                    str.append(" / ");
                }
                else if (dyExpr.getOperator() == Expression.OP_EQ)
                {
                    str.append(" == ");
                }
                else if (dyExpr.getOperator() == Expression.OP_GT)
                {
                    str.append(" > ");
                }
                else if (dyExpr.getOperator() == Expression.OP_LT)
                {
                    str.append(" < ");
                }
                else if (dyExpr.getOperator() == Expression.OP_GTEQ)
                {
                    str.append(" >= ");
                }
                else if (dyExpr.getOperator() == Expression.OP_LTEQ)
                {
                    str.append(" <= ");
                }
                else if (dyExpr.getOperator() == Expression.OP_NOTEQ)
                {
                    str.append(" != ");
                }
                else if (dyExpr.getOperator() == Expression.OP_DISTINCT)
                {
                    // Processed above
                }
                else if (dyExpr.getOperator() == Expression.OP_IS)
                {
                    str.append(" instanceof ");
                }
                else
                {
                    // TODO Support other operators
                    throw new UnsupportedOperationException("Dont currently support operator " + dyExpr.getOperator() + " in JDOQL conversion");
                }

                if (right != null)
                {
                    str.append(getJDOQLForExpression(right));
                }
            }

            str.append(")");
            return str.toString();
        }
        else if (expr instanceof PrimaryExpression)
        {
            PrimaryExpression primExpr = (PrimaryExpression)expr;
            if (primExpr.getLeft() != null)
            {
                return getJDOQLForExpression(primExpr.getLeft()) + "." + primExpr.getId();
            }
            return primExpr.getId();
        }
        else if (expr instanceof ParameterExpression)
        {
            ParameterExpression paramExpr = (ParameterExpression)expr;
            if (paramExpr.getId() != null)
            {
                return ":" + paramExpr.getId();
            }
            return "?" + paramExpr.getPosition();
        }
        else if (expr instanceof VariableExpression)
        {
            VariableExpression varExpr = (VariableExpression)expr;
            return varExpr.getId();
        }
        else if (expr instanceof InvokeExpression)
        {
            InvokeExpression invExpr = (InvokeExpression)expr;
            StringBuilder str = new StringBuilder();
            if (invExpr.getLeft() != null)
            {
                str.append(getJDOQLForExpression(invExpr.getLeft())).append(".");
            }
            str.append(invExpr.getOperation());
            str.append("(");
            List<Expression> args = invExpr.getArguments();
            if (args != null)
            {
                Iterator<Expression> iter = args.iterator();
                while (iter.hasNext())
                {
                    str.append(getJDOQLForExpression(iter.next()));
                    if (iter.hasNext())
                    {
                        str.append(",");
                    }
                }
            }
            str.append(")");
            return str.toString();
        }
        else if (expr instanceof Literal)
        {
            Literal litExpr = (Literal)expr;
            Object value = litExpr.getLiteral();
            if (value instanceof String || value instanceof Character)
            {
                return "'" + value.toString() + "'";
            }
            else if (value instanceof Class)
            {
                return ((Class)value).getName();
            }
            else if (value instanceof Boolean)
            {
                return ((Boolean)value ? "TRUE" : "FALSE");
            }
            else
            {
                if (litExpr.getLiteral() == null)
                {
                    return "null";
                }
                return litExpr.getLiteral().toString();
            }
        }
        else if (expr instanceof CaseExpression)
        {
            CaseExpression caseExpr = (CaseExpression)expr;
            List<ExpressionPair> conds = caseExpr.getConditions();
            Expression elseExpr = caseExpr.getElseExpression();
            StringBuilder str = new StringBuilder();
            for (ExpressionPair pair : conds)
            {
                if (str.length() > 0)
                {
                    str.append(" ELSE ");
                }
                str.append("IF ").append(getJDOQLForExpression(pair.getWhenExpression())).append(" ");
                str.append(getJDOQLForExpression(pair.getActionExpression()));
            }
            if (elseExpr != null)
            {
                str.append(" ELSE ").append(getJDOQLForExpression(elseExpr));
            }
            return str.toString();
        }
        else
        {
            throw new UnsupportedOperationException("Dont currently support " + expr.getClass().getName() + " in JDOQLHelper");
        }
    }
}