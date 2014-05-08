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

import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.typesafe.BooleanExpression;
import org.datanucleus.query.typesafe.ComparableExpression;
import org.datanucleus.query.typesafe.NumericExpression;
import org.datanucleus.query.typesafe.OrderExpression;
import org.datanucleus.query.typesafe.PersistableExpression;
import org.datanucleus.query.typesafe.OrderExpression.OrderDirection;

/**
 * Implementation of the methods for ComparableExpression.
 */
public class ComparableExpressionImpl<T> extends ExpressionImpl<T> implements ComparableExpression<T>
{
    public ComparableExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public ComparableExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    public ComparableExpressionImpl(Class<T> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#gt(org.datanucleus.query.typesafe.ComparableExpression)
     */
    public BooleanExpression gt(org.datanucleus.query.typesafe.ComparableExpression expr)
    {
        Expression leftQueryExpr = queryExpr;
        Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_GT, rightQueryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#gt(java.lang.Object)
     */
    public BooleanExpression gt(T t)
    {
        Expression leftQueryExpr = queryExpr;
        Expression rightQueryExpr = new Literal(t);

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_GT, rightQueryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#gteq(org.datanucleus.query.typesafe.ComparableExpression)
     */
    public BooleanExpression gteq(org.datanucleus.query.typesafe.ComparableExpression expr)
    {
        Expression leftQueryExpr = queryExpr;
        Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_GTEQ, rightQueryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#gteq(java.lang.Object)
     */
    public BooleanExpression gteq(T t)
    {
        Expression leftQueryExpr = queryExpr;
        Expression rightQueryExpr = new Literal(t);

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_GTEQ, rightQueryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#lt(org.datanucleus.query.typesafe.ComparableExpression)
     */
    public BooleanExpression lt(org.datanucleus.query.typesafe.ComparableExpression expr)
    {
        Expression leftQueryExpr = queryExpr;
        Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_LT, rightQueryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#lt(java.lang.Object)
     */
    public BooleanExpression lt(T t)
    {
        Expression leftQueryExpr = queryExpr;
        Expression rightQueryExpr = new Literal(t);

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_LT, rightQueryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#lteq(org.datanucleus.query.typesafe.ComparableExpression)
     */
    public BooleanExpression lteq(org.datanucleus.query.typesafe.ComparableExpression expr)
    {
        Expression leftQueryExpr = queryExpr;
        Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_LTEQ, rightQueryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#lteq(java.lang.Object)
     */
    public BooleanExpression lteq(T t)
    {
        Expression leftQueryExpr = queryExpr;
        Expression rightQueryExpr = new Literal(t);

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_LTEQ, rightQueryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#max()
     */
    public NumericExpression max()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "max", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#min()
     */
    public NumericExpression min()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "min", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#asc()
     */
    public OrderExpression asc()
    {
        return new OrderExpressionImpl(this, OrderDirection.ASC);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#desc()
     */
    public OrderExpression desc()
    {
        return new OrderExpressionImpl(this, OrderDirection.DESC);
    }
}