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

package org.picketlink.extensions.core.pbox.event;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.audit.AuditProvider;
import org.picketbox.core.audit.event.AuditEventHandler;
import org.picketbox.core.authentication.event.UserAuthenticatedEvent;
import org.picketbox.core.authentication.event.UserAuthenticationFailedEvent;
import org.picketbox.core.authentication.event.UserNotAuthenticatedEvent;

/**
 * @author Pedro Silva
 * 
 */
@ApplicationScoped
public class CDIAuditEventHandler extends AuditEventHandler {

    @Inject
    private PicketBoxManager picketBoxManager;
    
    public CDIAuditEventHandler() {
    }

    public CDIAuditEventHandler(AuditProvider auditProvider) {
        super(auditProvider);
    }

    @Override
    public void onAuthenticationFailed(@Observes UserAuthenticationFailedEvent event) {
        if (getAuditProvider() != null) { 
            super.onAuthenticationFailed(event);
        }
    }

    @Override
    public void onSuccessfulAuthentication(@Observes UserAuthenticatedEvent event) {
        if (getAuditProvider() != null) {
            super.onSuccessfulAuthentication(event);
        }
    }

    @Override
    public void onUnSuccessfulAuthentication(@Observes UserNotAuthenticatedEvent event) {
        if (getAuditProvider() != null) {
            super.onUnSuccessfulAuthentication(event);
        }
    }
    
    @Override
    public AuditProvider getAuditProvider() {
        return this.picketBoxManager.getAuditProvider();
    }
}
