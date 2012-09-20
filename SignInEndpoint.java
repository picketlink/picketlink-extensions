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

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.picketlink.cdi.Identity;
import org.jboss.picketlink.cdi.credential.Credential;
import org.jboss.picketlink.cdi.credential.LoginCredentials;
import org.jboss.picketlink.idm.IdentityManager;
import org.jboss.picketlink.idm.model.Group;
import org.jboss.picketlink.idm.model.Role;
import org.jboss.picketlink.idm.model.User;
import org.picketbox.cdi.PicketBoxUser;
import org.picketbox.core.authentication.credential.UsernamePasswordCredential;

/**
 * <p>JAX-RS Endpoint to authenticate users.</p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Stateless
@Path("/signin")
@TransactionAttribute
public class SignInEndpoint {

    @Inject
    private Identity identity;
    
    @Inject
    private LoginCredentials credential;
    
    @Inject
    private IdentityManager identityManager;
    
    /**
     * <p>Loads some users during the first construction.</p>
     */
    @PostConstruct
    public void loadUsers() {
        User abstractj = this.identityManager.createUser("abstractj");

        abstractj.setEmail("abstractj@aerogear.com");
        abstractj.setFirstName("Bruno");
        abstractj.setLastName("Oliveira");
        
        this.identityManager.updatePassword(abstractj, "123");
        
        Role roleDeveloper = this.identityManager.createRole("developer");
        Role roleAdmin = this.identityManager.createRole("admin");

        Group groupCoreDeveloper = identityManager.createGroup("Core Developers");

        identityManager.grantRole(roleDeveloper, abstractj, groupCoreDeveloper);
        identityManager.grantRole(roleAdmin, abstractj, groupCoreDeveloper);
        
        User guest = this.identityManager.createUser("guest");

        guest.setEmail("guest@aerogear.com");
        guest.setFirstName("Guest");
        guest.setLastName("User");

        this.identityManager.updatePassword(guest, "123");
        
        Role roleGuest = this.identityManager.createRole("guest");
        
        identityManager.grantRole(roleGuest, guest, groupCoreDeveloper);
    }

    
    /**
     * <p>Performs the authentication using the informations provided by the {@link AuthenticationRequest}</p>
     * 
     * @param authcRequest
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AuthenticationResponse login(final AuthenticationRequest authcRequest) {
        if (this.identity.isLoggedIn()) {
            return createResponse(authcRequest);
        }
        
        credential.setCredential(new Credential<UsernamePasswordCredential>() {

            @Override
            public UsernamePasswordCredential getValue() {
                return new UsernamePasswordCredential(authcRequest.getUserId(), authcRequest.getPassword());
            }
        });
        
        this.identity.login();

        return createResponse(authcRequest);
    }

    private AuthenticationResponse createResponse(AuthenticationRequest authcRequest) {
        AuthenticationResponse response = new AuthenticationResponse();
        
        response.setUserId(authcRequest.getUserId());
        response.setLoggedIn(this.identity.isLoggedIn());
        
        if (response.isLoggedIn()) {
            PicketBoxUser user = (PicketBoxUser) this.identity.getUser();
            
            response.setToken(user.getSubject().getSession().getId().getId().toString());
        }
        
        return response;
    }
    
}