/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.extensions.util;

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
