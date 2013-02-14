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

package org.picketlink.test.core.idm;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketbox.core.authentication.credential.UsernamePasswordCredential;
import org.picketbox.core.identity.jpa.EntityManagerPropagationContext;
import org.picketlink.extensions.core.pbox.LoginCredential;
import org.picketlink.extensions.core.pbox.PicketBoxIdentity;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.test.core.arquillian.ArchiveUtil;

/**
 * <p>Test for the PicketLink IDM support.</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@RunWith(Arquillian.class)
public class IdentityManagerTestCase {
    
    private EntityManagerFactory entityManagerFactory;
    
    protected static final String USER_NAME = "someUser";
    protected static final String USER_PASSWORD = "123";

    @Inject
    private PicketBoxIdentity identity;

    @Inject
    private LoginCredential credential;

    @Inject
    private IdentityManager identityManager;

    @Before
    public void onSetup() throws Exception {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("picketbox-testing-pu");

        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        EntityManagerPropagationContext.set(entityManager);
    }

    @After
    public void onFinish() throws Exception {
        EntityManager entityManager = EntityManagerPropagationContext.get();

        entityManager.flush();
        entityManager.getTransaction().commit();
        entityManager.close();

        EntityManagerPropagationContext.clear();
        this.entityManagerFactory.close();
    }

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

        archive.addPackages(true, IdentityManagerTestCase.class.getPackage());

        return archive;
    }

    /**
     * <p>Creates an user using the PicketLink IDM API and performs an authentication.</p>
     *
     * @throws Exception
     */
    @Test
    public void testAuthentication() throws Exception {
        SimpleUser user = new SimpleUser(USER_NAME);

        this.identityManager.add(user);

        user.setEmail("someUser@picketbox.com");
        user.setFirstName("Some");
        user.setLastName("User");

        this.identityManager.updateCredential(user, new Password(USER_PASSWORD.toCharArray()));

        Role roleDeveloper = new SimpleRole("developer");
        
        identityManager.add(roleDeveloper);
        
        Role roleAdmin = new SimpleRole("admin");
        
        this.identityManager.add(roleAdmin);

        Group testingGroup = new SimpleGroup("PicketBox Testing Group");
        
        this.identityManager.add(testingGroup);

        this.identityManager.grantRole(user, roleDeveloper);
        this.identityManager.grantRole(user, roleAdmin);
        
        this.identityManager.addToGroup(user, testingGroup);

        populateUserCredential();

        this.identity.login();

        Assert.assertTrue(this.identity.isLoggedIn());

        Assert.assertTrue(this.identity.hasRole(roleDeveloper.getName()));
        Assert.assertTrue(this.identity.hasRole(roleAdmin.getName()));
        Assert.assertTrue(this.identity.hasGroup(testingGroup.getName()));
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