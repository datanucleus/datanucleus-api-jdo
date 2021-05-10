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

import java.util.ArrayList;
import java.util.List;

import javax.jdo.query.BooleanExpression;
import javax.jdo.query.NumericExpression;
import javax.jdo.query.PersistableExpression;
import javax.jdo.query.geospatial.GeometryExpression;
import javax.jdo.query.geospatial.LineStringExpression;

import org.datanucleus.api.jdo.query.BooleanExpressionImpl;
import org.datanucleus.api.jdo.query.ExpressionImpl;
import org.datanucleus.api.jdo.query.ExpressionType;
import org.datanucleus.store.query.expression.Expression;
import org.datanucleus.store.query.expression.InvokeExpression;

/**
 * Implementation of a LineStringExpression.
 */
public class LineStringExpressionImpl<T> extends GeometryExpressionImpl<T> implements LineStringExpression<T>
{
    public LineStringExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public LineStringExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    public LineStringExpressionImpl(Class cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.LineStringExpression#isRing()
     */
    @Override
    public BooleanExpression isRing()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "isRing", null);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.LineStringExpression#getStartPoint()
     */
    @Override
    public GeometryExpression getStartPoint()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getStartPoint", null);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.LineStringExpression#getPointN(javax.jdo.query.NumericExpression)
     */
    @Override
    public GeometryExpression getPointN(NumericExpression position)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)position).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getPointN", args);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.LineStringExpression#getEndPoint()
     */
    @Override
    public GeometryExpression getEndPoint()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getEndPoint", null);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.LineStringExpression#isClosed()
     */
    @Override
    public BooleanExpression isClosed()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "isClosed", null);
        return new BooleanExpressionImpl(invokeExpr);
    }
}