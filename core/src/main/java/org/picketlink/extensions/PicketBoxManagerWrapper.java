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

package org.picketlink.extensions;

import org.picketbox.core.DefaultPicketBoxManager;
import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.UserContext;
import org.picketbox.core.audit.AuditProvider;
import org.picketbox.core.authorization.Resource;
import org.picketbox.core.config.PicketBoxConfiguration;
import org.picketbox.core.event.PicketBoxEventManager;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.core.session.SessionManager;
import org.picketlink.idm.IdentityManager;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class PicketBoxManagerWrapper implements PicketBoxManager {

    private PicketBoxManager delegate;

    public PicketBoxManagerWrapper(PicketBoxConfiguration configuration) {
        this.delegate = new DefaultPicketBoxManager(configuration);
    }

    /**
     * @return
     * @see org.picketbox.core.PicketBoxLifecycle#started()
     */
    @Override
    public boolean started() {
        return this.delegate.started();
    }

    /**
     *
     * @see org.picketbox.core.PicketBoxLifecycle#start()
     */
    @Override
    public void start() {
        this.delegate.start();
    }

    /**
     * @return
     * @see org.picketbox.core.PicketBoxLifecycle#stopped()
     */
    @Override
    public boolean stopped() {
        return this.delegate.stopped();
    }

    /**
     *
     * @see org.picketbox.core.PicketBoxLifecycle#stop()
     */
    @Override
    public void stop() {
        this.delegate.stop();
    }

    /**
     * @param subject
     * @return
     * @throws AuthenticationException
     * @see org.picketbox.core.PicketBoxManager#authenticate(org.picketbox.core.UserContext)
     */
    @Override
    public UserContext authenticate(UserContext subject) throws AuthenticationException {
        return this.delegate.authenticate(subject);
    }

    /**
     * @param subject
     * @param resource
     * @return
     * @see org.picketbox.core.PicketBoxManager#authorize(org.picketbox.core.UserContext, org.picketbox.core.authorization.Resource)
     */
    @Override
    public boolean authorize(UserContext subject, Resource resource) {
        return this.delegate.authorize(subject, resource);
    }

    /**
     * @param authenticatedUser
     * @throws IllegalStateException
     * @see org.picketbox.core.PicketBoxManager#logout(org.picketbox.core.UserContext)
     */
    @Override
    public void logout(UserContext authenticatedUser) throws IllegalStateException {
        this.delegate.logout(authenticatedUser);
    }

    @Override
    public PicketBoxEventManager getEventManager() {
        return this.delegate.getEventManager();
    }

    @Override
    public IdentityManager getIdentityManager() {
        return this.delegate.getIdentityManager();
    }

    @Override
    public PicketBoxConfiguration getConfiguration() {
        return this.delegate.getConfiguration();
    }

    @Override
    public SessionManager getSessionManager() {
        return this.delegate.getSessionManager();
    }

    @Override
    public AuditProvider getAuditProvider() {
        return this.delegate.getAuditProvider();
    }
}