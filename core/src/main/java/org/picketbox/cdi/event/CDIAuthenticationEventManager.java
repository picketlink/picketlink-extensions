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

package org.picketbox.cdi.event;

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;

import org.picketbox.core.event.PicketBoxEventManager;

/**
 * <p>Implementation of {@link PicketBoxEventManager} that allows to raise events using the CDI {@link BeanManager}.</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Typed
public class CDIAuthenticationEventManager implements PicketBoxEventManager {

    private BeanManager beanManager;

    public CDIAuthenticationEventManager(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    /* (non-Javadoc)
     * @see org.picketbox.core.authentication.AuthenticationEventManager#raiseEvent(org.picketbox.core.authentication.event.AuthenticationEvent)
     */
    @Override
    public void raiseEvent(Object event) {
        this.beanManager.fireEvent(event);
    }

    /* (non-Javadoc)
     * @see org.picketbox.core.event.PicketBoxEventManager#addHandler(org.picketbox.core.event.PicketBoxEventHandler)
     */
    @Override
    public void addHandler(Object handler) {
    }

}
