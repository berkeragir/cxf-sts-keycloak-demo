package org.apache.coheigea.cxf.sts;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import lombok.Data;

public class KeycloakAuthValidator extends AbstractSoapInterceptor {

    private String address;
    private String realm;
    private ObjectMapper mapper;

    private static org.apache.commons.logging.Log log =
    org.apache.commons.logging.LogFactory.getLog(KeycloakAuthValidator.class);

    public KeycloakAuthValidator() {
        super(Phase.PRE_PROTOCOL);

        this.mapper = new ObjectMapper();
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {

            final HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
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
                    HttpServletResponse httpResponse = (HttpServletResponse) message.get(AbstractHTTPDestination.HTTP_RESPONSE);
                    httpResponse.reset();
                    httpResponse.setStatus(401);

                    Fault fault = new Fault(new AuthenticationException("Unauthorized"));
                    fault.setStatusCode(401);
                    throw fault;
                } else {
                    HttpServletResponse httpResponse = (HttpServletResponse) message.get(AbstractHTTPDestination.HTTP_RESPONSE);
                    httpResponse.reset();
                    httpResponse.setStatus(400);

                    Fault fault = new Fault(new Exception("Error occured"));
                    fault.setStatusCode(400);
                    throw fault;
                }

                response.close();
            }


        /*if (message.getContextualProperty("Authorization") != null) {
            log.warn("Auth exists: " + message.getContextualProperty("Authorization"));
        }

        final List<Header> headers = message.;

        log.warn("HEADERS:");

        for (final Header h : headers) {
            log.warn(h.getName());
        }*/

        //if you want to read more http header messages, just use getÂ method to obtain fromÂ Â HttpServletRequest.
        /*HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        if(null!=request){

            //Read http header to get HeaderNames
            Enumeration enums = request.getHeaderNames();

            while(enums.hasMoreElements()) {
                String header = (String) enums.nextElement();
                log.warn(header + ": " + request.getHeader(header));
            }

            log.warn("AUTH: " + request.getHeader("Authorization"));

        }*/
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
        private boolean email_verified;
        private String sub;
        private String name;
        private String given_name;
        private String family_name;
        private String email;

    }
}