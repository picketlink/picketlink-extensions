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

package org.picketlink.test.core.authentication;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.idm.model.User;
import org.picketlink.test.core.arquillian.ArchiveUtil;

/**
 * <p>
 * Tests user authentication.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AuthenticationTestCase extends AbstractAuthenticationTestCase {

    @Inject
    private WhoAmIService whoAmIService;

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

        archive.addPackages(true, AuthenticationTestCase.class.getPackage());

        return archive;
    }

    /**
     * <p>Tests a successful authentication.</p>
     *
     * @throws Exception
     */
    @Test
    public void testSuccessfullAuthentication() throws Exception {
        User user = this.whoAmIService.whoAmI();

        Assert.assertNotNull(user);
        Assert.assertEquals(USER_NAME, user.getLoginName());
    }
    
    @Test (expected=SecurityException.class)
    public void testAlreadyLoggedInException() throws Exception {
        this.identity.login();
    }

}