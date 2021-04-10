package org.leikin;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;

import java.util.Collections;
import java.util.List;

public class KeycloakAdminClientExample {

    public static void main(String[] args) {

        String kcServerUrl = "http://localhost:8080/auth/";
        String kcMasterRealm = "master";
        String kcAdminUser = "admin";
        String kcAdminPassword = "admin";
        String kcAdminClientID = "admin-cli";

        String newRealmName = "Demo";
        String newClientName = "demo-client";
        String newRoleName = "demo-permission";
        String newUsername = "georgi";
        String newUserFirstName = "Georgi";
        String newUserLastName = "Demo";
        String newUserPassword = "admin";
        String newUserEmail = "demo@useremail.com";

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

        // Create a client role
        RolesResource clientRolesResource = realmResource.clients().get(clientRepresentation.getId()).roles();
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(newRoleName);
        clientRolesResource.create(roleRepresentation);

        // Add client role mapping to an existing user
        UserRepresentation existingUser = usersResource.search(newUsername).get(0);
        RoleMappingResource roleMappingResource = usersResource.get(existingUser.getId()).roles();
        RoleScopeResource roleScopeResource = roleMappingResource.clientLevel(clientRepresentation.getId());
        RoleRepresentation existingRole = roleScopeResource.listAvailable().get(0);

        roleScopeResource.add(Collections.singletonList(existingRole));

        // Get user's client role mapping
        List<RoleRepresentation> userClientRoleMapping = roleScopeResource.listEffective();

        keycloak.close();
    }
}
