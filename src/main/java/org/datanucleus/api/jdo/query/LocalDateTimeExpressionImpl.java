/**********************************************************************
Copyright (c) 2015 Andy Jefferson and others. All rights reserved.
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

import java.time.LocalDateTime;

import javax.jdo.query.NumericExpression;
import javax.jdo.query.PersistableExpression;

import org.datanucleus.api.jdo.query.ComparableExpressionImpl;
import org.datanucleus.api.jdo.query.ExpressionType;
import org.datanucleus.api.jdo.query.NumericExpressionImpl;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.InvokeExpression;

/**
 * Implementation of a DateTime expression.
 */
public class LocalDateTimeExpressionImpl extends ComparableExpressionImpl<LocalDateTime> implements LocalDateTimeExpression
{
    public LocalDateTimeExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public LocalDateTimeExpressionImpl(Class<LocalDateTime> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    public LocalDateTimeExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.jdo.query.LocalDateTimeExpression#getDayOfMonth()
     */
    public NumericExpression<Integer> getDayOfMonth()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getDayOfMonth", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.jdo.query.LocalDateTimeExpression#getHour()
     */
    public NumericExpression<Integer> getHour()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getHour", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.jdo.query.LocalDateTimeExpression#getMinute()
     */
    public NumericExpression<Integer> getMinute()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getMinute", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.jdo.query.LocalDateTimeExpression#getMonthValue()
     */
    public NumericExpression<Integer> getMonthValue()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getMonthValue", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.jdo.query.LocalDateTimeExpression#getSecond()
     */
    public NumericExpression<Integer> getSecond()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getSecond", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.jdo.query.LocalDateTimeExpression#getYear()
     */
    public NumericExpression<Integer> getYear()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getYear", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }
}