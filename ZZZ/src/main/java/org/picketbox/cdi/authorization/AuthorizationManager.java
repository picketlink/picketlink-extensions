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

package org.picketbox.cdi.authorization;

import static org.picketbox.cdi.util.AnnotationUtil.getDeclaredAnnotation;

import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.InvocationContext;

import org.apache.deltaspike.security.api.authorization.annotation.Secures;
import org.picketbox.cdi.PicketBoxIdentity;
import org.picketlink.Identity;

/**
 * <p>
 * Provides all authorization capabilities for applications using PicketBox.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@ApplicationScoped
public class AuthorizationManager {

    /**
     * <p>
     * Authorization method for the {@link RolesAllowed} annotation.
     * </p>
     *
     * @param ctx
     * @param identity
     * @return
     */
    @Secures
    @RolesAllowed
    public boolean restrictRoles(InvocationContext ctx, PicketBoxIdentity identity) {
        if (!identity.isLoggedIn()) {
            return false;
        }


        String[] restrictedRoles = getRestrictedRoles(ctx);

        for (String restrictedRole : restrictedRoles) {
            if (identity.hasRole(restrictedRole)) {
                return true;
            }
        }

        return false;
    }

    /**
     * <p>Checks if the resources protected with the {@link UserLoggedIn} annotation are visible only for authenticated users.</p>
     *
     * @param ctx
     * @param identity
     * @return
     */
    @Secures
    @UserLoggedIn
    public boolean isUserLoggedIn(InvocationContext ctx, Identity identity) {
        return identity.isLoggedIn();
    }

    /**
     * <p>
     * Returns the restricted roles defined by the use of the {@link RolesAllowed} annotation. If the annotation is not
     * present a empty array is returned.
     * </p>
     *
     * @param ctx
     * @return
     */
    private String[] getRestrictedRoles(InvocationContext ctx) {
        RolesAllowed restrictedRolesAnnotation = getDeclaredAnnotation(RolesAllowed.class, ctx);

        if (restrictedRolesAnnotation != null) {
            return restrictedRolesAnnotation.value();
        }

        return new String[] {};
    }

}
