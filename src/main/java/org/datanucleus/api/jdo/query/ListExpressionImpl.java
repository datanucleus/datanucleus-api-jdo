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

import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.typesafe.Expression;
import org.datanucleus.query.typesafe.ListExpression;
import org.datanucleus.query.typesafe.NumericExpression;
import org.datanucleus.query.typesafe.PersistableExpression;

/**
 * Implementation of a ListExpression.
 */
public class ListExpressionImpl<T extends List<E>, E> extends CollectionExpressionImpl<T, E> 
    implements ListExpression<T, E>
{
    public ListExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public ListExpressionImpl(Class<T> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.ListExpression#get(int)
     */
    public Expression get(int pos)
    {
        List args = new ArrayList();
        args.add(new Literal(pos));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "get", args);
        return new ExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.ListExpression#get(org.datanucleus.query.typesafe.NumericExpression)
     */
    public Expression get(NumericExpression<Integer> posExpr)
    {
        List args = new ArrayList();
        args.add(((ExpressionImpl)posExpr).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "get", args);
        return new ExpressionImpl(invokeExpr);
    }
}