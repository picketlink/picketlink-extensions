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

package org.picketlink.extensions.core.auth;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.picketbox.core.authentication.credential.UsernamePasswordCredential;
import org.picketbox.jaxrs.model.AuthenticationRequest;
import org.picketbox.jaxrs.model.AuthenticationResponse;
import org.picketlink.credential.internal.DefaultLoginCredentials;
import org.picketlink.extensions.core.pbox.PicketBoxIdentity;

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
    private PicketBoxIdentity identity;
    
    @Inject
    private DefaultLoginCredentials credential;
    
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
        
        credential.setCredential(new UsernamePasswordCredential(authcRequest.getUserId(), authcRequest.getPassword()));
        
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
