package com.task.manager.configuration;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CamundaConfigurer {

 /*   @Autowired
    private UserRestService userRestService;*/

    @Autowired
    private IdentityService identityService;

    @Autowired
    ManagementService managementService;

    @Autowired
    AuthorizationService authorizationService;


    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {

        managementService.toggleTelemetry(false);

        final Group directors = createGroup("directors", "Директорa");
        final Group accountants = createGroup("accountants", "Бухгалтера");
        final Group managers = createGroup("managers", "Менеджеры");


        final User director = createUser("director",
                "director",
                "director@emai.com",
                "Иван",
                "Иванович");

        final User accountant = createUser("accountant",
                "accountant",
                "accountant@emai.com",
                "Василий",
                "Васильевич");

        final User manager = createUser("manager",
                "manager",
                "manager@emai.com",
                "Ольга",
                "Дмитриевна");
        identityService.createMembership(director.getId(), directors.getId());
        identityService.createMembership(accountant.getId(), accountants.getId());
        identityService.createMembership(manager.getId(), managers.getId());

        grantAccessToApplication("directors", "cockpit");
        grantAccessToApplication("directors", "tasklist");
        grantAccessToApplication("managers", "tasklist");
        grantAccessToApplication("accountants", "tasklist");
    }

    private void grantAccessToApplication(String groupId, String resourceId) {
        final Authorization newAuthorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
        newAuthorization.addPermission(Permissions.ALL);
        newAuthorization.setGroupId(groupId);
        newAuthorization.setResourceId(resourceId);
        newAuthorization.setResourceType(0);
        authorizationService.saveAuthorization(newAuthorization);
    }

    private User createUser(String login, String password, String email, String firstName, String lastName) {
        final User newUser = identityService.newUser(login);
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPassword(password);
        identityService.saveUser(newUser);
        return newUser;
    }

    private Group createGroup(String groupId, String groupName) {
        final Group newGroup = identityService.newGroup(groupId);
        newGroup.setName(groupName);
        identityService.saveGroup(newGroup);
        return newGroup;
    }

    private UserDto create(String login, String position, String name, String lastName) {
        UserDto userDto = new UserDto();
        userDto.setCredentials(createCredentials(login));
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setFirstName(name);
        userProfileDto.setLastName(lastName);
        userProfileDto.setId(login);
        userDto.setProfile(userProfileDto);
        return userDto;
    }

    private UserCredentialsDto createCredentials(String password) {
        UserCredentialsDto userCredentialsDto = new UserCredentialsDto();
        userCredentialsDto.setPassword(password);
        return userCredentialsDto;
    }

}
