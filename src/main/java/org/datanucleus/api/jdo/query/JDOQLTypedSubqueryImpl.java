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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.jdo.JDOException;
import javax.jdo.JDOQLTypedSubquery;
import javax.jdo.PersistenceManager;
import javax.jdo.query.BooleanExpression;
import javax.jdo.query.CharacterExpression;
import javax.jdo.query.CollectionExpression;
import javax.jdo.query.DateExpression;
import javax.jdo.query.DateTimeExpression;
import javax.jdo.query.Expression;
import javax.jdo.query.NumericExpression;
import javax.jdo.query.PersistableExpression;
import javax.jdo.query.StringExpression;
import javax.jdo.query.TimeExpression;
import javax.jdo.query.OrderExpression.OrderDirection;

import org.datanucleus.query.JDOQLQueryHelper;
import org.datanucleus.query.expression.VariableExpression;

/**
 * Implementation of a JDOQLTypedSubquery.
 */
public class JDOQLTypedSubqueryImpl<T> extends AbstractJDOQLTypedQuery<T> implements JDOQLTypedSubquery<T>
{
    private static final long serialVersionUID = 8872729615681952405L;

    public JDOQLTypedSubqueryImpl(PersistenceManager pm, Class<T> candidateClass, String candidateAlias, JDOQLTypedQueryImpl parentQuery)
    {
        super(pm, candidateClass, candidateAlias);
    }

