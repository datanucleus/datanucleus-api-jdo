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

import javax.jdo.query.Expression;
import javax.jdo.query.PersistableExpression;

import org.datanucleus.query.expression.InvokeExpression;

/**
 * (Base) implementation of a persistable expression.
 * This will be extended by the "Q" class implementations of PersistableExpression to add on public fields
 * that equate to the fields/properties of the persistable class.
 */
public class PersistableExpressionImpl<T> extends ExpressionImpl<T> implements PersistableExpression<T>
{
    public PersistableExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public PersistableExpressionImpl(Class<T> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    public PersistableExpressionImpl(org.datanucleus.query.expression.Expression queryExpr)
    {
        super(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.PersistableExpression#jdoObjectId()
     */
    public Expression jdoObjectId()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "JDOHelper.getObjectId", args);
        return new ObjectExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.PersistableExpression#jdoVersion()
     */
    public Expression jdoVersion()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "JDOHelper.getVersion", args);
        return new ObjectExpressionImpl(invokeExpr);
    }
}