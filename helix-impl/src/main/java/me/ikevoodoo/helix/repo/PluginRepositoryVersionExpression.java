package me.ikevoodoo.helix.repo;

import com.github.zafarkhaja.semver.expr.CompositeExpression;
import com.github.zafarkhaja.semver.expr.Expression;

public class PluginRepositoryVersionExpression extends CompositeExpression {

    private final String expressionString;

    public PluginRepositoryVersionExpression(Expression expr, String expressionString) {
        super(expr);
        this.expressionString = expressionString;
    }

    public String getExpressionString() {
        return this.expressionString;
    }
}
