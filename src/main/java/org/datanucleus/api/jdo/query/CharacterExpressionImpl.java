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

import javax.jdo.query.CharacterExpression;
import javax.jdo.query.PersistableExpression;

import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.InvokeExpression;

/**
 * Implementation of a CharacterExpression
 */
public class CharacterExpressionImpl<T> extends ComparableExpressionImpl<Character> implements CharacterExpression
{
    public CharacterExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public CharacterExpressionImpl(Class<Character> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    public CharacterExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.CharacterExpression#toLowerCase()
     */
    @Override
    public CharacterExpression toLowerCase()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "toLowerCase", null);
        return new CharacterExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.CharacterExpression#toUpperCase()
     */
    @Override
    public CharacterExpression toUpperCase()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "toUpperCase", null);
        return new CharacterExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.CharacterExpression#neg()
     */
    @Override
    public CharacterExpression neg()
    {
        org.datanucleus.query.expression.Expression queryExpr = new DyadicExpression(org.datanucleus.query.expression.Expression.OP_NEG, this.queryExpr);
        return new CharacterExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.CharacterExpression#com()
     */
    @Override
    public CharacterExpression com()
    {
        org.datanucleus.query.expression.Expression queryExpr = new DyadicExpression(org.datanucleus.query.expression.Expression.OP_COM, this.queryExpr);
        return new CharacterExpressionImpl(queryExpr);
    }
}
