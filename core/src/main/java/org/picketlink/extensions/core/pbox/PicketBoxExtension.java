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

package org.picketlink.extensions.core.pbox;

import java.lang.reflect.Type;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducer;

import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.config.ConfigurationBuilder;
import org.picketlink.Identity;
import org.picketlink.producer.IdentityManagerProducer;

/**
 * <p>
 * PicketBox CDI Extension for configuring the PicketBox security environment.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class PicketBoxExtension implements Extension {

    private Type configurationType;

    /**
     * <p>
     * Adds the {@link PicketBoxManagerBeanDefinition}.
     * </p>
     * 
     * @param event
     * @param beanManager
     */
    public void addPicketBoxManagerBeanDefinition(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        event.addBean(new PicketBoxManagerBeanDefinition(beanManager, this.configurationType));
        event.addBean(new PicketBoxIdentityBeanDefinition(beanManager, this.configurationType));
    }

    public void installPicketBoxManager(@Observes ProcessProducer<?, ConfigurationBuilder> event) {
        this.configurationType = event.getAnnotatedMember().getBaseType();
    }

    /**
     * <p>
     * Vetos all {@link Identity} beans. They would be created by the {@link PicketBoxIdentityBeanDefinition}.
     * <p>
     * 
     * @param event
     * @param beanManager
     */
    public void vetoIdentityBeans(@Observes ProcessAnnotatedType<Identity> event) {
        event.veto();
    }

    /**
     * <p>
     * Veto PicketLink {@link IdentityManagerProducer} bean.
     * <p>
     * 
     * @param event
     * @param beanManager
     */
    public void vetoIdentityManagerProducer(@Observes ProcessAnnotatedType<IdentityManagerProducer> event) {
        event.veto();
    }
    
    /**
     * <p>
     * Veto all{@link PicketBoxManager} beans. The would be created by the {@link PicketBoxManagerBeanDefinition}.
     * <p>
     * 
     * @param event
     * @param beanManager
     */
    public void vetoPicketBoxManagerBeans(@Observes ProcessAnnotatedType<PicketBoxManager> event) {
        event.veto();
    }

}