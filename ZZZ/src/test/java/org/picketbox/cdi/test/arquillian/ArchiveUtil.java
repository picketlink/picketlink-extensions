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

package org.picketbox.cdi.test.arquillian;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.picketbox.cdi.PicketBoxExtension;

/**
 * <p>Utility class with common methods to handle ShrinkWrap archives.</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class ArchiveUtil {

    /**
     * <p>
     * Creates a simple {@link WebArchive} for deployment with the necessary structure/configuration to run the tests.
     * </p>
     *
     * @return
     */
    public static WebArchive createTestArchive() {
        WebArchive archive = ShrinkWrap
                .create(WebArchive.class, "test.jar")
                .addAsManifestResource(getBeansXml(), ArchivePaths.create("beans.xml"))
                .addPackages(true, new Filter<ArchivePath>() {
                    @Override
                    public boolean include(ArchivePath object) {
                        return object.get().indexOf("/test/") == -1;
                    }}, PicketBoxExtension.class.getPackage())
                .addAsLibraries(
                        ShrinkWrap.createFromZipFile(
                                JavaArchive.class,
                                DependencyResolvers.use(MavenDependencyResolver.class)
                                        .loadMetadataFromPom("pom.xml")
                                        .artifact("org.apache.deltaspike.modules:deltaspike-security-module-impl").goOffline()
                                        .resolveAsFiles()[0]))
                .addAsLibraries(
                        ShrinkWrap.createFromZipFile(
                                JavaArchive.class,
                                DependencyResolvers.use(MavenDependencyResolver.class)
                                        .loadMetadataFromPom("pom.xml")
                                        .artifact("org.picketlink:picketlink-core-impl").goOffline()
                                        .resolveAsFiles()[0]))
                .addAsLibraries(
                        ShrinkWrap.createFromZipFile(
                                JavaArchive.class,
                                DependencyResolvers.use(MavenDependencyResolver.class)
                                        .loadMetadataFromPom("pom.xml")
                                        .artifact("org.picketlink:picketlink-core-api").goOffline()
                                        .resolveAsFiles()[0]))
                .addAsLibraries(
                        ShrinkWrap.createFromZipFile(
                                JavaArchive.class,
                                DependencyResolvers.use(MavenDependencyResolver.class).goOffline()
                                        .loadMetadataFromPom("pom.xml")
                                        .artifact("org.picketbox:picketbox-core")
                                        .resolveAsFiles()[0]));

        return archive;
    }

    private static Asset getBeansXml() {
        Asset beansXml = new StringAsset("<beans></beans>");

        return beansXml;
    }

}
