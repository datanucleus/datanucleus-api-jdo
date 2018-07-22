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

import javax.jdo.query.BooleanExpression;
import javax.jdo.query.CharacterExpression;
import javax.jdo.query.Expression;
import javax.jdo.query.NumericExpression;
import javax.jdo.query.PersistableExpression;
import javax.jdo.query.StringExpression;

import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;

/**
 * Implementation of a StringExpression
 */
public class StringExpressionImpl extends ComparableExpressionImpl<String> implements StringExpression
{
    public StringExpressionImpl(PersistableExpression parent, String name)
    {
        super(parent, name);
    }

    public StringExpressionImpl(org.datanucleus.query.expression.Expression queryExpr)
    {
        super(queryExpr);
    }

    public StringExpressionImpl(Class<String> cls, String name, ExpressionType type)
    {
        super(cls, name, type);
    }

    /**
     * Method to return an expression for this expression added to the passed expression (String concatenation).
     * @param expr The other expression
     * @return The summation
     */
    public StringExpression add(Expression expr)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = ((ExpressionImpl)expr).getQueryExpression();

        org.datanucleus.query.expression.Expression queryExpr =
            new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_ADD, rightQueryExpr);
        return new StringExpressionImpl(queryExpr);
    }

    /**
     * Method to return an expression for this string added to the passed expression (String concatenation).
     * @param str The other string
     * @return The summation
     */
    public StringExpression add(String str)
    {
        org.datanucleus.query.expression.Expression leftQueryExpr = queryExpr;
        org.datanucleus.query.expression.Expression rightQueryExpr = new Literal(str);

        org.datanucleus.query.expression.Expression queryExpr =
                new DyadicExpression(leftQueryExpr, org.datanucleus.query.expression.Expression.OP_ADD, rightQueryExpr);
        return new StringExpressionImpl(queryExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#charAt(int)
     */
    public CharacterExpression charAt(int pos)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(pos));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "charAt", args);
        return new CharacterExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#charAt(org.datanucleus.query.typesafe.NumericExpression)
     */
    public CharacterExpression charAt(NumericExpression pos)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)pos).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "charAt", args);
        return new CharacterExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#endsWith(java.lang.String)
     */
    public BooleanExpression endsWith(String str)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(str));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "endsWith", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#endsWith(org.datanucleus.query.typesafe.StringExpression)
     */
    public BooleanExpression endsWith(StringExpression expr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "endsWith", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#equalsIgnoreCase(java.lang.String)
     */
    public BooleanExpression equalsIgnoreCase(String str)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(str));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "equalsIgnoreCase", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#equalsIgnoreCase(org.datanucleus.query.typesafe.StringExpression)
     */
    public BooleanExpression equalsIgnoreCase(StringExpression expr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "equalsIgnoreCase", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#indexOf(java.lang.String, int)
     */
    public NumericExpression indexOf(String str, int pos)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(str));
        args.add(new Literal(pos));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "indexOf", args);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#indexOf(java.lang.String, org.datanucleus.query.typesafe.NumericExpression)
     */
    public NumericExpression<Integer> indexOf(String str, NumericExpression pos)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(str));
        args.add(((ExpressionImpl)pos).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "indexOf", args);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#indexOf(java.lang.String)
     */
    public NumericExpression<Integer> indexOf(String str)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(str));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "indexOf", args);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#indexOf(org.datanucleus.query.typesafe.StringExpression, int)
     */
    public NumericExpression<Integer> indexOf(StringExpression expr, int pos)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        args.add(new Literal(pos));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "indexOf", args);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#indexOf(org.datanucleus.query.typesafe.StringExpression, org.datanucleus.query.typesafe.NumericExpression)
     */
    public NumericExpression<Integer> indexOf(StringExpression expr, NumericExpression pos)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        args.add(((ExpressionImpl)pos).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "indexOf", args);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#indexOf(org.datanucleus.query.typesafe.StringExpression)
     */
    public NumericExpression<Integer> indexOf(StringExpression expr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "indexOf", args);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#length()
     */
    public NumericExpression<Integer> length()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "length", null);
        return new NumericExpressionImpl<Integer>(invokeExpr);
    }

    public BooleanExpression matches(StringExpression expr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "matches", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    public BooleanExpression matches(String str)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(str));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "matches", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#startsWith(java.lang.String)
     */
    public BooleanExpression startsWith(String str)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(str));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "startsWith", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#startsWith(org.datanucleus.query.typesafe.StringExpression)
     */
    public BooleanExpression startsWith(StringExpression expr)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)expr).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "startsWith", args);
        return new BooleanExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#substring(int, int)
     */
    public StringExpression substring(int startPos, int endPos)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(startPos));
        args.add(new Literal(endPos));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "substring", args);
        return new StringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#substring(int)
     */
    public StringExpression substring(int pos)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(new Literal(pos));
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "substring", args);
        return new StringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#substring(org.datanucleus.query.typesafe.NumericExpression, org.datanucleus.query.typesafe.NumericExpression)
     */
    public StringExpression substring(NumericExpression<Integer> startPos, NumericExpression<Integer> endPos)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)startPos).getQueryExpression());
        args.add(((ExpressionImpl)endPos).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "substring", args);
        return new StringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#substring(org.datanucleus.query.typesafe.NumericExpression)
     */
    public StringExpression substring(NumericExpression<Integer> pos)
    {
        List<org.datanucleus.query.expression.Expression> args = new ArrayList();
        args.add(((ExpressionImpl)pos).getQueryExpression());
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "substring", args);
        return new StringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#toLowerCase()
     */
    public StringExpression toLowerCase()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "toLowerCase", null);
        return new StringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#toUpperCase()
     */
    public StringExpression toUpperCase()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "toUpperCase", null);
        return new StringExpressionImpl(invokeExpr);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.query.typesafe.StringExpression#trim()
     */
    public StringExpression trim()
    {
        org.datanucleus.query.expression.Expression invokeExpr = new InvokeExpression(queryExpr, "trim", null);
        return new StringExpressionImpl(invokeExpr);
    }
}