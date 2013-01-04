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
package org.aerogear.todo.server.security.authc.social.twitter;

import java.io.IOException;
import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.aerogear.todo.server.security.authc.AuthenticationResponse;
import org.picketbox.cdi.LoginCredential;
import org.picketbox.cdi.PicketBoxIdentity;
import org.picketbox.core.UserContext;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;

/**
 * Enables signin with facebook
 * 
 * @author anil saldhana
 * @since Sep 19, 2012
 */
@Stateless
@Path("/twitter")
public class TwitterSignInEndpoint {

    @Inject
    private PicketBoxIdentity identity;

    @Inject
    private LoginCredential credential;

    @Inject
    private IdentityManager identityManager;

    @GET
    public String login(@Context final HttpServletRequest request, @Context final HttpServletResponse response) throws IOException {
        if (this.identity.isLoggedIn()) {
            return null;
        }
        
        this.credential.setCredential(new TwitterCredential(request, response));
        
        this.identity.login();
        
        if (this.identity.isLoggedIn()) {
            provisionNewUser();
            return "<script>window.opener.sendMainPage();</script>";
        }
        
        return null;
    }

    /**
     * <p>Provision the authenticated user if he is not stored yes.</p>
     * 
     * TODO: user provisioning feature should be provided by PicketBox ? 
     */
    private void provisionNewUser() {
        TwitterPrincipal twitterPrincipal = getAuthenticatedPrincipal();
        
        //Check if the user exists in DB
        User storedUser = identityManager.getUser(twitterPrincipal.getName());
        
        if(storedUser == null){
            storedUser = new SimpleUser(twitterPrincipal.getName());
            
            storedUser.setFirstName(twitterPrincipal.getName());

            identityManager.add(storedUser);
            
            Role guest = new SimpleRole("guest");
            
            this.identityManager.add(guest);

            Group guests = new SimpleGroup("Guests");
            
            identityManager.add(guests);

            identityManager.grantRole(storedUser, guest);

            UserContext subject = this.identity.getUserContext();

            subject.setUser(storedUser);

            ArrayList<Role> roles = new ArrayList<Role>();

            roles.add(guest);

            subject.setRoles(roles);
        }
    }

    private TwitterPrincipal getAuthenticatedPrincipal() {
        UserContext userContext = identity.getUserContext();
        
        return (TwitterPrincipal) userContext.getPrincipal();
    }

    private AuthenticationResponse createSuccessfulAuthResponse() {
        AuthenticationResponse response = new AuthenticationResponse();
        
        response.setLoggedIn(this.identity.isLoggedIn());
        
        return response;
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public AuthenticationResponse getStatus(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException{
        if(identity.isLoggedIn()){
            User user = identity.getUser();
            AuthenticationResponse authResponse = createSuccessfulAuthResponse();
            authResponse.setLoggedIn(true);
            authResponse.setToken(user.getId());
            return authResponse;
        }
        return null;
    }
}