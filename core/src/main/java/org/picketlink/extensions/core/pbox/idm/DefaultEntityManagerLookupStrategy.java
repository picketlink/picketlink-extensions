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

package org.picketlink.extensions.core.pbox.idm;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.picketbox.core.identity.jpa.EntityManagerLookupStrategy;

/**
 * <p>
 * Custom {@link JPATemplate} to be used during the JPA Identity Store configuration. This bean automatically inject the
 * {@link EntityManager} instance to be used during the IDM operations.
 * </p>
 *
 * @author pedroigor
 *
 */
@ApplicationScoped
public class DefaultEntityManagerLookupStrategy extends EntityManagerLookupStrategy {

    @Inject
    private Instance<EntityManager> entityManager;

    @Override
    protected EntityManager lookupEntityManager() {
        EntityManager entityManager = null;

        try {
            entityManager = this.entityManager.get();
        } catch (Exception e) {

        }

        return entityManager;
    }

}
