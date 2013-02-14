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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.AnnotationLiteral;

import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.config.ConfigurationBuilder;
import org.picketbox.core.config.PicketBoxConfiguration;
import org.picketbox.http.PicketBoxHTTPManager;
import org.picketbox.http.config.HTTPConfigurationBuilder;
import org.picketbox.http.config.PicketBoxHTTPConfiguration;
import org.picketlink.extensions.core.pbox.event.CDIAuthenticationEventManager;

/**
 * <p>
 * {@link Bean} implementation to define/customize the behaviour for {@link PicketBoxManager} instances.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class PicketBoxManagerBeanDefinition implements Bean<PicketBoxManager> {

    private BeanManager beanManager;
    private InjectionTarget<PicketBoxManager> injectionTarget;
    private Type configurationType;

    @SuppressWarnings("unchecked")
    public PicketBoxManagerBeanDefinition(BeanManager beanManager, Type configurationType) {
        this.beanManager = beanManager;
        this.configurationType = configurationType;
        AnnotatedType<? extends PicketBoxManager> at = null;

        if (isHTTPConfiguration()) {
            at = beanManager.createAnnotatedType(CDIPicketBoxHTTPManager.class);
        } else {
            at = beanManager.createAnnotatedType(CDIDefaultPicketBoxManager.class);
        }

        this.injectionTarget = (InjectionTarget<PicketBoxManager>) beanManager.createInjectionTarget(at);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.context.spi.Contextual#create(javax.enterprise.context.spi.CreationalContext)
     */
    @Override
    public PicketBoxManager create(CreationalContext<PicketBoxManager> creationalContext) {
        PicketBoxManager picketBoxManager = null;

        ConfigurationBuilder configurationBuilder = resolveConfigurationBuilder();

        configurationBuilder.eventManager().manager(new CDIAuthenticationEventManager(this.beanManager));
        
        PicketBoxConfiguration configuration = configurationBuilder.build();
        
        if (isHTTPConfiguration()) {
            picketBoxManager = new CDIPicketBoxHTTPManager((PicketBoxHTTPConfiguration) configuration);
        } else {
            picketBoxManager = new CDIDefaultPicketBoxManager(configuration);
        }

        picketBoxManager.start();

        return picketBoxManager;
    }

    /**
     * <p>
     * Resolves the {@link ConfigurationBuilder} instance to be used during the {@link PicketBoxManager} creation.
     * </p>
     * 
     * @return
     */
    @SuppressWarnings({ "unchecked", "serial" })
    private ConfigurationBuilder resolveConfigurationBuilder() {
        Set<Bean<?>> beans = this.beanManager.getBeans(ConfigurationBuilder.class, new AnnotationLiteral<Any>() {
        });

        if (beans.isEmpty()) {
            throw new IllegalStateException(
                    "No ConfigurationBuilder provided. Maybe you forgot to provide a @Producer method for the ConfigurationBuilder.");
        }

        Bean<ConfigurationBuilder> bean = (Bean<ConfigurationBuilder>) beans.iterator().next();

        CreationalContext<ConfigurationBuilder> createCreationalContext = this.beanManager.createCreationalContext(bean);

        return bean.create(createCreationalContext);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.context.spi.Contextual#destroy(java.lang.Object, javax.enterprise.context.spi.CreationalContext)
     */
    @Override
    public void destroy(PicketBoxManager instance, CreationalContext<PicketBoxManager> creationalContext) {
        this.injectionTarget.preDestroy(instance);
        instance.stop();
        this.injectionTarget.dispose(instance);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.inject.spi.Bean#getTypes()
     */
    @Override
    public Set<Type> getTypes() {
        Set<Type> types = new HashSet<Type>();

        if (isHTTPConfiguration()) {
            types.add(PicketBoxHTTPManager.class);
        }

        types.add(PicketBoxManager.class);
        types.add(Object.class);

        return types;
    }

    private boolean isHTTPConfiguration() {
        return HTTPConfigurationBuilder.class.equals(this.configurationType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.inject.spi.Bean#getQualifiers()
     */
    @SuppressWarnings("serial")
    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<Annotation>();

        qualifiers.add(new AnnotationLiteral<Default>() {
        });
        qualifiers.add(new AnnotationLiteral<Any>() {
        });

        return qualifiers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.inject.spi.Bean#getScope()
     */
    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.inject.spi.Bean#getName()
     */
    @Override
    public String getName() {
        return "PicketBoxManager";
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.inject.spi.Bean#getStereotypes()
     */
    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.inject.spi.Bean#getBeanClass()
     */
    @Override
    public Class<?> getBeanClass() {
        return PicketBoxManager.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.inject.spi.Bean#isAlternative()
     */
    @Override
    public boolean isAlternative() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.inject.spi.Bean#isNullable()
     */
    @Override
    public boolean isNullable() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.inject.spi.Bean#getInjectionPoints()
     */
    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return this.injectionTarget.getInjectionPoints();
    }

}