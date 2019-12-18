# An example CXF STS Spring Boot application secured by external Keycloak

This project contains a Spring Boot application which is an Apache CXF Security Token
Service (STS). It is derived from  Colm O hEigeartaigh's demo at https://github.com/coheigea/testcases/tree/master/apache/docker/cxf/sts-keycloak.
He wrote a nice blog post on this demo (https://coheigea.blogspot.com/2018/06/combining-keycloak-with-apache-cxf-sts.html), stating that he
would consider authenticating users via their bearer token rather than username and password, but that he could not find
an endpoint at Keycloak to achieve that. Actually, Keycloak does have such a REST endpoint (`/userinfo`), which we make use of in this project.

To build:
```
mvn clean install
```

To run in standalone mode on your machine:
```
mvn spring-boot:run
```

### Excerpt from the original Readme

To test:
 * Log onto the Keycloak admin console via: http://localhost:9080/auth/ (admin:password)
 * Create a new role and a new user, assigning the role mapping to the new user.
 * Go to the "Credentials" tab for the user you have created, and specify a
   password, unselecting the "Temporary" checkbox, and reset the password.
 * Use SOAP-UI to create a new SOAP project using the WSDL: http://localhost:8080/cxf-sts-keycloak/UT?wsdl
 * Click on the "Issue" Binding and change the SOAP Body content of the request
   message to:

```xml
   <ns:RequestSecurityToken>
     <t:TokenType xmlns:t="http://docs.oasis-open.org/ws-sx/ws-trust/200512">http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0</t:TokenType>
     <t:KeyType xmlns:t="http://docs.oasis-open.org/ws-sx/ws-trust/200512">http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer</t:KeyType>
     <t:RequestType xmlns:t="http://docs.oasis-open.org/ws-sx/ws-trust/200512">http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue</t:RequestType>
     <t:Claims xmlns:ic="http://schemas.xmlsoap.org/ws/2005/05/identity" xmlns:t="http://docs.oasis-open.org/ws-sx/ws-trust/200512" Dialect="http://schemas.xmlsoap.org/ws/2005/05/identity">
        <ic:ClaimType xmlns:ic="http://schemas.xmlsoap.org/ws/2005/05/identity" Uri="http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role"/>
     </t:Claims>
   </ns:RequestSecurityToken>
```

 * [Right] click on the request message and add a WS-Security UsernameToken
   and send the request. If successful, you should see a SAML Assertion in
   the right-hand pane.
