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

package org.aerogear.todo.server.security.authc;

import java.security.Principal;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.picketlink.idm.IdentityManager;
import org.jboss.picketlink.idm.model.Group;
import org.jboss.picketlink.idm.model.Role;
import org.jboss.picketlink.idm.model.User;
import org.picketbox.core.PicketBoxPrincipal;
import org.picketbox.core.authentication.AbstractAuthenticationManager;
import org.picketbox.core.authentication.AuthenticationManager;
import org.picketbox.core.exceptions.AuthenticationException;

/**
 * <p>{@link AuthenticationManager} that uses the PicketLink IDM to check user credentials.</p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@ApplicationScoped
public class IDMAuthenticationManager extends AbstractAuthenticationManager {

    @Inject
    private Instance<org.jboss.picketlink.idm.IdentityManager> identityManager;
    
    /* (non-Javadoc)
     * @see org.picketbox.core.authentication.AbstractAuthenticationManager#authenticate(java.lang.String, java.lang.Object)
     */
    @Override
    public Principal authenticate(String username, Object credential) throws AuthenticationException {
        User user = getIdentityManager().getUser(username);
        
        if (user != null) {
            String password = user.getAttribute("password");
            
            if (password.equals(credential.toString())) {
                return new PicketBoxPrincipal(username);
            }
        }
        
        return null;
    }
 
    @PostConstruct
    public void loadUsers() {
        IdentityManager identityManager = getIdentityManager();
        
        User user = identityManager.createUser("abstractj");
        
        user.setEmail("abstractj@aerogear.com");
        user.setFirstName("Bruno");
        user.setLastName("Oliveira");
        
        user.setAttribute("password", "123");
        
        Role roleDeveloper = identityManager.createRole("developer");
        Role roleAdmin = identityManager.createRole("admin");
        
        Group groupCoreDeveloper = identityManager.createGroup("Core Developers");
        
        identityManager.grantRole(roleDeveloper, user, groupCoreDeveloper);
        identityManager.grantRole(roleAdmin, user, groupCoreDeveloper);
    }
    
    private org.jboss.picketlink.idm.IdentityManager getIdentityManager() {
        try {
            return this.identityManager.get();
        } finally {
        }
    }

}