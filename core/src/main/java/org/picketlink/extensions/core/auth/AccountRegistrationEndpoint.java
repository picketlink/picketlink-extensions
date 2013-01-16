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

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.identity.jpa.EntityManagerPropagationContext;
import org.picketbox.jaxrs.model.AccountRegistrationRequest;
import org.picketbox.jaxrs.model.AccountRegistrationResponse;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;

/**
 * Endpoint for User Account Registration
 * @author anil saldhana
 * @since Jan 16, 2013
 */
@Stateless
@Path("/accregister")
public class AccountRegistrationEndpoint {   

    @Inject 
    private PicketBoxManager picketboxManager;
    
    private IdentityManager identityManager;

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;

    /**
     * Check if an UserName is already taken
     * @param userName
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AccountRegistrationResponse alreadyExists(@QueryParam("id") String userName){

        AccountRegistrationResponse response = new AccountRegistrationResponse();

        EntityManagerPropagationContext.set(entityManager);
        identityManager = picketboxManager.getIdentityManager();
        
        IdentityQuery<User> query = identityManager.createQuery(User.class);
        query.setParameter(User.ID , userName);
        
        List<User> users = query.getResultList();
        int size = users.size();
        if(size >0){
            response.setRegistered(true);
        }
        return response;
    }
    
    /**
     * Register an user account
     * @param request
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AccountRegistrationResponse register(AccountRegistrationRequest request){
        AccountRegistrationResponse response = new AccountRegistrationResponse();
        
        String userName = request.getUserName();
        
        EntityManagerPropagationContext.set(entityManager);
        identityManager = picketboxManager.getIdentityManager();
        
        IdentityQuery<User> query = identityManager.createQuery(User.class);
        query.setParameter(User.ID , userName);
        
        List<User> users = query.getResultList();
        int size = users.size();
        
        if(size == 0){
            //UserName is not already registered
            User user = new SimpleUser(userName);
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            user.setAttribute( new Attribute<String>("address", request.getAddress()) );
            user.setAttribute( new Attribute<String>("city", request.getCity()) );
            user.setAttribute( new Attribute<String>("state", request.getState()) );
            user.setAttribute( new Attribute<String>("postalCode", request.getPostalCode()) );
            user.setAttribute( new Attribute<String>("country", request.getCountry()) );
            
            identityManager.add(user);
            
            identityManager.updateCredential(user, new Password(request.getPassword()));
            response.setStatus("Registered");
            response.setRegistered(true);
            
        } else {
            response.setStatus("UserName already taken. Choose another name!");
        }
        return response;
    }
}