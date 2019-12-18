/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.coheigea.cxf.sts;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.common.security.SecurityToken;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.token.BinarySecurity;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.message.token.UsernameToken;
import org.apache.wss4j.dom.validate.Credential;
import org.apache.wss4j.dom.validate.Validator;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

/**
 * This is a custom Validator that authenticates to a Keycloak IDM and checks to see whether the
 * supplied username and password are in the system.
 */
public class KeycloakTokenValidator implements Validator {

    private static org.apache.commons.logging.Log log =
            org.apache.commons.logging.LogFactory.getLog(KeycloakTokenValidator.class);

    private String address;
    private String realm;

    public Credential validate(final Credential credential, final RequestData data) throws WSSecurityException {

        // Validate the token

        MessageContext msgContext = ((MessageContext) data.getMsgContext());
        final HttpServletRequest request = (HttpServletRequest) msgContext.get(AbstractHTTPDestination.HTTP_REQUEST);

        


        Keycloak keycloak = KeycloakBuilder.builder()
            .realm(realm)
            .grantType(OAuth2Constants.ACCESS_TOKEN)
            .build();
            

        try {

        } catch (final ForbiddenException ex) {
            // We allow 403 here as we only care about authentication. 403 means authentication succeeds but
            // the user might not have the permissions to access the admin-cli
        } catch (final RuntimeException ex) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
        }

        // TODO: How to handle SAML token invalidation?

        return credential;
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

}
