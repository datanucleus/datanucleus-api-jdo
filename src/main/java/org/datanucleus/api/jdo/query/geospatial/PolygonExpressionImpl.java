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

import javax.jdo.query.NumericExpression;
import javax.jdo.query.PersistableExpression;
import javax.jdo.query.geospatial.GeometryExpression;
import javax.jdo.query.geospatial.PolygonExpression;

import org.datanucleus.api.jdo.query.ExpressionImpl;
import org.datanucleus.api.jdo.query.ExpressionType;
import org.datanucleus.api.jdo.query.NumericExpressionImpl;
import org.datanucleus.store.query.expression.Expression;
import org.datanucleus.store.query.expression.InvokeExpression;

/**
 * Implementation of a PolygonExpression.
 */
public class PolygonExpressionImpl<T> extends GeometryExpressionImpl<T> implements PolygonExpression<T>
{
    public PolygonExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public PolygonExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    public PolygonExpressionImpl(Class<T> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.PolygonExpression#getExteriorRing()
     */
    @Override
    public GeometryExpression getExteriorRing()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getExteriorRing", null);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.PolygonExpression#getNumInteriorRings()
     */
    @Override
    public NumericExpression getNumInteriorRings()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getNumInteriorRings", null);
        return new NumericExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.PolygonExpression#getInteriorRingN(javax.jdo.query.NumericExpression)
     */
    @Override
    public GeometryExpression getInteriorRingN(NumericExpression position)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)position).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getInteriorRingN", args);
        return new GeometryExpressionImpl(invokeExpr);
    }
}
