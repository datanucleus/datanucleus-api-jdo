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

import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;

/**
 * Implementation of a NumericExpression.
 */
public class NumericExpressionImpl<T> extends ComparableExpressionImpl<Number> implements NumericExpression<T>
{
    public NumericExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public NumericExpressionImpl(org.datanucleus.query.expression.Expression queryExpr)
    {
        super(queryExpr);
    }

    public NumericExpressionImpl(Class<Number> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#add(org.datanucleus.query.typesafe.Expression)
     */
    public NumericExpression add(Expression expr)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_ADD, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#add(java.lang.Number)
     */
    public NumericExpression add(Number num)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = new Literal(num);

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_ADD, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#mul(org.datanucleus.query.typesafe.Expression)
     */
    public NumericExpression mul(Expression expr)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_MUL, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#mul(java.lang.Number)
     */
    public NumericExpression mul(Number num)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = new Literal(num);

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_MUL, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#sub(org.datanucleus.query.typesafe.Expression)
     */
    public NumericExpression sub(Expression expr)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_SUB, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#sub(java.lang.Number)
     */
    public NumericExpression sub(Number num)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = new Literal(num);

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_SUB, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#div(org.datanucleus.query.typesafe.Expression)
     */
    public NumericExpression div(Expression expr)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_DIV, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#div(java.lang.Number)
     */
    public NumericExpression div(Number num)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = new Literal(num);

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_DIV, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#mod(org.datanucleus.query.typesafe.Expression)
     */
    public NumericExpression mod(Expression expr)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_MOD, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#mod(java.lang.Number)
     */
    public NumericExpression mod(Number num)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = new Literal(num);

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_MOD, rightQueryExpr);
        return new NumericExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#avg()
     */
    public NumericExpression<T> avg()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "avg", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.Expression#sum()
     */
    public NumericExpression<T> sum()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "sum", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#abs()
     */
    public NumericExpression<T> abs()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "abs", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#sqrt()
     */
    public NumericExpression<T> sqrt()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "asin", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#acos()
     */
    public NumericExpression acos()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "acos", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#asin()
     */
    public NumericExpression asin()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "atan", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#atan()
     */
    public NumericExpression atan()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "sqrt", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#sin()
     */
    public NumericExpression sin()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "sin", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#cos()
     */
    public NumericExpression cos()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "cos", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#tan()
     */
    public NumericExpression tan()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "tan", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#exp()
     */
    public NumericExpression exp()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "exp", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#log()
     */
    public NumericExpression log()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "log", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#ceil()
     */
    public NumericExpression ceil()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "ceil", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.NumericExpression#floor()
     */
    public NumericExpression floor()
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(queryExpr);
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(null, "floor", args);
        return new NumericExpressionImpl<T>(invokeExpr);
    }
}