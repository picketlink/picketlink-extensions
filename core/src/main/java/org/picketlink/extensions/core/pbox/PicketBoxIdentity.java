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

package org.picketlink.extensions.core.pbox;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.UserContext;
import org.picketbox.core.UserCredential;
import org.picketbox.core.session.DefaultSessionId;
import org.picketlink.Identity;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.authentication.event.LoginFailedEvent;
import org.picketlink.credential.internal.DefaultLoginCredentials;
import org.picketlink.idm.model.User;
import org.picketlink.internal.DefaultIdentity;

/**
 * <p>
 * PicketBox implementation for the {@link Identity} component. This implementation is the main integration point for
 * DeltaSpike.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@SessionScoped
@Named("identity")
public class PicketBoxIdentity extends DefaultIdentity {

    private static final long serialVersionUID = -290838764498141080L;

    @Inject
    private BeanManager beanManager;

    @Inject
    private DefaultLoginCredentials credential;

    @Inject
    private PicketBoxManager picketBoxManager;

    private UserContext subject;

    /*
     * (non-Javadoc)
     *
     * @see org.apache.deltaspike.security.impl.authentication.DefaultIdentity#authenticate()
     */
    @Override
    public boolean authenticate() throws AuthenticationException {
        return authenticate(null);
    }

    /**
     * <p>
     * Performs the authentication using the specified
     * <code>sessionId<code>/session identifier. If a valid identifier is specified, PicketBox will try to restore the user session and create
     * the {@link Identity} state. Otherwise the credentials will be used to perform the authentication.
     * </p>
     *
     * @param sessionId
     * @return
     * @throws AuthenticationException
     */
    private boolean authenticate(String sessionId) throws AuthenticationException {
        UserContext subject = null;

        try {
            UserContext authenticationUserContext = null;

            if (sessionId != null) {
                authenticationUserContext = new UserContext(new DefaultSessionId(sessionId));
            } else {
                authenticationUserContext = new UserContext();
            }

            if (sessionId == null) {
                authenticationUserContext.setCredential((UserCredential) this.credential.getCredential());
            }

            subject = this.picketBoxManager.authenticate(authenticationUserContext);
        } catch (Exception e) {
            this.beanManager.fireEvent(new LoginFailedEvent(e));
            throw new AuthenticationException(e.getMessage());
        }

        if (subject != null && subject.isAuthenticated()) {
            this.subject = subject;
            return true;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.deltaspike.security.impl.authentication.DefaultIdentity#logout()
     */
    @Override
    public void logout() {
        if (isLoggedIn()) {
            super.logout();
            this.picketBoxManager.logout(this.subject);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.deltaspike.security.impl.authentication.DefaultIdentity#isLoggedIn()
     */
    @Override
    public boolean isLoggedIn() {
        return this.subject != null && this.subject.isAuthenticated();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.deltaspike.security.spi.authentication.Authenticator#getUser()
     */
    @Override
    public User getUser() {
        return this.subject.getUser();
    }

    /**
     * <p>
     * Restores the user's security context/state using the specified <code>sessionId</code>
     * </p>
     *
     * @param sessionId
     * @return
     * @throws AuthenticationException
     */
    public boolean restoreSession(String sessionId) throws AuthenticationException {
        return authenticate(sessionId);
    }

    public boolean hasRole(String restrictedRole) {
        return isLoggedIn() && this.subject.hasRole(restrictedRole);
    }

    public UserContext getUserContext() {
        return this.subject;
    }
}
