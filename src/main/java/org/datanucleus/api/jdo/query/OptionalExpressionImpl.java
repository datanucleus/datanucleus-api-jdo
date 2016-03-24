/**********************************************************************
Copyright (c) 2016 Andy Jefferson and others. All rights reserved.
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
import javax.jdo.query.OptionalExpression;
import javax.jdo.query.PersistableExpression;

import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.InvokeExpression;

/**
 * Implementation of an Optional expression.
 */
public class OptionalExpressionImpl<T> extends ComparableExpressionImpl<java.util.Optional<T>> implements OptionalExpression<T>
{
    public OptionalExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public OptionalExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.OptionalExpression#get()
     */
    @Override
    public javax.jdo.query.Expression<T> get()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "get", null);
        // Would be nice to return NumericExpressionImpl when T implies that, or StringExpressionImpl, etc.
        return new ExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.OptionalExpression#isPresent()
     */
    @Override
    public BooleanExpression isPresent()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "isPresent", null);
        return new BooleanExpressionImpl(invokeExpr);
    }
}