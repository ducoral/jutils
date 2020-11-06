package com.github.ducoral.jutils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.nonNull;

class Mocked<T> implements Core.Mock<T>, InvocationHandler {

    private final ClassLoader loader;

    private final Class<?>[] interfaces;

    private final T mock;

    private final Map<String, Object> returns = new HashMap<>();

    private final AtomicReference<String> signature = new AtomicReference<>();

    public Core.Mock<T> returns(Function<T, ?> function) {
        Object returnValue = function.apply(mock);
        if (nonNull(signature.get()))
            returns.put(signature.get(), returnValue);
        signature.set(null);
        return this;
    }

    @SuppressWarnings("unchecked")
    public T done() {
        return (T) newProxyInstance(loader, interfaces, (proxy, method, args) -> returns.get(signature(method, args)));
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        signature.set(signature(method, args));
        return null;
    }

    private String signature(Method method, Object[] args) {
        StringBuilder builder = new StringBuilder(method.getName());
        for (Object arg : args)
            builder.append(':').append(arg);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    Mocked(Class<T> type) {
        loader = Mocked.class.getClassLoader();
        interfaces = new Class[]{type};
        mock = (T) newProxyInstance(loader, interfaces,this);
    }
}
