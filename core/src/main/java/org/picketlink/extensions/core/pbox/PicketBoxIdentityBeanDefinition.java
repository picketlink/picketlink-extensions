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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.AnnotationLiteral;

import org.picketbox.http.config.HTTPConfigurationBuilder;
import org.picketlink.Identity;
import org.picketlink.extensions.core.http.PicketBoxHTTPIdentity;

/**
 * <p>
 * {@link Bean} implementation to define/customize the behaviour for {@link DefaultPicketBoxIdentity} instances.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class PicketBoxIdentityBeanDefinition implements Bean<Identity> {

    private BeanManager beanManager;
    private InjectionTarget<Identity> injectionTarget;
    private Type configurationType;

    @SuppressWarnings("unchecked")
    public PicketBoxIdentityBeanDefinition(BeanManager beanManager, Type configurationType) {
        this.beanManager = beanManager;
        this.configurationType = configurationType;
        AnnotatedType<? extends Identity> at = null;
        
        if (isHTTPConfiguration()) {
            at = beanManager.createAnnotatedType(PicketBoxHTTPIdentity.class);    
        } else {
            at = beanManager.createAnnotatedType(DefaultPicketBoxIdentity.class);
        }
        
        this.injectionTarget = (InjectionTarget<Identity>) beanManager.createInjectionTarget(at);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.context.spi.Contextual#create(javax.enterprise.context.spi.CreationalContext)
     */
    @Override
    public Identity create(CreationalContext<Identity> creationalContext) {
        Identity identity = this.injectionTarget.produce(creationalContext);
        
        this.injectionTarget.inject(identity, creationalContext);
        this.injectionTarget.postConstruct(identity);
        
        return identity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.context.spi.Contextual#destroy(java.lang.Object, javax.enterprise.context.spi.CreationalContext)
     */
    @Override
    public void destroy(Identity instance, CreationalContext<Identity> creationalContext) {
        this.injectionTarget.preDestroy(instance);
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
        
//        if (isHTTPConfiguration()) {
//            types.add(PicketBoxHTTPIdentity.class);
//        }
        
        types.add(PicketBoxIdentity.class);
        types.add(Identity.class);
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
        return SessionScoped.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.enterprise.inject.spi.Bean#getName()
     */
    @Override
    public String getName() {
        return "identity";
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
        if (isHTTPConfiguration()) {
            return PicketBoxHTTPIdentity.class;
        } else {
            return DefaultPicketBoxIdentity.class;
        }
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