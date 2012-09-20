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

package org.aerogear.todo.server.security.authc.oauth;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aerogear.todo.server.security.authc.fb.FacebookProcessor;
import org.picketbox.core.Credential;
import org.picketbox.core.authentication.AuthenticationInfo;
import org.picketbox.core.authentication.AuthenticationManager;
import org.picketbox.core.authentication.AuthenticationResult;
import org.picketbox.core.authentication.impl.AbstractAuthenticationMechanism;
import org.picketbox.core.exceptions.AuthenticationException;

/**
 * @author Anil Saldhana
 * @author Pedro Silva
 */
public class FacebookAuthenticationMechanism extends AbstractAuthenticationMechanism {

    protected String returnURL;
    protected String clientID;
    protected String clientSecret;
    protected String scope = "email";

    protected List<String> roles = new ArrayList<String>();
    protected FacebookProcessor processor;

    public FacebookAuthenticationMechanism() {
        clientID = System.getProperty("FB_CLIENT_ID");
        clientSecret = System.getProperty("FB_CLIENT_SECRET");
        returnURL = System.getProperty("FB_RETURN_URL");
        roles.add("guest");
    }
    
    private enum STATES {
        AUTH, AUTHZ, FINISH
    };

    @Override
    public List<AuthenticationInfo> getAuthenticationInfo() {
        ArrayList<AuthenticationInfo> info = new ArrayList<AuthenticationInfo>();

        info.add(new AuthenticationInfo("oAuth Authentication", "Provides oAuth authentication.", oAuthCredential.class));

        return info;
    }

    @Override
    protected Principal doAuthenticate(AuthenticationManager authenticationManager, Credential credential,
            AuthenticationResult result) throws AuthenticationException {
        oAuthCredential oAuthCredential = (org.aerogear.todo.server.security.authc.oauth.oAuthCredential) credential;
        HttpServletRequest request = oAuthCredential.getRequest();
        HttpServletResponse response = oAuthCredential.getResponse();
        
        HttpSession session = request.getSession();
        String state = (String) session.getAttribute("STATE");
        if (processor == null)
            processor = new FacebookProcessor(clientID, clientSecret, scope, returnURL, roles);

        if (state == null || state.isEmpty()) {
            try {
                boolean initialInteraction = processor.initialInteraction(request, response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        // We have sent an auth request
        if (state.equals(STATES.AUTH.name())) {
            processor.handleAuthStage(request, response);
            return null;
        }

        // Principal facebookPrincipal = null;
        if (state.equals(STATES.AUTHZ.name())) {
            Principal principal = processor.getPrincipal(request, response);

            if (principal != null) {

                session.setAttribute("PRINCIPAL", principal);
                session.removeAttribute("STATE");
                return principal;
            }

            session.setAttribute("PRINCIPAL", principal);

            session.removeAttribute("STATE");
        }
        return null;
    }

}
