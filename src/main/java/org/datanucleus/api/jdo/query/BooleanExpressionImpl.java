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

import javax.jdo.query.BooleanExpression;
import javax.jdo.query.PersistableExpression;

import org.datanucleus.store.query.expression.DyadicExpression;
import org.datanucleus.store.query.expression.Expression;

/**
 * Implementation of a BooleanExpression
 */
public class BooleanExpressionImpl<T> extends ComparableExpressionImpl<Boolean> implements BooleanExpression
{
    public BooleanExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public BooleanExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    public BooleanExpressionImpl(Class<Boolean> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.BooleanExpression#and(org.datanucleus.query.typesafe.BooleanExpression)
     */
    public BooleanExpression and(BooleanExpression expr)
    {
        Expression leftQueryExpr = queryExpr;
        Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_AND, rightQueryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.BooleanExpression#not()
     */
    public BooleanExpression not()
    {
        Expression rightQueryExpr = queryExpr;

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(org.datanucleus.store.query.expression.Expression.OP_NOT, rightQueryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.BooleanExpression#or(org.datanucleus.query.typesafe.BooleanExpression)
     */
    public BooleanExpression or(BooleanExpression expr)
    {
        Expression leftQueryExpr = queryExpr;
        Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_OR, rightQueryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.BooleanExpression#neg()
     */
    @Override
    public BooleanExpression neg()
    {
        org.datanucleus.store.query.expression.Expression queryExpr = new DyadicExpression(org.datanucleus.store.query.expression.Expression.OP_NEG, this.queryExpr);
        return new BooleanExpressionImpl(queryExpr);
    }
}