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

import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import org.picketbox.core.Credential;
import org.picketbox.core.PicketBoxPrincipal;
import org.picketbox.core.authentication.AuthenticationInfo;
import org.picketbox.core.authentication.AuthenticationResult;
import org.picketbox.core.authentication.credential.OTPCredential;
import org.picketbox.core.authentication.impl.AbstractAuthenticationMechanism;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.core.util.TimeBasedOTP;
import org.picketbox.core.util.TimeBasedOTPUtil;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.social.standalone.fb.FacebookProcessor;

/**
 * An authentication mechanism for OTP SignIn
 * 
 * @author Anil Saldhana
 * @author Pedro Silva
 */
public class OTPAuthenticationMechanism extends AbstractAuthenticationMechanism {

    protected String returnURL;
    protected String clientID;
    protected String clientSecret;
    protected String scope = "email";

    protected FacebookProcessor processor;
    
    @Inject
    private IdentityManager identityManager;
    
    private String algorithm = TimeBasedOTP.HMAC_SHA1;
    //This is the number of digits in the totp
    private int NUMBER_OF_DIGITS = 6;

    public OTPAuthenticationMechanism() {
        clientID = System.getProperty("TWIT_CLIENT_ID");
        clientSecret = System.getProperty("TWIT_CLIENT_SECRET");
        returnURL = System.getProperty("TWIT_RETURN_URL");
    }

    @Override
    public List<AuthenticationInfo> getAuthenticationInfo() {
        ArrayList<AuthenticationInfo> info = new ArrayList<AuthenticationInfo>();

        info.add(new AuthenticationInfo("OTP Authentication", "Provides OTP authentication.", OTPCredential.class));

        return info;
    }

    /* (non-Javadoc)
     * @see org.picketbox.core.authentication.impl.AbstractAuthenticationMechanism#doAuthenticate(org.picketbox.core.authentication.AuthenticationManager, org.picketbox.core.Credential, org.picketbox.core.authentication.AuthenticationResult)
     */
    @Override
    protected Principal doAuthenticate(Credential credential, AuthenticationResult result) throws AuthenticationException {
        OTPCredential otpCredential = (OTPCredential) credential;
        
        String username = otpCredential.getUserName();
        String pass = otpCredential.getPassword();
        String otp = otpCredential.getOtp();
        
        Principal principal = null;
        
        User user = identityManager.getUser(username);
        
        if(user != null){
            boolean validation = identityManager.validatePassword(user, pass);
            if(validation){
                //Validate OTP
                String seed = user.getAttribute("serial");
                if( seed != null){
                    try {
                        if( algorithm.equals( TimeBasedOTP.HMAC_SHA1 ))
                        {
                           //validation =  TimeBasedOTPUtil.validate( otp, seed.getBytes() , NUMBER_OF_DIGITS );
                           validation =  validate( otp, seed.getBytes() , NUMBER_OF_DIGITS );
                        }
                        else if( algorithm.equals( TimeBasedOTP.HMAC_SHA256 ))
                        {
                            validation =  TimeBasedOTPUtil.validate256( otp, seed.getBytes() , NUMBER_OF_DIGITS ); 
                        }
                        else if( algorithm.equals( TimeBasedOTP.HMAC_SHA512 ))
                        {
                            validation =  TimeBasedOTPUtil.validate512( otp, seed.getBytes() , NUMBER_OF_DIGITS ); 
                        }
                    } catch (GeneralSecurityException e) {
                        throw new AuthenticationException(e);
                    }
                }
            }
            if(validation){
                principal = new PicketBoxPrincipal(username);
                checkUserInStore(principal);
            }
        }
        return principal;
    }
    
    private void checkUserInStore(Principal principal){
        if(identityManager != null){
            User newUser = null;
            
            User storedUser = identityManager.getUser(principal.getName());
            if(storedUser == null){ 
                newUser = identityManager.createUser(principal.getName());
                newUser.setFirstName(principal.getName());

                Role guest = this.identityManager.createRole("guest");
                Group guests = identityManager.createGroup("Guests");

                identityManager.grantRole(guest, newUser, guests);
            }
        }
    }
    
    public static boolean validate(String submittedOTP, byte[] secret, int numDigits) throws GeneralSecurityException {
        System.out.println("Submitted OTP=" + submittedOTP);
        long TIME_INTERVAL = 30 * 1000; // 30 secs
        TimeZone utc = TimeZone.getTimeZone("UTC");
        Calendar currentDateTime = Calendar.getInstance(utc);

        String generatedTOTP = TimeBasedOTP.generateTOTP(new String(secret), numDigits);
        boolean result = generatedTOTP.equals(submittedOTP);
        long timeInMilis = currentDateTime.getTimeInMillis(); 
        
        System.out.println(" OTP=" + generatedTOTP);


        if (!result) {
            timeInMilis -= TIME_INTERVAL;

            generatedTOTP = TimeBasedOTP.generateTOTP(new String(secret), "" + timeInMilis, numDigits);

            System.out.println(" OTP=" + generatedTOTP);
            result = generatedTOTP.equals(submittedOTP);
        }
        
        if (!result) {
            timeInMilis -= TIME_INTERVAL;

            generatedTOTP = TimeBasedOTP.generateTOTP(new String(secret), "" + timeInMilis, numDigits);
            System.out.println(" OTP=" + generatedTOTP);
            result = generatedTOTP.equals(submittedOTP);
        }

        if (!result) {
            timeInMilis += TIME_INTERVAL;
            generatedTOTP = TimeBasedOTP.generateTOTP(new String(secret), "" + timeInMilis, numDigits);
            System.out.println(" OTP=" + generatedTOTP);
            result = generatedTOTP.equals(submittedOTP);
        }
        
        if (!result) {
            timeInMilis += TIME_INTERVAL;
            generatedTOTP = TimeBasedOTP.generateTOTP(new String(secret), "" + timeInMilis, numDigits);
            System.out.println(" OTP=" + generatedTOTP);
            result = generatedTOTP.equals(submittedOTP);
        }

        return result;
    }
}