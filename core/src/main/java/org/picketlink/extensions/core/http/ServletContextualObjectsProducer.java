package org.picketlink.extensions.core.http;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletContextualObjectsProducer {

    @Inject
    private ServletContextualObjectsHolder holder;
    
    @Produces
    @RequestScoped
    public HttpServletRequest getRequest() {
        return this.holder.getRequest();
    }

    @Produces
    @RequestScoped
    public HttpServletResponse getResponse() {
        return this.holder.getResponse();
    }

}
