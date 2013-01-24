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
package org.picketlink.extensions.core.rest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.picketbox.jaxrs.model.AccountRegistrationRequest;
import org.picketbox.jaxrs.model.AccountRegistrationResponse;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.internal.Password;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;

/**
 * Endpoint for User Account Registration
 * 
 * @author anil saldhana
 * @since Jan 16, 2013
 */
@Stateless
@Path("/accregister")
public class AccountRegistrationEndpoint {

    @Inject
    private IdentityManager identityManager;

    // @PersistenceContext(type = PersistenceContextType.EXTENDED)
    // private EntityManager entityManager;

    /**
     * Register an user account
     * 
     * @param request
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AccountRegistrationResponse register(AccountRegistrationRequest request) {
        AccountRegistrationResponse response = new AccountRegistrationResponse();

        String userName = request.getUserName();

        // EntityManagerPropagationContext.set(entityManager);
        if (this.identityManager.getUser(userName) == null) {
            // UserName is not already registered
            User user = new SimpleUser(userName);
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            user.setAttribute(new Attribute<String>("address", request.getAddress()));
            user.setAttribute(new Attribute<String>("city", request.getCity()));
            user.setAttribute(new Attribute<String>("state", request.getState()));
            user.setAttribute(new Attribute<String>("postalCode", request.getPostalCode()));
            user.setAttribute(new Attribute<String>("country", request.getCountry()));

            this.identityManager.add(user);
            this.identityManager.updateCredential(user, new Password(request.getPassword()));
            
            response.setStatus("Registered");
            response.setRegistered(true);
        } else {
            response.setStatus("UserName already taken. Choose another name!");
        }
        
        // EntityManagerPropagationContext.clear();
        return response;
    }
}