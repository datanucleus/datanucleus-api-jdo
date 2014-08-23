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

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;

import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.query.typesafe.BooleanExpression;
import org.datanucleus.query.typesafe.CharacterExpression;
import org.datanucleus.query.typesafe.CollectionExpression;
import org.datanucleus.query.typesafe.DateExpression;
import org.datanucleus.query.typesafe.DateTimeExpression;
import org.datanucleus.query.typesafe.Expression;
import org.datanucleus.query.typesafe.NumericExpression;
import org.datanucleus.query.typesafe.PersistableExpression;
import org.datanucleus.query.typesafe.StringExpression;
import org.datanucleus.query.typesafe.TimeExpression;
import org.datanucleus.query.typesafe.TypesafeSubquery;

/**
 * Implementation of a typesafe subquery for JDO.
 * TODO JDOQL subqueries only allow result and filter, and JPQL subqueries allow result, filter, grouping, having.
 */
public class JDOTypesafeSubquery<T> extends AbstractTypesafeQuery<T> implements TypesafeSubquery<T>
{
    public JDOTypesafeSubquery(PersistenceManager pm, Class<T> candidateClass, String candidateAlias,
            JDOTypesafeQuery parentQuery)
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
        String qName = candName.substring(0, pos+1) + JDOTypesafeQuery.getQueryClassNameForClassName(candName.substring(pos+1));
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
    public TypesafeSubquery filter(BooleanExpression expr)
    {
        discardCompiled();
        this.filter = (BooleanExpressionImpl)expr;
        return this;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.TypesafeSubquery#groupBy(org.datanucleus.query.typesafe.Expression[])
     */
    public TypesafeSubquery groupBy(Expression... exprs)
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
    public TypesafeSubquery having(Expression expr)
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
        this.result = new ArrayList<ExpressionImpl>();
        this.result.add((ExpressionImpl)expr);

        VariableExpression varExpr = new VariableExpression(getAlias());
        try
        {
            Constructor ctr = implClass.getConstructor(
                new Class[] {org.datanucleus.query.expression.Expression.class});
            return (Expression)ctr.newInstance(new Object[] {varExpr});
        }
        catch (NoSuchMethodException nsme)
        {
            throw new JDOException("Unable to create expression of type " + expr.getClass().getName() +
                " since required constructor doesnt exist");
        }
        catch (InvocationTargetException ite)
        {
            throw new JDOException("Unable to create expression of type " + expr.getClass().getName() +
            " due to error in constructor");
        }
        catch (IllegalAccessException iae)
        {
            throw new JDOException("Unable to create expression of type " + expr.getClass().getName() +
            " due to error in constructor");
        }
        catch (InstantiationException ie)
        {
            throw new JDOException("Unable to create expression of type " + expr.getClass().getName() +
            " due to error in constructor");
        }
    }
}