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

package org.picketlink.test.core.authorization;

import javax.inject.Inject;

import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.extensions.core.pbox.authorization.RolesAllowed;
import org.picketlink.test.core.arquillian.ArchiveUtil;
import org.picketlink.test.core.authentication.AbstractAuthenticationTestCase;

/**
 * <p>
 * Tests some simple method authorization scenarios. Basically, the tests asserts if the {@link ProtectedService} methods are
 * being protected by the roles defined in the {@link RolesAllowed} annotation.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AuthorizationTestCase extends AbstractAuthenticationTestCase {

    @Inject
    private ProtectedService protectedService;

    /**
     * <p>
     * Creates a simple {@link WebArchive} for deployment with the necessary structure/configuration to run the tests.
     * </p>
     *
     * @return
     */
    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ArchiveUtil.createTestArchive();

        archive.addPackages(true, AuthorizationTestCase.class.getPackage());

        return archive;
    }

    /**
     * <p>
     * Tests if an invocation for a unprotected method is allowed.
     * </p>
     */
    @Test
    public void testUnProtectedOperation() {
        this.protectedService.unProtectedMethod();
    }

    /**
     * <p>
     * Tests if an invocation for a protected method is allowed, considering that the authenticated user has the required roles.
     * </p>
     */
    @Test
    public void testSuccessfullAuthorization() {
        this.protectedService.onlyForManagersOperation();
    }

    /**
     * <p>Tests if an invocation for a protected method is denied, considering that the authenticated user do not have the required roles.</p>
     */
    @Test(expected = AccessDeniedException.class)
    public void testFailedAuthorization() {
        this.protectedService.onlyForExecutives();
    }

    /**
     * <p>Tests if an invocation for a protected method is denied, considering that the user is not authenticated.</p>
     */
    @Test(expected = AccessDeniedException.class)
    public void testUserNotAuthenticated() {
        // forces a logout, so we can test if the method is restricted for authenticated users.
        this.identity.logout();
        this.protectedService.onlyForAuthenticatedUsers();
    }

}