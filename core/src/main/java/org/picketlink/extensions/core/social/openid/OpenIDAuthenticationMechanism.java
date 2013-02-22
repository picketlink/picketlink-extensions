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

package org.picketlink.extensions.core.social.openid;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
 
import org.picketbox.core.authentication.AuthenticationInfo;
import org.picketbox.core.authentication.AuthenticationResult;
import org.picketbox.core.authentication.credential.UserCredential;
import org.picketbox.core.authentication.impl.AbstractAuthenticationMechanism;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.social.standalone.oauth.OpenIDProcessor;
import org.picketlink.social.standalone.oauth.OpenIdPrincipal;

/**
 * An authentication mechanism for Google/OpenID SignIn
 * 
 * @author Anil Saldhana
 * @author Pedro Silva
 */
public class OpenIDAuthenticationMechanism extends AbstractAuthenticationMechanism {
    protected String returnURL;
    protected String requiredAttributes;
    protected String optionalAttributes;
    protected String scope = "email";

    protected OpenIDProcessor processor;
    
    @Inject
    private IdentityManager identityManager;

    public OpenIDAuthenticationMechanism() {
        requiredAttributes = System.getProperty("OPENID_REQUIRED","name,email,ax_firstName,ax_lastName,ax_fullName,ax_email");
        optionalAttributes = System.getProperty("OPENID_OPTIONAL");
        returnURL = System.getProperty("OPENID_RETURN_URL");
    }
    
    private enum STATES {
        AUTH, AUTHZ, FINISH
    }; 

    @Override
    public List<AuthenticationInfo> getAuthenticationInfo() {
        ArrayList<AuthenticationInfo> info = new ArrayList<AuthenticationInfo>();

        info.add(new AuthenticationInfo("oAuth Authentication", "Provides oAuth authentication.", OpenIDCredential.class));

        return info;
    }

    @Override
    protected Principal doAuthenticate(UserCredential credential, AuthenticationResult result) throws AuthenticationException {
        OpenIDCredential oAuthCredential = (OpenIDCredential) credential;
        if (processor == null)
            processor = new OpenIDProcessor(returnURL, requiredAttributes, optionalAttributes);

        List<String> roles = new ArrayList<String>();
        roles.add("Guest");
        
        if (!processor.isInitialized()) {
            try {
                processor.initialize(roles);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        HttpServletRequest request = oAuthCredential.getRequest();
        HttpServletResponse response = oAuthCredential.getResponse();
        HttpSession session = request.getSession();
        
        HttpSession httpSession = request.getSession();
        String state = (String) httpSession.getAttribute("STATE");

        Principal principal = (Principal) session.getAttribute("PRINCIPAL"); 

        if (STATES.FINISH.name().equals(state)){
            return principal;   
        }

        if (state == null || state.isEmpty()) {
            try {
                processor.prepareAndSendAuthRequest(request, response);
                state = (String) httpSession.getAttribute("STATE");
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        // We have sent an auth request
        if (state.equals(STATES.AUTH.name())) {
            session = request.getSession(true);
           
            try {
                principal = processor.processIncomingAuthResult(request, response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (principal == null)
                throw new RuntimeException("Principal was null. Maybe login modules need to be configured properly.");
            session.setAttribute("PRINCIPAL", principal);

            checkUserInStore((OpenIdPrincipal) principal);
            session.setAttribute("STATE", STATES.FINISH.name());
            return principal;
        }
        return principal;
    } 
   
    
    private void checkUserInStore(OpenIdPrincipal openIDPrincipal){
        if(identityManager != null){
            User newUser = null;
            
            User storedUser = identityManager.getUser(openIDPrincipal.getFullName());
            if(storedUser == null){ 
                newUser = new SimpleUser(openIDPrincipal.getFullName());
                
                newUser.setFirstName(openIDPrincipal.getFirstName());
                newUser.setLastName(openIDPrincipal.getLastName());
                newUser.setEmail(openIDPrincipal.getEmail()); 

                identityManager.add(newUser);
                
                Role guest = new SimpleRole("guest");
                
                this.identityManager.add(guest);

                Group guests = new SimpleGroup("Guests");
                
                identityManager.add(guests);

                identityManager.grantRole(storedUser, guest);
            }
        }
    }
}