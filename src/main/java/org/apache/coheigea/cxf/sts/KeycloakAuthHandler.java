package org.apache.coheigea.cxf.sts;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import lombok.Data;

public class KeycloakAuthHandler implements SOAPHandler {

    private String address;
    private String realm;
    private ObjectMapper mapper;

    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(KeycloakAuthHandler.class);

    public KeycloakAuthHandler() {
        super();
        this.mapper = new ObjectMapper();
    }

    @Override
    public void close(final MessageContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean handleFault(final MessageContext context) {
        // TODO Auto-generated method stub

        log.warn("***** HANDLER handleFault() CALLED *****");

        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        log.warn("Is outBound: " + outboundProperty);

        return false;
    }

    @Override
    public boolean handleMessage(final MessageContext context) {
        
        // if you want to read more http header messages, just use getÂ method to obtain
        // fromÂ Â HttpServletRequest.

        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);


        if(!outboundProperty) {

            final HttpServletRequest request = (HttpServletRequest) context.get(AbstractHTTPDestination.HTTP_REQUEST);
            if (null != request) {

                // Read http header to get HeaderNames
                
                final String authToken = request.getHeader("Authorization");

                log.warn("AUTH: " + authToken);

                String userinfoUrl = address + "/realms/" + realm + "/protocol/openid-connect/userinfo";
                log.warn("Will query on: " + userinfoUrl);

                ResteasyClient client = new ResteasyClientBuilder().build();

                // Register your custom header here
                client.register(new ClientRequestFilter(){
                
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        requestContext.getHeaders().add("Authorization", authToken);
                    }
                });
      

                ResteasyWebTarget target = client.target(userinfoUrl);
                Response response = target.request().get();

                log.warn("Keycloak userinfo get response status: " + response.getStatusInfo().toString());

                if(response.getStatus() == 200) {
                    String body = response.readEntity(String.class);
                    log.warn("Response entity: " + body);
                    
                    try {
                        UserInfo userInfo = mapper.readValue(body, UserInfo.class);
                        log.warn(userInfo.toString());

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (response.getStatus() == 401) {
                    HttpServletResponse httpResponse = (HttpServletResponse) context.get(AbstractHTTPDestination.HTTP_RESPONSE);
                    httpResponse.reset();
                    httpResponse.setStatus(401);

                    return false;
                } else {
                    HttpServletResponse httpResponse = (HttpServletResponse) context.get(AbstractHTTPDestination.HTTP_RESPONSE);
                    httpResponse.reset();
                    httpResponse.setStatus(400);

                    return false;
                }

                response.close();
            }
        }

        return true;
    }

    @Override
    public Set getHeaders() {
        // TODO Auto-generated method stub
        return null;
    }


    public void setAddress(final String newAddress) {
        address = newAddress;
    }

    public String getAddress() {
        return address;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(final String realm) {
        this.realm = realm;
    }

    @Data
    public static class UserInfo {
        private String preferred_username;
        private String email_verified;
        private String sub;
    }
}