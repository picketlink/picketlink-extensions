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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.aerogear.todo.server.security.authc.fb.FacebookCredentialCredential;
import org.aerogear.todo.server.security.authc.fb.FacebookProcessor;
import org.jboss.picketlink.cdi.Identity;
import org.jboss.picketlink.cdi.credential.Credential;
import org.jboss.picketlink.cdi.credential.LoginCredentials;
import org.jboss.picketlink.idm.IdentityManager;
import org.jboss.picketlink.idm.model.Group;
import org.jboss.picketlink.idm.model.Role;
import org.jboss.picketlink.idm.model.User;

/**
 * Enables signin with facebook
 * 
 * @author anil saldhana
 * @since Sep 19, 2012
 */
@Stateless
@Path("/facebook")
public class FacebookSignInEndpoint {

    @Resource
    ServletContext context;

    @Inject
    private Identity identity;

    @Inject
    private LoginCredentials credential;

    @Inject
    private IdentityManager identityManager;

    protected String returnURL;
    protected String clientID;
    protected String clientSecret;
    protected String scope = "email";

    protected List<String> roles = new ArrayList<String>();
    protected FacebookProcessor processor;

    public FacebookSignInEndpoint() {
        clientID = System.getProperty("FB_CLIENT_ID");
        clientSecret = System.getProperty("FB_CLIENT_SECRET");
        returnURL = System.getProperty("FB_RETURN_URL");
        roles.add("guest");
    }

    @GET
    public String login(@Context final HttpServletRequest request, @Context final HttpServletResponse response) throws IOException {
        if (this.identity.isLoggedIn()) {
            return null;
        }
        
        this.credential.setCredential(new Credential<FacebookCredentialCredential>() {

            @Override
            public FacebookCredentialCredential getValue() {
                FacebookCredentialCredential oAuthCredential = new FacebookCredentialCredential();
                
                oAuthCredential.setRequest(request);
                oAuthCredential.setResponse(response);
                
                return oAuthCredential;
            }
        });
        
        this.identity.login();
        
        boolean result = this.identity.isLoggedIn();
        
        if(result){
            //Check if the user exists in DB
            User user = identity.getUser();
            User storedUser = identityManager.getUser(user.getId());
            
            if(storedUser == null){
                User newUser = identityManager.createUser(user.getId());
                newUser.setFirstName(user.getId());

                Role guest = this.identityManager.createRole("guest");
                Group guests = identityManager.createGroup("Guests");

                identityManager.grantRole(guest, newUser, guests);
            }
            
            return "<script>window.opener.sendMainPage();</script>";
        }
        
        return null;
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