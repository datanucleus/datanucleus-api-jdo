/**********************************************************************
Copyright (c) 2018 Andy Jefferson and others. All rights reserved.
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
import javax.jdo.query.Expression;
import javax.jdo.query.IfElseExpression;

import org.datanucleus.query.expression.CaseExpression;
import org.datanucleus.query.expression.Literal;

/**
 * Implementation of an IfElseExpression.
 * Generates an underlying generic CaseExpression.
 */
public class IfElseExpressionImpl<T> extends ComparableExpressionImpl<T> implements IfElseExpression<T>
{
    CaseExpression caseExpr = null;

    public IfElseExpressionImpl(Class<T> type)
    {
        super(new CaseExpression());
        caseExpr = (CaseExpression) queryExpr;
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.IfElseExpression#ifThen(javax.jdo.query.BooleanExpression, java.lang.Object)
     */
    @Override
    public IfElseExpression<T> ifThen(BooleanExpression ifExpr, T value)
    {
        caseExpr.addCondition(((BooleanExpressionImpl)ifExpr).getQueryExpression(), new Literal(value));
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.IfElseExpression#ifThen(javax.jdo.query.BooleanExpression, javax.jdo.query.Expression)
     */
    @Override
    public IfElseExpression<T> ifThen(BooleanExpression ifExpr, Expression<T> valueExpr)
    {
        caseExpr.addCondition(((BooleanExpressionImpl)ifExpr).getQueryExpression(), ((ExpressionImpl)valueExpr).getQueryExpression());
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.IfElseExpression#elseEnd(java.lang.Object)
     */
    @Override
    public IfElseExpression<T> elseEnd(T value)
    {
        caseExpr.setElseExpression(new Literal(value));
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.IfElseExpression#elseEnd(javax.jdo.query.Expression)
     */
    @Override
    public IfElseExpression<T> elseEnd(Expression<T> valueExpr)
    {
        caseExpr.setElseExpression(((ExpressionImpl)valueExpr).getQueryExpression());
        return this;
    }
}