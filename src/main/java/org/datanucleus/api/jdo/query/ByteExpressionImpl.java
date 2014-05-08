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

import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.typesafe.ByteExpression;
import org.datanucleus.query.typesafe.PersistableExpression;

/**
 * Implementation of a ByteExpression
 */
public class ByteExpressionImpl<T> extends ComparableExpressionImpl<Byte> implements ByteExpression
{
    public ByteExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public ByteExpressionImpl(Class<Byte> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    public ByteExpressionImpl(Expression queryExpr)
    {
        super(queryExpr);
    }
}