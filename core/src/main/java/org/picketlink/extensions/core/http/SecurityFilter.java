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

package org.picketlink.extensions.core.http;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketbox.core.PicketBoxManager;
import org.picketbox.http.PicketBoxHTTPManager;
import org.picketbox.http.filters.DelegatingSecurityFilter;
import org.picketlink.Identity;

/**
 * @author Pedro Silva
 *
 */
public class SecurityFilter extends DelegatingSecurityFilter {

    @Inject
    private PicketBoxManager securityManager;
    
    @Inject
    private ServletContextualObjectsHolder holder;
    
    @Inject
    private Identity identity;
    
    @Override
    protected PicketBoxHTTPManager doInitSecurityManager(FilterConfig fc) {
        return (PicketBoxHTTPManager) this.securityManager;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        try {
            this.holder.setRequest((HttpServletRequest) request);
            this.holder.setResponse((HttpServletResponse) response);
            
            super.doFilter(request, response, chain);            
        } catch (ServletException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            this.holder.clearRequest();
            this.holder.clearResponse();
        }
    }
}
