/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.extensions.core.pbox.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;

/**
 * <p>Utility class with common methods to handle Java Annotations.<p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AnnotationUtil {

    /**
     * <p>
     * Returns the an {@link Annotation} instance giving its class. The annotation will be looked up on method and type levels,
     * only.
     * </p>
     *
     * @param annotationClass
     * @param ctx
     * @return
     */
    public static <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass, InvocationContext ctx) {
        Method method = ctx.getMethod();
        Class<?> type = method.getDeclaringClass();

        if (method.isAnnotationPresent(annotationClass)) {
            return method.getAnnotation(annotationClass);
        }

        if (type.isAnnotationPresent(annotationClass)) {
            return type.getAnnotation(annotationClass);
        }

        return null;
    }

}
