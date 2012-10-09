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

package org.aerogear.todo.server.security.authc.otp;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.aerogear.todo.server.security.authc.AuthenticationRequest;
import org.aerogear.todo.server.security.authc.AuthenticationResponse;
import org.picketbox.cdi.PicketBoxIdentity;
import org.picketlink.cdi.credential.Credential;
import org.picketlink.cdi.credential.LoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * <p>JAX-RS Endpoint to authenticate users using otp.</p>
 * @author anil saldhana
 * @author Pedro Silva
 */
@Stateless
@Path("/otp")
@TransactionAttribute
public class OTPSignInEndpoint {

    @Inject
    private PicketBoxIdentity identity;
    
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
        
        credential.setCredential(new Credential<OTPCredential>() {

            @Override
            public OTPCredential getValue() {
                return new OTPCredential(authcRequest.getUserId(), authcRequest.getPassword(), authcRequest.getOtp());
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
            
            response.setToken(this.identity.getUserContext().getSession().getId().getId().toString());
        }
        
        return response;
    }
    
}