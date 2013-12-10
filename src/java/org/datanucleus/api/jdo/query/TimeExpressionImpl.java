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

import java.util.Date;

import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.typesafe.NumericExpression;
import org.datanucleus.query.typesafe.PersistableExpression;
import org.datanucleus.query.typesafe.TimeExpression;

/**
 * Implementation of a Time expression.
 */
public class TimeExpressionImpl<T> extends ComparableExpressionImpl<Date> implements TimeExpression<Date>
{
    public TimeExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public TimeExpressionImpl(Class<Date> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    public TimeExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.DateExpression#getHour()
     */
    public NumericExpression<Integer> getHour()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getHour", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.DateExpression#getMinute()
     */
    public NumericExpression<Integer> getMinute()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getMinute", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.DateExpression#getSecond()
     */
    public NumericExpression<Integer> getSecond()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getSecond", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }
}