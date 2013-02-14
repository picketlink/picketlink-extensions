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