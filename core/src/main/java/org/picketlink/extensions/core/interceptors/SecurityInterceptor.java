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

package org.picketlink.extensions.core.interceptors;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.picketbox.jaxrs.model.AuthenticationResponse;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.extensions.core.auth.LogoutEndpoint;
import org.picketlink.extensions.core.auth.SignInEndpoint;
import org.picketlink.extensions.core.pbox.PicketBoxIdentity;

/**
 * <p>
 * Implementation of {@link PreProcessInterceptor} that checks the existence of the authentication token before invoking the
 * destination endpoint.
 * </p>
 * <p>If the token is valid, the {@link PicketBoxIdentity} will restored with the all user information.</p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@ApplicationScoped
@ServerInterceptor
public class SecurityInterceptor implements PreProcessInterceptor {

    private static final String AUTH_TOKEN_HEADER_NAME = "Auth-Token";
    @Inject
    private PicketBoxIdentity identity;

    /* (non-Javadoc)
     * @see org.jboss.resteasy.spi.interception.PreProcessInterceptor#preProcess(org.jboss.resteasy.spi.HttpRequest, org.jboss.resteasy.core.ResourceMethod)
     */
    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
        ServerResponse response = null;

        if (requiresAuthentication(method) && !this.identity.isLoggedIn()) {
            boolean isLoggedIn = false;
            String token = getToken(request);
            
            if (token != null) {
                try {
                    isLoggedIn = identity.restoreSession(token);
                } catch (AuthenticationException e) {

                }
            }

            if (!isLoggedIn) {
                AuthenticationResponse authcResponse = new AuthenticationResponse();
                
                authcResponse.setLoggedIn(false);
                
                response = new ServerResponse();
                response.setEntity(authcResponse);
                response.setStatus(HttpStatus.SC_FORBIDDEN);
            }
        }

        return response;
    }

    /**
     * <p>Retrieve the token from the request, if present.</p>
     * 
     * @param request
     * @return
     */
    private String getToken(HttpRequest request) {
        List<String> tokenHeader = request.getHttpHeaders().getRequestHeader(AUTH_TOKEN_HEADER_NAME);
        String token = null;
        
        if (tokenHeader != null && !tokenHeader.isEmpty()) {
            token = tokenHeader.get(0);
        }
        
        return token;
    }

    /**
     * <p>Checks if the {@link ResourceMethod} requires authentication.</p>
     * 
     * @param method
     * @return
     */
    private boolean requiresAuthentication(ResourceMethod method) {
        Class<?> declaringClass = method.getMethod().getDeclaringClass();
        
        Class<?> sign = SignInEndpoint.class;
        Class<?> logout = LogoutEndpoint.class;
        
        return !(declaringClass.equals(sign) || declaringClass.equals(logout));
    }
}