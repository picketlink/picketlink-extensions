package org.picketlink.extensions.core.pbox;

import org.picketbox.http.DefaultPicketBoxHTTPManager;
import org.picketbox.http.config.PicketBoxHTTPConfiguration;

public class CDIPicketBoxHTTPManager extends DefaultPicketBoxHTTPManager {

    public CDIPicketBoxHTTPManager() {
        this(null);
        throw new InstantiationError("This constructor only exists to follow CDI rules for proxiable beans.");
    }

    public CDIPicketBoxHTTPManager(PicketBoxHTTPConfiguration configuration) {
        super(configuration);
    }

}
