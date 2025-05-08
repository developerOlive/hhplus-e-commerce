package com.hhplusecommerce.support.lock;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
/**
 * SpEL 기반의 동적 락 키 생성 담당
 */
@Component
public class LockKeyGenerator {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    public String generateKey(Method method, Object[] args, String prefix, String keyExpression) {
        String[] paramNames = nameDiscoverer.getParameterNames(method);
        StandardEvaluationContext context = new StandardEvaluationContext();

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        String dynamicKey = parser.parseExpression(keyExpression).getValue(context, String.class);
        return "lock:" + prefix + ":" + dynamicKey;
    }
}
