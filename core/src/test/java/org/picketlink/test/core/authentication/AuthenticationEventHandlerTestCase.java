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

package org.picketlink.test.core.authentication;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketbox.cdi.test.arquillian.ArchiveUtil;

/**
 * <p>
 * Tests user authentication.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AuthenticationEventHandlerTestCase extends AbstractAuthenticationTestCase {

    @Inject
    private AuthenticationEventHandler eventHandler;

    /**
     * <p>
     * Creates a simple {@link WebArchive} for deployment with the necessary structure/configuration to run the tests.
     * </p>
     *
     * @return
     */
    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ArchiveUtil.createTestArchive();

        archive.addPackages(true, AuthenticationEventHandlerTestCase.class.getPackage());

        return archive;
    }

    /**
     * <p>Tests a successful authentication.</p>
     *
     * @throws Exception
     */
    @Test
    public void testSuccessfullAuthentication() throws Exception {
        Assert.assertTrue(this.eventHandler.isSuccessfulAuthentication());
    }

}