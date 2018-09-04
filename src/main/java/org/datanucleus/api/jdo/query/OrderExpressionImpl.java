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

import javax.jdo.query.Expression;
import javax.jdo.query.OrderExpression;

/**
 * Implementation of an ordering expression.
 */
public class OrderExpressionImpl<T> implements OrderExpression<T>
{
    /** The ordering expression. */
    protected Expression orderExpr;

    /** The order direction for this expression. */
    protected OrderDirection direction;

    /** Positioning of nulls in the ordering. */
    protected OrderNullsPosition nullsPosition;

    public OrderExpressionImpl(Expression<T> expr, OrderDirection dir)
    {
        this.orderExpr = expr;
        this.direction = dir;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.OrderExpression#getDirection()
     */
    public OrderExpression.OrderDirection getDirection()
    {
        return direction;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.OrderExpression#getExpression()
     */
    public Expression<T> getExpression()
    {
        return orderExpr;
    }

    public OrderNullsPosition getNullsPosition()
    {
        return nullsPosition;
    }

    public OrderExpression<T> nullsFirst()
    {
        this.nullsPosition = OrderNullsPosition.FIRST;
        return this;
    }

    public OrderExpression<T> nullsLast()
    {
        this.nullsPosition = OrderNullsPosition.LAST;
        return this;
    }
}