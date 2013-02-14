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

import static junit.framework.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.authentication.credential.UsernamePasswordCredential;
import org.picketlink.Identity;
import org.picketlink.extensions.core.pbox.LoginCredential;
import org.picketlink.test.core.arquillian.AbstractArquillianTestCase;

/**
 * <p>Base class for test cases that needs to have an authenticated user during the tests.</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractAuthenticationTestCase extends AbstractArquillianTestCase {

    protected static final String USER_NAME = "admin";
    protected static final String USER_PASSWORD = "admin";

    @Inject
    protected Identity identity;

    @Inject
    protected LoginCredential credential;
    
    @Inject
    private PicketBoxManager picketBoxManager;

    /**
     * <p>Authenticates the user</p>
     *
     * @throws Exception
     */
    @Before
    public void onSetup() throws Exception {
        populateUserCredential();
        this.identity.login();
        assertTrue(this.identity.isLoggedIn());
    }

    /**
     * <p>Logouts the user after a test execution.</p>
     */
    @After
    public void onFinish() {
        this.identity.logout();
    }

    /**
     * <p>
     * Populates the {@link LoginCredential} with the username and password.
     * </p>
     */
    private void populateUserCredential() {
        this.credential.setCredential(new UsernamePasswordCredential(USER_NAME, USER_PASSWORD));
    }

}
