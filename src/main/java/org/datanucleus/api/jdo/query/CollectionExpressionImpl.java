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
import java.util.Collection;
import java.util.List;

import javax.jdo.query.BooleanExpression;
import javax.jdo.query.CollectionExpression;
import javax.jdo.query.Expression;
import javax.jdo.query.NumericExpression;
import javax.jdo.query.PersistableExpression;

import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;

/**
 * Implementation of a CollectionExpression
 */
public class CollectionExpressionImpl<T extends Collection<E>, E> extends ExpressionImpl<T> implements CollectionExpression<T, E>
{
    public CollectionExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public CollectionExpressionImpl(Class<T> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.CollectionExpression#contains(java.lang.Object)
     */
    public BooleanExpression contains(E elem)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(elem));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "contains", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.CollectionExpression#contains(org.datanucleus.query.typesafe.Expression)
     */
    public BooleanExpression contains(Expression<E> expr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "contains", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.CollectionExpression#isEmpty()
     */
    public BooleanExpression isEmpty()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "isEmpty", null);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.CollectionExpression#size()
     */
    public NumericExpression<Integer> size()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "size", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }
}