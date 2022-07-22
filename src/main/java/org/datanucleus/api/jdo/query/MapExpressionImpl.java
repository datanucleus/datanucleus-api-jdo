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
import java.util.Map;
import java.util.Map.Entry;

import javax.jdo.query.BooleanExpression;
import javax.jdo.query.Expression;
import javax.jdo.query.MapExpression;
import javax.jdo.query.NumericExpression;
import javax.jdo.query.PersistableExpression;

import org.datanucleus.store.query.expression.InvokeExpression;
import org.datanucleus.store.query.expression.Literal;

/**
 * Implementation of a MapExpression
 */
public class MapExpressionImpl<T extends Map<K, V>, K, V> extends ExpressionImpl<T> implements MapExpression<T, K, V>
{
    public MapExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public MapExpressionImpl(Class<T> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.MapExpression#get(javax.jdo.query.Expression)
     */
    @Override
    public Expression<V> get(Expression<K> expr)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList<>();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "get", args);
        return new ExpressionImpl<V>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.MapExpression#get(java.lang.Object)
     */
    @Override
    public Expression<V> get(K key)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList<>();
        args.add(new Literal(key));
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "get", args);
        return new ExpressionImpl<V>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.MapExpression#containsEntry(java.util.Map.Entry)
     */
    public BooleanExpression containsEntry(Entry<K, V> entry)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList<>();
        args.add(new Literal(entry));
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "containsEntry", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.MapExpression#containsEntry(org.datanucleus.query.typesafe.Expression)
     */
    public BooleanExpression containsEntry(Expression<Entry<K, V>> expr)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList<>();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "containsEntry", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.MapExpression#containsKey(org.datanucleus.query.typesafe.Expression)
     */
    public BooleanExpression containsKey(Expression<K> expr)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList<>();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "containsKey", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.MapExpression#containsKey(java.lang.Object)
     */
    public BooleanExpression containsKey(K key)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList<>();
        args.add(new Literal(key));
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "containsKey", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.MapExpression#containsValue(org.datanucleus.query.typesafe.Expression)
     */
    public BooleanExpression containsValue(Expression<V> expr)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList<>();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "containsValue", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.MapExpression#containsValue(java.lang.Object)
     */
    public BooleanExpression containsValue(V value)
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList<>();
        args.add(new Literal(value));
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "containsValue", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.MapExpression#isEmpty()
     */
    public BooleanExpression isEmpty()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "isEmpty", null);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.MapExpression#size()
     */
    public NumericExpression<Integer> size()
    {
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "size", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }
}