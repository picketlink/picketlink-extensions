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

package org.picketlink.extensions.core.social.twitter;

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
import org.picketlink.social.standalone.fb.FacebookProcessor;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * An authentication mechanism for Twitter SignIn
 * 
 * @author Anil Saldhana
 * @author Pedro Silva
 */
public class TwitterAuthenticationMechanism extends AbstractAuthenticationMechanism {

    private static final String TWIT_REQUEST_TOKEN_SESSION_ATTRIBUTE = "TWIT_REQUEST_TOKEN_SESSION_ATTRIBUTE";
    protected String returnURL;
    protected String clientID;
    protected String clientSecret;
    protected String scope = "email";

    protected FacebookProcessor processor;
    
    @Inject
    private IdentityManager identityManager;

    public TwitterAuthenticationMechanism() {
        clientID = System.getProperty("TWIT_CLIENT_ID");
        clientSecret = System.getProperty("TWIT_CLIENT_SECRET");
        returnURL = System.getProperty("TWIT_RETURN_URL");
    }

    @Override
    public List<AuthenticationInfo> getAuthenticationInfo() {
        ArrayList<AuthenticationInfo> info = new ArrayList<AuthenticationInfo>();

        info.add(new AuthenticationInfo("oAuth Authentication", "Provides oAuth authentication.", TwitterCredential.class));

        return info;
    }

    @Override
    protected Principal doAuthenticate(UserCredential credential, AuthenticationResult result) throws AuthenticationException {
        TwitterCredential oAuthCredential = (TwitterCredential) credential;
        
        HttpServletRequest request = oAuthCredential.getRequest();
        HttpServletResponse response = oAuthCredential.getResponse();
        HttpSession session = request.getSession();

        Principal principal = null;
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(clientID, clientSecret);
        
        //See if we are a callback
        RequestToken requestToken = (RequestToken) session.getAttribute(TWIT_REQUEST_TOKEN_SESSION_ATTRIBUTE);
        if(requestToken != null){
            String verifier = request.getParameter("oauth_verifier");
            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                session.setAttribute("accessToken", accessToken);
                session.removeAttribute("requestToken");
            } catch (TwitterException e) {
                throw new AuthenticationException(e);
            }
            
            try {
                principal = new TwitterPrincipal(twitter.verifyCredentials());
                checkUserInStore((TwitterPrincipal) principal);
            } catch (TwitterException e) {
                throw new AuthenticationException(e);
            }
            return principal;
        }
        try {
            requestToken = twitter.getOAuthRequestToken(returnURL);
            session.setAttribute(TWIT_REQUEST_TOKEN_SESSION_ATTRIBUTE, requestToken);
            response.sendRedirect(requestToken.getAuthenticationURL());

        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
        return principal;
    }
    
    private void checkUserInStore(TwitterPrincipal twitterPrincipal){
        if(identityManager != null){
            User newUser = null;
            
            User storedUser = identityManager.getUser(twitterPrincipal.getName());
            
            if(storedUser == null){ 
                newUser = new SimpleUser(twitterPrincipal.getName());
                
                newUser.setFirstName(twitterPrincipal.getName());
                
                identityManager.add(newUser);
                
                Role guest = new SimpleRole("guest");
                
                this.identityManager.add(guest);
                
                Group guests = new SimpleGroup("Guests");
                
                identityManager.add(guests);

                identityManager.grantRole(newUser, guest);
                identityManager.addToGroup(newUser, guests);
            }
        }
    }
}
