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
import javax.jdo.query.NumericExpression;
import javax.jdo.query.PersistableExpression;

import org.datanucleus.store.query.expression.DyadicExpression;
import org.datanucleus.store.query.expression.InvokeExpression;
import org.datanucleus.store.query.expression.Literal;

/**
 * Implementation of a NumericExpression.
 */
public class NumericExpressionImpl<T> extends ComparableExpressionImpl<Number> implements NumericExpression<T>
{
    public NumericExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public NumericExpressionImpl(org.datanucleus.store.query.expression.Expression queryExpr)
    {
        super(queryExpr);
    }

    public NumericExpressionImpl(Class<Number> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#add(javax.jdo.query.Expression)
     */
    public NumericExpression<T> add(Expression expr)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_ADD, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#add(java.lang.Number)
     */
    public NumericExpression<T> add(Number num)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = new Literal(num);

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_ADD, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#mul(javax.jdo.query.Expression)
     */
    public NumericExpression<T> mul(Expression expr)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_MUL, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#mul(java.lang.Number)
     */
    public NumericExpression<T> mul(Number num)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = new Literal(num);

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_MUL, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#sub(javax.jdo.query.Expression)
     */
    public NumericExpression<T> sub(Expression expr)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_SUB, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#sub(java.lang.Number)
     */
    public NumericExpression<T> sub(Number num)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = new Literal(num);

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_SUB, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#div(javax.jdo.query.Expression)
     */
    public NumericExpression<T> div(Expression expr)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_DIV, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#div(java.lang.Number)
     */
    public NumericExpression<T> div(Number num)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = new Literal(num);

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_DIV, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.Expression#mod(javax.jdo.query.Expression)
     */
    public NumericExpression<T> mod(Expression expr)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_MOD, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#mod(java.lang.Number)
     */
    public NumericExpression<T> mod(Number num)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = new Literal(num);

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_MOD, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#avg()
     */
    public NumericExpression<Double> avg()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "avg", args);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#avgDistinct()
     */
    @Override
    public NumericExpression<Double> avgDistinct()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(new DyadicExpression(org.datanucleus.store.query.expression.Expression.OP_DISTINCT, queryExpr));
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "avg", args);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#sum()
     */
    public NumericExpression<T> sum()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "sum", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#sumDistinct()
     */
    @Override
    public NumericExpression<T> sumDistinct()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(new DyadicExpression(org.datanucleus.store.query.expression.Expression.OP_DISTINCT, queryExpr));
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "sum", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#abs()
     */
    public NumericExpression<T> abs()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "abs", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#sqrt()
     */
    public NumericExpression<Double> sqrt()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "sqrt", args);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#acos()
     */
    public NumericExpression<Double> acos()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "acos", args);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#asin()
     */
    public NumericExpression<Double> asin()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "asin", args);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#atan()
     */
    public NumericExpression<Double> atan()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "atan", args);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#cos()
     */
    public NumericExpression<Double> cos()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "cos", args);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#sin()
     */
    public NumericExpression<Double> sin()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "sin", args);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#tan()
     */
    public NumericExpression<Double> tan()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "tan", args);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#exp()
     */
    public NumericExpression exp()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "exp", args);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#log()
     */
    public NumericExpression<Double> log()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "log", args);
        return new NumericExpressionImpl<Double>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#ceil()
     */
    public NumericExpression<T> ceil()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "ceil", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#floor()
     */
    public NumericExpression floor()
    {
        List<org.datanucleus.store.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.store.query.expression.Expression invokeExpr = new InvokeExpression(null, "floor", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#neg()
     */
    @Override
    public NumericExpression<T> neg()
    {
        org.datanucleus.store.query.expression.Expression queryExpr = new DyadicExpression(org.datanucleus.store.query.expression.Expression.OP_NEG, this.queryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#com()
     */
    @Override
    public NumericExpression<T> com()
    {
        org.datanucleus.store.query.expression.Expression queryExpr = new DyadicExpression(org.datanucleus.store.query.expression.Expression.OP_COM, this.queryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#bAnd(javax.jdo.query.NumericExpression)
     */
    @Override
    public NumericExpression<T> bAnd(NumericExpression bitExpr)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = ((ExpressionImpl)bitExpr).getQueryExpression();

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_BIT_AND, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#bOr(javax.jdo.query.NumericExpression)
     */
    @Override
    public NumericExpression<T> bOr(NumericExpression bitExpr)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = ((ExpressionImpl)bitExpr).getQueryExpression();

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_BIT_OR, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see javax.jdo.query.NumericExpression#bXor(javax.jdo.query.NumericExpression)
     */
    @Override
    public NumericExpression<T> bXor(NumericExpression bitExpr)
    {
        org.datanucleus.store.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.store.query.expression.Expression rightQueryExpr = ((ExpressionImpl)bitExpr).getQueryExpression();

        org.datanucleus.store.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.store.query.expression.Expression.OP_BIT_XOR, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }
}