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
import javax.jdo.query.ObjectExpression;
import javax.jdo.query.PersistableExpression;
import javax.jdo.query.StringExpression;
import javax.jdo.query.geospatial.GeometryExpression;

import org.datanucleus.api.jdo.query.BooleanExpressionImpl;
import org.datanucleus.api.jdo.query.ComparableExpressionImpl;
import org.datanucleus.api.jdo.query.ExpressionImpl;
import org.datanucleus.api.jdo.query.ExpressionType;
import org.datanucleus.api.jdo.query.NumericExpressionImpl;
import org.datanucleus.api.jdo.query.ObjectExpressionImpl;
import org.datanucleus.api.jdo.query.StringExpressionImpl;
import org.datanucleus.store.query.expression.Expression;
import org.datanucleus.store.query.expression.InvokeExpression;

/**
 * Implementation of a GeometryExpression.
 */
public class GeometryExpressionImpl<T> extends ComparableExpressionImpl<T> implements GeometryExpression<T>
{
    public GeometryExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public GeometryExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    public GeometryExpressionImpl(Class cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getEnvelope()
     */
    @Override
    public GeometryExpression getEnvelope()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getEnvelope", null);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getDimension()
     */
    @Override
    public NumericExpression getDimension()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getDimension", null);
        return new NumericExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getBoundary()
     */
    @Override
    public GeometryExpression getBoundary()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getBoundary", null);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getSRID()
     */
    @Override
    public NumericExpression getSRID()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getSRID", null);
        return new NumericExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#isSimple()
     */
    @Override
    public BooleanExpression isSimple()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "isSimple", null);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#isEmpty()
     */
    @Override
    public BooleanExpression isEmpty()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "isEmpty", null);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#toText()
     */
    @Override
    public StringExpression toText()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "toText", null);
        return new StringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#toBinary()
     */
    @Override
    public ObjectExpression toBinary()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "toBinary", null);
        return new ObjectExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getGeometryType()
     */
    @Override
    public StringExpression getGeometryType()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getGeometryType", null);
        return new StringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#contains(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public BooleanExpression contains(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "contains", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#overlaps(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public BooleanExpression overlaps(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "overlaps", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#touches(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public BooleanExpression touches(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "touches", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#crosses(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public BooleanExpression crosses(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "crosses", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#within(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public BooleanExpression within(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "within", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#intersects(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public BooleanExpression intersects(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "intersects", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#equals(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public BooleanExpression equals(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "equals", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#disjoint(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public BooleanExpression disjoint(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "disjoint", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#relate(javax.jdo.query.geospatial.GeometryExpression, javax.jdo.query.StringExpression)
     */
    @Override
    public BooleanExpression relate(GeometryExpression geom, StringExpression pattern)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "relate", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#distance(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public NumericExpression distance(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "distance", args);
        return new NumericExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#intersection(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public GeometryExpression intersection(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "intersection", args);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#buffer(javax.jdo.query.NumericExpression)
     */
    @Override
    public GeometryExpression buffer(NumericExpression dist)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)dist).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "buffer", args);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#convexHull()
     */
    @Override
    public GeometryExpression convexHull()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "convexHull", null);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#symDifference(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public GeometryExpression symDifference(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "symDifference", args);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#difference(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public GeometryExpression difference(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "difference", args);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#union(javax.jdo.query.geospatial.GeometryExpression)
     */
    @Override
    public GeometryExpression union(GeometryExpression geom)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)geom).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "union", args);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getLength()
     */
    @Override
    public NumericExpression getLength()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getLength", null);
        return new NumericExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getNumPoints()
     */
    @Override
    public NumericExpression getNumPoints()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getNumPoints", null);
        return new NumericExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getCentroid()
     */
    @Override
    public GeometryExpression getCentroid()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getCentroid", null);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getArea()
     */
    @Override
    public NumericExpression getArea()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getArea", null);
        return new NumericExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getPointOnSurface()
     */
    @Override
    public GeometryExpression getPointOnSurface()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getPointOnSurface", null);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getNumGeometries()
     */
    @Override
    public NumericExpression getNumGeometries()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getNumGeometries", null);
        return new NumericExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeometryExpression#getGeometryN(javax.jdo.query.NumericExpression)
     */
    @Override
    public GeometryExpression getGeometryN(NumericExpression position)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)position).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "getGeometryN", args);
        return new GeometryExpressionImpl(invokeExpr);
    }
}