    public String getAlias()
    {
        return "VAR_" + candidateAlias.toUpperCase();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#candidate()
     */
    public PersistableExpression candidate()
    {
        String candName = candidateCls.getName();
        int pos = candName.lastIndexOf('.');
        String qName = candName.substring(0, pos+1) + JDOQLTypedQueryImpl.getQueryClassNameForClassName(candName.substring(pos+1));
        try
        {
            // Access the "candidate" field of the query class
            Class qClass = ec.getClassLoaderResolver().classForName(qName);
            Constructor ctr = qClass.getConstructor(new Class[] {PersistableExpression.class, String.class});
            Object candObj = ctr.newInstance(new Object[] {null, candidateAlias});
            if (candObj == null || !(candObj instanceof PersistableExpression))
            {
                throw new JDOException("Class " + candidateCls.getName() + " has a Query class but the candidate is invalid");
            }
            return (PersistableExpression)candObj;
        }
        catch (NoSuchMethodException nsfe)
        {
            throw new JDOException("Class " + candidateCls.getName() + " has a Query class but the candidate is invalid");
        }
        catch (InvocationTargetException ite)
        {
            throw new JDOException("Class " + candidateCls.getName() + " has a Query class but the candidate is invalid");
        }
        catch (InstantiationException ie)
        {
            throw new JDOException("Class " + candidateCls.getName() + " has a Query class but the candidate is invalid");
        }
        catch (IllegalAccessException iae)
        {
            throw new JDOException("Class " + candidateCls.getName() + " has a Query class but the candidate is invalid");
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#filter(org.datanucleus.query.typesafe.BooleanExpression)
     */
    public JDOQLTypedSubquery<T> filter(BooleanExpression expr)
    {
        discardCompiled();
        this.filter = (BooleanExpressionImpl)expr;
        return this;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#groupBy(org.datanucleus.query.typesafe.Expression[])
     */
    public JDOQLTypedSubquery<T> groupBy(Expression... exprs)
    {
        discardCompiled();
        if (exprs != null && exprs.length > 0)
        {
            grouping = new ArrayList<ExpressionImpl>();
            for (int i=0;i<exprs.length;i++)
            {
                grouping.add((ExpressionImpl)exprs[i]);
            }
        }
        return this;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#having(org.datanucleus.query.typesafe.Expression)
     */
    public JDOQLTypedSubquery<T> having(Expression expr)
    {
        discardCompiled();
        this.having = (ExpressionImpl)expr;
        return this;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#selectUnique(org.datanucleus.query.typesafe.NumericExpression)
     */
    public <S> NumericExpression<S> selectUnique(NumericExpression<S> expr)
    {
        return (NumericExpression<S>)internalSelect(expr, NumericExpressionImpl.class);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#selectUnique(org.datanucleus.query.typesafe.StringExpression)
     */
    public StringExpression selectUnique(StringExpression expr)
    {
        return (StringExpression)internalSelect(expr, StringExpressionImpl.class);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#selectUnique(org.datanucleus.query.typesafe.DateExpression)
     */
    public DateExpression selectUnique(DateExpression expr)
    {
        return (DateExpression)internalSelect(expr, DateExpressionImpl.class);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#selectUnique(org.datanucleus.query.typesafe.DateTimeExpression)
     */
    public DateTimeExpression selectUnique(DateTimeExpression expr)
    {
        return (DateTimeExpression)internalSelect(expr, DateTimeExpressionImpl.class);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#selectUnique(org.datanucleus.query.typesafe.TimeExpression)
     */
    public TimeExpression selectUnique(TimeExpression expr)
    {
        return (TimeExpression)internalSelect(expr, TimeExpressionImpl.class);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#selectUnique(org.datanucleus.query.typesafe.CharacterExpression)
     */
    public CharacterExpression selectUnique(CharacterExpression expr)
    {
        return (CharacterExpression)internalSelect(expr, CharacterExpressionImpl.class);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#select(org.datanucleus.query.typesafe.CollectionExpression)
     */
    public CollectionExpression select(CollectionExpression expr)
    {
        return (CollectionExpression)internalSelect(expr, CollectionExpressionImpl.class);
    }

    protected Expression internalSelect(Expression expr, Class implClass)
    {
        discardCompiled();
        this.result = new ArrayList<ExpressionImpl>();
        this.result.add((ExpressionImpl)expr);

        VariableExpression varExpr = new VariableExpression(getAlias());
        try
        {
            Constructor ctr = implClass.getConstructor(new Class[] {org.datanucleus.query.expression.Expression.class});
            return (Expression)ctr.newInstance(new Object[] {varExpr});
        }
        catch (NoSuchMethodException nsme)
        {
            throw new JDOException("Unable to create expression of type " + expr.getClass().getName() + " since required constructor doesnt exist");
        }
        catch (InvocationTargetException ite)
        {
            throw new JDOException("Unable to create expression of type " + expr.getClass().getName() + " due to error in constructor");
        }
        catch (IllegalAccessException iae)
        {
            throw new JDOException("Unable to create expression of type " + expr.getClass().getName() + " due to error in constructor");
        }
        catch (InstantiationException ie)
        {
            throw new JDOException("Unable to create expression of type " + expr.getClass().getName() + " due to error in constructor");
        }
    }

    /**
     * Method to return the single-string form of this JDOQL query.
     * @return Single-string form of the query
     */
    public String toString()
    {
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
                        str.append(JDOQLQueryHelper.getJDOQLForExpression(resultExpr.getQueryExpression()));
                        if (iter.hasNext())
                        {
                            str.append(",");
                        }
                    }
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

            if (type == QueryType.BULK_UPDATE)
            {
                str.append(" SET");
                Iterator<ExpressionImpl> exprIter = updateExprs.iterator();
                Iterator<ExpressionImpl> valIter = updateVals.iterator();
                while (exprIter.hasNext())
                {
                    ExpressionImpl expr = exprIter.next();
                    ExpressionImpl val = valIter.next();
                    str.append(" ").append(JDOQLQueryHelper.getJDOQLForExpression(expr.getQueryExpression()));
                    str.append(" = ").append(JDOQLQueryHelper.getJDOQLForExpression(val.getQueryExpression()));
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
                str.append(JDOQLQueryHelper.getJDOQLForExpression(filter.getQueryExpression()));
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
                        str.append(JDOQLQueryHelper.getJDOQLForExpression(groupExpr.getQueryExpression()));
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
                    str.append(JDOQLQueryHelper.getJDOQLForExpression(having.getQueryExpression()));
                }

                // Ordering
                if (ordering != null && !ordering.isEmpty())
                {
                    str.append(" ORDER BY ");
                    Iterator<OrderExpressionImpl> iter = ordering.iterator();
                    while (iter.hasNext())
                    {
                        OrderExpressionImpl orderExpr = iter.next();
                        str.append(JDOQLQueryHelper.getJDOQLForExpression(
                            ((ExpressionImpl)orderExpr.getExpression()).getQueryExpression()));
                        str.append(" " + (orderExpr.getDirection() == OrderDirection.ASC ? "ASCENDING" : "DESCENDING"));
                        if (iter.hasNext())
                        {
                            str.append(",");
                        }
                    }
                }
            }

            queryString = str.toString();
        }
        return queryString;
    }
}