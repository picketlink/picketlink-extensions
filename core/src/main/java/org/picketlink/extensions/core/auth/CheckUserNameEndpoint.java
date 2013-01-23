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
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.picketbox.jaxrs.model.AccountRegistrationResponse;
import org.picketlink.idm.IdentityManager;

/**
 * @author Pedro Silva
 *
 */
@Stateless
@Path("/alreadyExists")
public class CheckUserNameEndpoint {

    @Inject
    private IdentityManager identityManager;

    /**
     * Check if an UserName is already taken
     * 
     * @param userName
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AccountRegistrationResponse alreadyExists(@QueryParam("userName") String userName) {

        AccountRegistrationResponse response = new AccountRegistrationResponse();

        // EntityManagerPropagationContext.set(entityManager);
        // identityManager = picketboxManager.getIdentityManager();

        if (identityManager.getUser(userName) != null) {
            response.setRegistered(true);
        }
        // EntityManagerPropagationContext.clear();
        return response;
    }
    
}
