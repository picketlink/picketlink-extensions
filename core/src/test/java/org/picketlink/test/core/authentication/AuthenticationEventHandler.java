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

package org.picketlink.test.core.authentication;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.picketbox.core.authentication.event.UserAuthenticatedEvent;
import org.picketbox.core.authentication.event.UserAuthenticationFailedEvent;
import org.picketbox.core.authentication.event.UserNotAuthenticatedEvent;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@ApplicationScoped
public class AuthenticationEventHandler {

    private boolean successfulAuthentication;
    private boolean authenticationFailed;

    public void onSuccessfulAuthentication(@Observes UserAuthenticatedEvent userAuthenticatedEvent) {
        this.successfulAuthentication = true;
    }

    public void onUnSuccessfulAuthentication(@Observes UserNotAuthenticatedEvent userAuthenticatedEvent) {
        this.successfulAuthentication = false;
    }

    public void onUnSuccessfulAuthentication(@Observes UserAuthenticationFailedEvent userAuthenticatedEvent) {
        this.authenticationFailed = false;
    }

    public boolean isSuccessfulAuthentication() {
        return this.successfulAuthentication;
    }

    public boolean isAuthenticationFailed() {
        return this.authenticationFailed;
    }

}
