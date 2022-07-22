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
package org.datanucleus.api.jdo.query.geospatial;

import javax.jdo.query.BooleanExpression;
import javax.jdo.query.PersistableExpression;
import javax.jdo.query.geospatial.MultiLineStringExpression;

import org.datanucleus.api.jdo.query.BooleanExpressionImpl;
import org.datanucleus.api.jdo.query.ExpressionType;
import org.datanucleus.store.query.expression.Expression;
import org.datanucleus.store.query.expression.InvokeExpression;

/**
 * Implementation of a MultiLineStringExpression.
 */
public class MultiLineStringExpressionImpl<T> extends GeometryCollectionExpressionImpl<T> implements MultiLineStringExpression<T>
{
    public MultiLineStringExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public MultiLineStringExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    public MultiLineStringExpressionImpl(Class<T> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.LinearRingExpression#isClosed()
     */
    @Override
    public BooleanExpression isClosed()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "isClosed", null);
        return new BooleanExpressionImpl(invokeExpr);
    }
}
