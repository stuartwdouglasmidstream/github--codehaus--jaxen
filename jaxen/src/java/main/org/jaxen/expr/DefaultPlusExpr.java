
package org.jaxen.expr;

import org.jaxen.Context;
import org.jaxen.JaxenException;

import org.jaxen.function.NumberFunction;

class DefaultPlusExpr extends DefaultAdditiveExpr
{
    public DefaultPlusExpr(Expr lhs,
                           Expr rhs)
    {
        super( lhs,
               rhs );
    }

    public String getOperator()
    {
        return "+";
    }

    public Object evaluate(Context context) throws JaxenException
    {
        Number lhsValue = NumberFunction.evaluate( getLHS().evaluate( context ),
                                                   context.getNavigator() );
        Number rhsValue = NumberFunction.evaluate( getRHS().evaluate( context ),
                                                   context.getNavigator() );

        if ( lhsValue instanceof Double
             ||
             rhsValue instanceof Double )
        {
            double result = lhsValue.doubleValue() + rhsValue.doubleValue();

            return new Double( result );
        }

        int result = lhsValue.intValue() + rhsValue.intValue();

        return new Integer( result );
    }
}
