/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            throw new RuntimeException(e);
        }

        return entityManager;
    }

}
