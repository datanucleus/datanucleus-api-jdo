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
import java.util.List;

import javax.jdo.JDOException;
import javax.jdo.query.BooleanExpression;
import javax.jdo.query.Expression;
import javax.jdo.query.NumericExpression;
import javax.jdo.query.PersistableExpression;

import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;

/**
 * Implementation of the methods for Expression, to be extended by the XXXExpressionImpl classes.
 */
public class ExpressionImpl<T> implements Expression<T>
{
    /** The generic query expression that represents this typesafe expression. */
    protected org.datanucleus.query.expression.Expression queryExpr;

    /** Type of this expression. */
    protected ExpressionType exprType = ExpressionType.PATH;

    /**
     * Constructor, extending a parent, and accessing a member of that parent.
     * @param parent The parent expression (or null, if candidate)
     * @param name Name of the member to access
     */
    public ExpressionImpl(PersistableExpression parent, String name)
    {
        List<String> tuples = new ArrayList<String>();
        if (parent != null)
        {
            org.datanucleus.query.expression.Expression parentQueryExpr = ((ExpressionImpl)parent).getQueryExpression();
            if (parentQueryExpr instanceof PrimaryExpression)
            {
                tuples.addAll(((PrimaryExpression) parentQueryExpr).getTuples());
                tuples.add(name);
                queryExpr = new PrimaryExpression(parentQueryExpr.getLeft(), tuples);
            }
            else
            {
                tuples.add(name);
                queryExpr = new PrimaryExpression(parentQueryExpr, tuples);
            }
        }
        else
        {
            tuples.add(name);
            queryExpr = new PrimaryExpression(tuples);
        }
    }

    /**
     * Constructor for a parameter or variable of this type.
     * @param cls The type of the parameter/variable
     * @param name Name of the member to access
     * @param type The type, whether parameter or variable
     */
    public ExpressionImpl(Class cls, String name, ExpressionType type)
    {
        if (type == ExpressionType.PARAMETER || type == ExpressionType.VARIABLE)
        {
            exprType = type;
        }
        else
        {
            throw new JDOException("Should not have called this constructor of ExpressionImpl!");
        }

        if (exprType == ExpressionType.PARAMETER)
        {
            queryExpr = new ParameterExpression(name, cls);
        }
        else if (exprType == ExpressionType.VARIABLE)
        {
            queryExpr = new VariableExpression(name, cls);
        }
    }

    /**
     * Constructor taking in the query expression being represented.
     * @param queryExpr The (generic) query expression
     */
    public ExpressionImpl(org.datanucleus.query.expression.Expression queryExpr)
    {
        this.queryExpr = queryExpr;
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.Expression#as(java.lang.String)
     */
    @Override
    public Expression<T> as(String alias)
    {
        this.queryExpr.setAlias(alias);
        return this;
    }

    /**
     * Accessor for the underlying (generic) query expression.
     * @return The query expression
     */
    public org.datanucleus.query.expression.Expression getQueryExpression()
    {
        return queryExpr;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#isParameter()
     */
    public boolean isParameter()
    {
        return (exprType == ExpressionType.PARAMETER);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#isVariable()
     */
    public boolean isVariable()
    {
        return (exprType == ExpressionType.VARIABLE);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#eq(org.datanucleus.query.typesafe.Expression)
     */
    public BooleanExpression eq(Expression expr)
    {
        if (expr == null)
        {
            // Assume they meant to compare with NULL Literal
            return this.eq((T)null);
        }

        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        return new BooleanExpressionImpl(new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_EQ, rightQueryExpr));
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#eq(java.lang.Object)
     */
    public BooleanExpression eq(T t)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = new Literal(t);

        return new BooleanExpressionImpl(new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_EQ, rightQueryExpr));
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#ne(org.datanucleus.query.typesafe.Expression)
     */
    public BooleanExpression ne(Expression expr)
    {
        if (expr == null)
        {
            // Assume they meant to compare with NULL Literal
            return this.ne((T)null);
        }

        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        return new BooleanExpressionImpl(new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_NOTEQ, rightQueryExpr));
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#ne(java.lang.Object)
     */
    public BooleanExpression ne(T t)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = new Literal(t);

        return new BooleanExpressionImpl(new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_NOTEQ, rightQueryExpr));
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#instanceOf(java.lang.Class)
     */
    public BooleanExpression instanceOf(Class cls)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = new Literal(cls);

        return new BooleanExpressionImpl(new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_IS, rightQueryExpr));
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#cast(java.lang.Class)
     */
    public Expression cast(Class cls)
    {
        if (this instanceof PersistableExpressionImpl)
        {
            return new PersistableExpressionImpl(new DyadicExpression(queryExpr, org.datanucleus.query.expression.Expression.OP_CAST, new Literal(cls.getName())));
        }
        else if (this instanceof DateExpressionImpl)
        {
            return new DateExpressionImpl(new DyadicExpression(queryExpr, org.datanucleus.query.expression.Expression.OP_CAST, new Literal(cls.getName())));
        }
        else if (this instanceof TimeExpressionImpl)
        {
            return new TimeExpressionImpl(new DyadicExpression(queryExpr, org.datanucleus.query.expression.Expression.OP_CAST, new Literal(cls.getName())));
        }
        else if (this instanceof DateTimeExpressionImpl)
        {
            return new DateTimeExpressionImpl(new DyadicExpression(queryExpr, org.datanucleus.query.expression.Expression.OP_CAST, new Literal(cls.getName())));
        }
        throw new UnsupportedOperationException("cast not yet supported for expression of type " + this.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#count()
     */
    public NumericExpression<Long> count()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "count", args);
        return new NumericExpressionImpl<Long>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#countDistinct()
     */
    public NumericExpression<Long> countDistinct()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new DyadicExpression(org.datanucleus.query.expression.Expression.OP_DISTINCT, queryExpr));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "count", args);
        return new NumericExpressionImpl<Long>(invokeExpr);
    }
}