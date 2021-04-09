package org.leikin;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;

public class KeycloakAdminClientExample {

    public static void main(String[] args) {

        String kcServerUrl = "http://localhost:8080/auth/";
        String kcMasterRealm = "master";
        String kcAdminUser = "admin";
        String kcAdminPassword = "admin";
        String kcAdminClientID = "admin-cli";

        String newRealmName = "Demo";
        String newClientName = "app-client";
        String newRoleName = "read-permission";
        String newUsername = "georgi";
        String newUserFirstName = "Georgi";
        String newUserLastName = "Leikin";
        String newUserPassword = "admin";
        String newUserEmail = "user@email.com";

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(kcServerUrl)
                .realm(kcMasterRealm)
                .username(kcAdminUser)
                .password(kcAdminPassword)
                .clientId(kcAdminClientID)
                .build();

        // Create a new realm
        RealmRepresentation newRealm = new RealmRepresentation();
        newRealm.setRealm(newRealmName);
        newRealm.setEnabled(true);

        keycloak.realms().create(newRealm);

        // Create a new client
        ClientRepresentation newClient = new ClientRepresentation();
        newClient.setClientId(newClientName);

        keycloak.realms().realm(newRealmName).clients().create(newClient);

        // Get new client secret
        RealmResource realmResource = keycloak.realms().realm(newRealmName);
        ClientsResource clientsResource = realmResource.clients();
        ClientRepresentation clientRepresentation = clientsResource.findByClientId(newClientName).get(0);

        // Regenerate client secret
        CredentialRepresentation credentialRepresentation = clientsResource.get(clientRepresentation.getId()).generateNewSecret();

        // Create new realm user
        UsersResource usersResource = realmResource.users();

        // Create user password
        CredentialRepresentation userPassword = new CredentialRepresentation();
        userPassword.setType(CredentialRepresentation.PASSWORD);
        userPassword.setValue(newUserPassword);
        userPassword.setTemporary(false);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(newUsername);
        userRepresentation.setFirstName(newUserFirstName);
        userRepresentation.setLastName(newUserLastName);
        userRepresentation.setEmail(newUserEmail);
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(Collections.singletonList(userPassword));
        usersResource.create(userRepresentation);

        keycloak.close();
    }
}
