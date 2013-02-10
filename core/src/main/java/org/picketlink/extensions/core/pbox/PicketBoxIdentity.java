package org.picketlink.extensions.core.pbox;

import org.picketbox.core.UserContext;
import org.picketlink.Identity;
import org.picketlink.authentication.AuthenticationException;

public interface PicketBoxIdentity extends Identity {

    boolean hasRole(String restrictedRole);

    UserContext getUserContext();

    boolean hasGroup(String name);

    boolean restoreSession(String token) throws AuthenticationException;

}