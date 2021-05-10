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

import java.time.LocalDate;

import javax.jdo.query.NumericExpression;
import javax.jdo.query.PersistableExpression;

import org.datanucleus.store.query.expression.Expression;
import org.datanucleus.store.query.expression.InvokeExpression;

import javax.jdo.query.LocalDateExpression;

/**
 * Implementation of a LocalDate expression.
 */
public class LocalDateExpressionImpl extends ComparableExpressionImpl<LocalDate> implements LocalDateExpression
{
    public LocalDateExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public LocalDateExpressionImpl(Class<LocalDate> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    public LocalDateExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.LocalDateExpression#getDayOfMonth()
     */
    public NumericExpression<Integer> getDayOfMonth()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getDayOfMonth", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.LocalDateExpression#getMonthValue()
     */
    public NumericExpression<Integer> getMonthValue()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getMonthValue", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.LocalDateExpression#getYear()
     */
    public NumericExpression<Integer> getYear()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getYear", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }
}