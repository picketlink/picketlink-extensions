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

package org.picketbox.cdi.util;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.PasswordCredential;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.file.internal.FileUser;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class IdentityManagerInitializer {

    /**
     * <p>Initializes the store with some test data.</p>
     */
    public static void initializeIdentityStore() {
        FileBasedIdentityStore theStore = new FileBasedIdentityStore();

        theStore.setAlwaysCreateFiles(false);

        IdentityManager identityManager = new DefaultIdentityManager(theStore);

        FileUser adminUser = new FileUser("admin");

        identityManager.createUser(adminUser);

        adminUser.setEmail("admin@picketbox.com");
        adminUser.setFirstName("The");
        adminUser.setLastName("Admin");

        identityManager.updateCredential(adminUser, new PasswordCredential("admin"));

        Role roleManager = identityManager.createRole("Manager");

        Group groupCoreDeveloper = identityManager.createGroup("PicketBox Group");

        identityManager.grantRole(roleManager, adminUser, groupCoreDeveloper);
    }

}
