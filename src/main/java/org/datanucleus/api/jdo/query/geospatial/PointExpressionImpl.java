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

import javax.jdo.query.NumericExpression;
import javax.jdo.query.PersistableExpression;
import javax.jdo.query.geospatial.PointExpression;

import org.datanucleus.api.jdo.query.ExpressionType;
import org.datanucleus.api.jdo.query.NumericExpressionImpl;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.InvokeExpression;

/**
 * Implementation of a PointExpression.
 */
public class PointExpressionImpl<T> extends GeometryExpressionImpl<T> implements PointExpression<T>
{
    public PointExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public PointExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    public PointExpressionImpl(Class cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.PointExpression#getX()
     */
    @Override
    public NumericExpression<Double> getX()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getX", null);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.PointExpression#getY()
     */
    @Override
    public NumericExpression<Double> getY()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getY", null);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }
}
