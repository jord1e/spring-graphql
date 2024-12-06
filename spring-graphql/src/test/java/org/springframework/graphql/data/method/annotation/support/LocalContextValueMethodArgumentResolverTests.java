/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.graphql.data.method.annotation.support;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.graphql.Book;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.LocalContextValue;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ContextValueMethodArgumentResolver}.
 *
 * @author Rossen Stoyanchev
 */
public class LocalContextValueMethodArgumentResolverTests {

    @Nested
    public class ImplicitName extends NestedMethodTest {
        public ImplicitName() {
            super("handleDefault", "localBook");
        }
    }
    @Nested
    public class HandleValueArgument extends NestedMethodTest {
        public HandleValueArgument() {
            super("handleValue", CONTEXT_VALUE_KEY);
        }
    }
    @Nested
    public class HandleNameArgument extends NestedMethodTest {
        public HandleNameArgument() {
            super("handleDefault", CONTEXT_NAME_KEY);
        }
    }

    abstract static class NestedMethodTest {
        public NestedMethodTest(String methodName, String contextName) {
            this.contextTargetName = contextName;
            this.method = ClassUtils.getMethod(
                    LocalContextValueMethodArgumentResolverTests.class, methodName, (Class<?>[]) null);
        }

        private final String contextTargetName;

        private final Method method;

        private final LocalContextValueMethodArgumentResolver resolver = new LocalContextValueMethodArgumentResolver();

        private final Book book = new Book();


        @Test
        void supportsParameter() {
            assertThat(this.resolver.supportsParameter(methodParam(0))).isFalse();
            assertThat(this.resolver.supportsParameter(methodParam(1))).isTrue();
        }

        @Test
        void resolve() {
            GraphQLContext context = GraphQLContext.newContext().of(contextTargetName, this.book).build();
            Object actual = resolveValue(context, 1);

            assertThat(actual).isSameAs(this.book);
        }

        @Nullable
        private Object resolveValue(@Nullable GraphQLContext localContext, int index) {

            DataFetchingEnvironment environment = DataFetchingEnvironmentImpl.newDataFetchingEnvironment()
                    .localContext(localContext)
                    .graphQLContext(GraphQLContext.newContext().build())
                    .build();

            return this.resolver.resolveArgument(methodParam(index), environment);
        }

        private MethodParameter methodParam(int index) {
            MethodParameter methodParameter = new SynthesizingMethodParameter(method, index);
            methodParameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
            return methodParameter;
        }
    }


    @SuppressWarnings("unused")
    public void handleDefault(
            @ContextValue Book book,
            @LocalContextValue Book handleDefault) {
    }

    private static final String CONTEXT_VALUE_KEY = "fizz";

    @SuppressWarnings("unused")
    public void handleValue(
            @ContextValue Book book,
            @LocalContextValue(CONTEXT_VALUE_KEY) Book localBook) {
    }

    private static final String CONTEXT_NAME_KEY = "buzz";

    @SuppressWarnings("unused")
    public void handleName(
            @ContextValue Book book,
            @LocalContextValue(name = CONTEXT_NAME_KEY) Book localBook) {
    }

}
