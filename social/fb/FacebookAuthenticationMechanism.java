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

package org.aerogear.todo.server.security.authc.social.fb;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.picketbox.core.Credential;
import org.picketbox.core.authentication.AuthenticationInfo;
import org.picketbox.core.authentication.AuthenticationResult;
import org.picketbox.core.authentication.impl.AbstractAuthenticationMechanism;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.social.standalone.fb.FacebookPrincipal;
import org.picketlink.social.standalone.fb.FacebookProcessor;

/**
 * An authentication mechanism for Facebook SignIn
 * 
 * @author Anil Saldhana
 * @author Pedro Silva
 */
public class FacebookAuthenticationMechanism extends AbstractAuthenticationMechanism {

    private static final String FB_AUTH_STATE_SESSION_ATTRIBUTE = "FB_AUTH_STATE_SESSION_ATTRIBUTE";
    protected String returnURL;
    protected String clientID;
    protected String clientSecret;
    protected String scope = "email";

    protected FacebookProcessor processor;
    
    @Inject
    private IdentityManager identityManager;

    public FacebookAuthenticationMechanism() {
        clientID = System.getProperty("FB_CLIENT_ID");
        clientSecret = System.getProperty("FB_CLIENT_SECRET");
        returnURL = System.getProperty("FB_RETURN_URL");
    }
    
    private enum STATES {
        AUTH, AUTHZ, FINISH
    }; 

    @Override
    public List<AuthenticationInfo> getAuthenticationInfo() {
        ArrayList<AuthenticationInfo> info = new ArrayList<AuthenticationInfo>();

        info.add(new AuthenticationInfo("oAuth Authentication", "Provides oAuth authentication.", FacebookCredential.class));

        return info;
    }

    /* (non-Javadoc)
     * @see org.picketbox.core.authentication.impl.AbstractAuthenticationMechanism#doAuthenticate(org.picketbox.core.authentication.AuthenticationManager, org.picketbox.core.Credential, org.picketbox.core.authentication.AuthenticationResult)
     */
    @Override
    protected Principal doAuthenticate(Credential credential, AuthenticationResult result) throws AuthenticationException {
        FacebookCredential oAuthCredential = (FacebookCredential) credential;
        
        HttpServletRequest request = oAuthCredential.getRequest();
        HttpServletResponse response = oAuthCredential.getResponse();
        HttpSession session = request.getSession();

        Principal principal = null;
        
        if (isFirstInteraction(session)) {
            try {
                getFacebookProcessor().initialInteraction(request, response);
            } catch (IOException e) {
                throw new AuthenticationException("Error while initiating Facebook authentication interaction.", e);
            }
        } else if (isAuthenticationInteraction(session)) {
            if (!response.isCommitted())
                getFacebookProcessor().handleAuthStage(request, response);
        } else if (isAuthorizationInteraction(session)) {
            session.removeAttribute(FB_AUTH_STATE_SESSION_ATTRIBUTE);
            principal = getFacebookProcessor().getPrincipal(request, response);
            checkUserInStore((FacebookPrincipal) principal);
        }
        
        return principal;
    }

    private boolean isAuthorizationInteraction(HttpSession session) {
        return getCurrentAuthenticationState(session).equals(STATES.AUTHZ.name());
    }

    private boolean isAuthenticationInteraction(HttpSession session) {
        return getCurrentAuthenticationState(session).equals(STATES.AUTH.name());
    }

    private boolean isFirstInteraction(HttpSession session) {
        return getCurrentAuthenticationState(session) == null || getCurrentAuthenticationState(session).isEmpty();
    }

    private String getCurrentAuthenticationState(HttpSession session) {
        return (String) session.getAttribute(FB_AUTH_STATE_SESSION_ATTRIBUTE);
    }

    @SuppressWarnings("unchecked")
    private FacebookProcessor getFacebookProcessor() {
        if (this.processor == null) {
            this.processor = new FacebookProcessor(clientID, clientSecret, scope, returnURL, Collections.EMPTY_LIST);
        }
        return this.processor;
    }
    
    private void checkUserInStore(FacebookPrincipal principal){
        if(identityManager != null){
            User storedUser = identityManager.getUser(principal.getName());
            if(storedUser == null){
                User newUser = identityManager.createUser(principal.getName());
                newUser.setFirstName(principal.getFirstName());
                newUser.setLastName(principal.getLastName());
                newUser.setEmail(principal.getEmail());

                Role guest = this.identityManager.createRole("guest");
                Group guests = identityManager.createGroup("Guests");

                identityManager.grantRole(guest, newUser, guests);
            }
        }
    }
}
