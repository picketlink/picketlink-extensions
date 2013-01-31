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
package org.picketlink.extensions.core.rest.interceptors;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.picketbox.jaxrs.model.AuthenticationResponse;

/**
 * A {@link PostProcessInterceptor} that checks if we have an AuthenticationResponse
 * of success and then inserts cookie into the headers
 * @author anil saldhana
 * @since Jan 31, 2013
 */
@ApplicationScoped
@ServerInterceptor
@Provider
public class PostSignInCookieInterceptor implements PostProcessInterceptor {
    private static final String AUTH_TOKEN_HEADER_NAME = "Auth-Token";
    
    @Context ServletContext servletContext;
    
    /**
     * Put the Auth-Token in the cookie
     */
    @Override
    public void postProcess(ServerResponse response) {
        if(response != null){
            Object entity = response.getEntity();
            if(entity instanceof AuthenticationResponse){
                AuthenticationResponse authResponse = (AuthenticationResponse) entity;
                if(authResponse.isLoggedIn()){
                    String token = authResponse.getToken();
                    //Now set the cookie
                    MultivaluedMap<String, Object> headers = response.getMetadata();
                    List<Object> cookies = headers.get(HttpHeaderNames.SET_COOKIE);
                    if(cookies == null){
                        cookies = new ArrayList<Object>();
                    }
                    
                    String contextPath = servletContext.getContextPath();
                    SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
                    String path = sessionCookieConfig.getPath();
                    String domain = sessionCookieConfig.getDomain();
                    int age = sessionCookieConfig.getMaxAge();
                    boolean secureCookie = sessionCookieConfig.isSecure();
                    
                    //TODO: HTTPOnly
                    
                    NewCookie cookie = new NewCookie(AUTH_TOKEN_HEADER_NAME, token,contextPath,path,domain,age,secureCookie);
                    cookies.add(cookie);
                    headers.put(HttpHeaderNames.SET_COOKIE, cookies);
                }
            }
        }
    }
}