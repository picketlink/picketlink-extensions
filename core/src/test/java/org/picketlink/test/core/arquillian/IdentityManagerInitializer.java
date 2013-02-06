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

package org.picketlink.test.core.arquillian;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.picketbox.core.event.InitializedEvent;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.internal.Password;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@ApplicationScoped
public class IdentityManagerInitializer {

    /**
     * <p>Initializes the store with some test data.</p>
     * @param identityManager
     */
    public static void initializeIdentityStore(@Observes InitializedEvent event) {
        IdentityManager identityManager = event.getPicketBoxManager().getIdentityManager();
        
        SimpleUser adminUser = new SimpleUser("admin");

        identityManager.add(adminUser);

        adminUser.setEmail("admin@picketbox.com");
        adminUser.setFirstName("The");
        adminUser.setLastName("Admin");

        identityManager.updateCredential(adminUser, new Password("admin".toCharArray()));

        Role roleManager = new SimpleRole("Manager");

        identityManager.add(roleManager);

        Group groupPicketBox = new SimpleGroup("PicketBox Group");

        identityManager.add(groupPicketBox);

        identityManager.grantRole(adminUser, roleManager);
        identityManager.addToGroup(adminUser, groupPicketBox);
    }

}
