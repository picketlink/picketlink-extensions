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

import java.util.Collection;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
 
import org.picketbox.core.UserContext;
import org.picketlink.extensions.core.pbox.PicketBoxIdentity;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * <p>JAX-RS Endpoint to authenticate users.</p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Stateless
@Path("/userinfo")
public class UserInfoEndpoint {

    @Inject
    private PicketBoxIdentity identity;
    
    @GET
    @Produces (MediaType.APPLICATION_JSON)
    public UserInfo getInfo() {
        UserInfo userInfo = new UserInfo();
        
        User user = this.identity.getUser();
        userInfo.setUserId(user.getId());
        userInfo.setFullName(user.getFirstName() + " " + user.getLastName());
        
        UserContext userContext = this.identity.getUserContext();
        
        Collection<Role> roles = userContext.getRoles();
        String[] rolesArray = new String[roles.size()];
        
        int i = 0;
        
        for (Role role : roles) {
            rolesArray[i] = role.getName();
            i++;
        }
        
        userInfo.setRoles(rolesArray);
        
        return userInfo;
    }
    
}