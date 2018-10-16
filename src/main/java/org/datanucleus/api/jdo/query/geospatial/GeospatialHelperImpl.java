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
import javax.jdo.query.ObjectExpression;
import javax.jdo.query.StringExpression;
import javax.jdo.query.geospatial.GeometryCollectionExpression;
import javax.jdo.query.geospatial.GeometryExpression;
import javax.jdo.query.geospatial.GeospatialHelper;
import javax.jdo.query.geospatial.LineStringExpression;
import javax.jdo.query.geospatial.MultiLineStringExpression;
import javax.jdo.query.geospatial.MultiPointExpression;
import javax.jdo.query.geospatial.MultiPolygonExpression;
import javax.jdo.query.geospatial.PointExpression;
import javax.jdo.query.geospatial.PolygonExpression;

import org.datanucleus.api.jdo.query.ExpressionImpl;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;

/**
 * Implementation of a Geospatial helper for JDOQLTypedQuery.
 */
public class GeospatialHelperImpl implements GeospatialHelper
{
    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#geometryFromText(javax.jdo.query.StringExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public GeometryExpression geometryFromText(StringExpression wktExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wktExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.geomFromText", args);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#geometryFromText(java.lang.String, java.lang.Integer)
     */
    @Override
    public GeometryExpression geometryFromText(String wkt, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkt));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.geomFromText", args);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#geometryCollFromText(javax.jdo.query.StringExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public GeometryCollectionExpression geometryCollFromText(StringExpression wktExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wktExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.geomCollFromText", args);
        return new GeometryCollectionExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#geometryCollFromText(java.lang.String, java.lang.Integer)
     */
    @Override
    public GeometryCollectionExpression geometryCollFromText(String wkt, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkt));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.geomCollFromText", args);
        return new GeometryCollectionExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#pointFromText(javax.jdo.query.StringExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public PointExpression pointFromText(StringExpression wktExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wktExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.pointFromText", args);
        return new PointExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#pointFromText(java.lang.String, java.lang.Integer)
     */
    @Override
    public PointExpression pointFromText(String wkt, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkt));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.pointFromText", args);
        return new PointExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#lineStringFromText(javax.jdo.query.StringExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public LineStringExpression lineStringFromText(StringExpression wktExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wktExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.lineFromText", args);
        return new LineStringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#lineStringFromText(java.lang.String, java.lang.Integer)
     */
    @Override
    public LineStringExpression lineStringFromText(String wkt, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkt));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.lineFromText", args);
        return new LineStringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#polygonFromText(javax.jdo.query.StringExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public PolygonExpression polygonFromText(StringExpression wktExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wktExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.polyFromText", args);
        return new PolygonExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#polygonFromText(java.lang.String, java.lang.Integer)
     */
    @Override
    public PolygonExpression polygonFromText(String wkt, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkt));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.polyFromText", args);
        return new PolygonExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiPointFromText(javax.jdo.query.StringExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public MultiPointExpression multiPointFromText(StringExpression wktExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wktExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mPointFromText", args);
        return new MultiPointExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiPointFromText(java.lang.String, java.lang.Integer)
     */
    @Override
    public MultiPointExpression multiPointFromText(String wkt, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkt));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mPointFromText", args);
        return new MultiPointExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiLineStringFromText(javax.jdo.query.StringExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public MultiLineStringExpression multiLineStringFromText(StringExpression wktExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wktExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mLineFromText", args);
        return new MultiLineStringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiLineStringFromText(java.lang.String, java.lang.Integer)
     */
    @Override
    public MultiLineStringExpression multiLineStringFromText(String wkt, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkt));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mLineFromText", args);
        return new MultiLineStringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiPolygonFromText(javax.jdo.query.StringExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public MultiPolygonExpression multiPolygonFromText(StringExpression wktExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wktExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mPolyFromText", args);
        return new MultiPolygonExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiPolygonFromText(java.lang.String, java.lang.Integer)
     */
    @Override
    public MultiPolygonExpression multiPolygonFromText(String wkt, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkt));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mPolyFromText", args);
        return new MultiPolygonExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#geometryFromWKB(javax.jdo.query.ObjectExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public GeometryExpression geometryFromWKB(ObjectExpression wkbExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wkbExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.geomFromWKB", args);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#geometryFromWKB(java.lang.Object, java.lang.Integer)
     */
    @Override
    public GeometryExpression geometryFromWKB(Object wkb, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkb));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.geomFromWKB", args);
        return new GeometryExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#geometryCollFromWKB(javax.jdo.query.ObjectExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public GeometryCollectionExpression geometryCollFromWKB(ObjectExpression wkbExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wkbExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.geomCollFromWKB", args);
        return new GeometryCollectionExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#geometryCollFromWKB(java.lang.Object, java.lang.Integer)
     */
    @Override
    public GeometryCollectionExpression geometryCollFromWKB(Object wkb, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkb));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.geomCollFromWKB", args);
        return new GeometryCollectionExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#pointFromWKB(javax.jdo.query.ObjectExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public PointExpression pointFromWKB(ObjectExpression wkbExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wkbExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.pointFromWKB", args);
        return new PointExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#pointFromWKB(java.lang.Object, java.lang.Integer)
     */
    @Override
    public PointExpression pointFromWKB(Object wkb, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkb));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.pointFromWKB", args);
        return new PointExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#lineStringFromWKB(javax.jdo.query.ObjectExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public LineStringExpression lineStringFromWKB(ObjectExpression wkbExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wkbExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.lineFromWKB", args);
        return new LineStringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#lineStringFromWKB(java.lang.Object, java.lang.Integer)
     */
    @Override
    public LineStringExpression lineStringFromWKB(Object wkb, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkb));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.lineFromWKB", args);
        return new LineStringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#polygonFromWKB(javax.jdo.query.ObjectExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public PolygonExpression polygonFromWKB(ObjectExpression wkbExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wkbExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.polyFromWKB", args);
        return new PolygonExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#polygonFromWKB(java.lang.Object, java.lang.Integer)
     */
    @Override
    public PolygonExpression polygonFromWKB(Object wkb, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkb));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.polyFromWKB", args);
        return new PolygonExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiPointFromWKB(javax.jdo.query.ObjectExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public MultiPointExpression multiPointFromWKB(ObjectExpression wkbExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wkbExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mPointFromWKB", args);
        return new MultiPointExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiPointFromWKB(java.lang.Object, java.lang.Integer)
     */
    @Override
    public MultiPointExpression multiPointFromWKB(Object wkb, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkb));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mPointFromWKB", args);
        return new MultiPointExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiLineStringFromWKB(javax.jdo.query.ObjectExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public MultiLineStringExpression multiLineStringFromWKB(ObjectExpression wkbExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wkbExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mLineFromWKB", args);
        return new MultiLineStringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiLineStringFromWKB(java.lang.Object, java.lang.Integer)
     */
    @Override
    public MultiLineStringExpression multiLineStringFromWKB(Object wkb, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkb));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mLineFromWKB", args);
        return new MultiLineStringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiPolygonFromWKB(javax.jdo.query.ObjectExpression, javax.jdo.query.NumericExpression)
     */
    @Override
    public MultiPolygonExpression multiPolygonFromWKB(ObjectExpression wkbExpr, NumericExpression<Integer> sridExpr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)wkbExpr).getQueryExpression());
        args.add(((ExpressionImpl)sridExpr).getQueryExpression());

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mPolyFromWKB", args);
        return new MultiPolygonExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.geospatial.GeospatialHelper#multiPolygonFromWKB(java.lang.Object, java.lang.Integer)
     */
    @Override
    public MultiPolygonExpression multiPolygonFromWKB(Object wkb, Integer srid)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(wkb));
        args.add(new Literal(srid));

        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "Spatial.mPolyFromWKB", args);
        return new MultiPolygonExpressionImpl(invokeExpr);
    }
}