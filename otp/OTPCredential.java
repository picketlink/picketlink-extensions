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
package org.aerogear.todo.server.security.authc.otp;

import org.picketbox.core.authentication.credential.UsernamePasswordCredential;

/**
 * Extension of {@link UsernamePasswordCredential} to allow OTP
 * @author anil saldhana
 * @since Oct 9, 2012
 */
public class OTPCredential extends UsernamePasswordCredential {
    private String otp;
    
    public OTPCredential(String userName, String password, String otp) {
        super(userName, password);
        this.otp = otp;
    }

    /**
     * Get the One Time Password Value
     * @return
     */
    public String getOtp() {
        return otp;
    }
    /**
     * Set the One Time Password Value
     * @param otp
     */
    public void setOtp(String otp) {
        this.otp = otp;
    }
}