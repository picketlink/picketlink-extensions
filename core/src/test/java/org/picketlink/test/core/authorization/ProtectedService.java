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

package org.picketlink.test.core.authorization;

import org.picketlink.extensions.core.pbox.authorization.RolesAllowed;
import org.picketlink.extensions.core.pbox.authorization.UserLoggedIn;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class ProtectedService {

    @RolesAllowed ({"Manager"})
    public void onlyForManagersOperation() {

    }

    public void unProtectedMethod() {

    }

    @UserLoggedIn
    public void onlyForAuthenticatedUsers() {

    }

    @RolesAllowed ({"Executive"})
    public void onlyForExecutives() {

    }
}
