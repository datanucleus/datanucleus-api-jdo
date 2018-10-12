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

import javax.jdo.query.PersistableExpression;
import javax.jdo.query.geospatial.MultiPointExpression;

import org.datanucleus.api.jdo.query.ExpressionType;
import org.datanucleus.query.expression.Expression;

/**
 * Implementation of a MultiPointExpression.
 */
public class MultiPointExpressionImpl<T> extends GeometryCollectionExpressionImpl<T> implements MultiPointExpression<T>
{
    public MultiPointExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public MultiPointExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }

    public MultiPointExpressionImpl(Class cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }
}
