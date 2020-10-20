package com.task.manager.configuration;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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

    @Autowired
    FilterService filterService;

    @Autowired
    private ProcessEngine engine;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {

        managementService.toggleTelemetry(false);

        final Group administration = createGroup("administration", "Администрация");
        final Group accountants = createGroup("accountants", "Бухгалтерский Учёт");
        final Group workers = createGroup("workers", "Сотрудники");
        final Group secretariat = createGroup("secretariat", "Секретариат");


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

        final User secretary = createUser("secretary",
                "secretary",
                "secretary@emai.com",
                "Анна",
                "Олеговна");

        identityService.createMembership(director.getId(), administration.getId());
        identityService.createMembership(accountant.getId(), accountants.getId());
        identityService.createMembership(manager.getId(), workers.getId());
        identityService.createMembership(secretary.getId(), secretariat.getId());

        grantAccessToApplication(administration.getId(), "cockpit");
        grantAccessToApplication(administration.getId(), "tasklist");
        grantAccessToApplication(workers.getId(), "tasklist");
        grantAccessToApplication(accountants.getId(), "tasklist");
        grantAccessToApplication(secretariat.getId(), "tasklist");


        createFilterByGroup(administration, "Задачи Администрации");
        grantAccessToFilters(administration.getId(), "*", Permissions.ALL);

        Filter filter;
        filter = createFilterByGroup(accountants, "Задачи Бухгалтерии");
        grantAccessToFilters(accountants.getId(), filter.getId(), Permissions.READ);

        filter = createFilterByGroup(accountants, "Задачи Сотрудникам");
        grantAccessToFilters(workers.getId(), filter.getId(), Permissions.READ);

        filter = createFilterByGroup(accountants, "Задачи Секретариата");
        grantAccessToFilters(secretariat.getId(), filter.getId(), Permissions.READ);


        TaskQueryDto taskQueryDto  = new TaskQueryDto();
        taskQueryDto.setAssignee("${currentUser()}");
        filter = filterService.newTaskFilter("Мои задачи")
                .setQuery(taskQueryDto.toQuery(engine));
        filterService.saveFilter(filter);
        grantAccessToFilters(secretariat.getId(), filter.getId(), Permissions.READ);
        grantAccessToFilters(workers.getId(), filter.getId(), Permissions.READ);
        grantAccessToFilters(accountants.getId(), filter.getId(), Permissions.READ);
        grantAccessToFilters(administration.getId(), filter.getId(), Permissions.READ);

    }

    private Filter createFilterByGroup(Group candidateGroup, String filterName) {
        TaskQueryDto taskQueryDto  = new TaskQueryDto();
        taskQueryDto.setCandidateGroup(candidateGroup.getId());
        Filter filter = filterService.newTaskFilter(filterName)
                .setQuery(taskQueryDto.toQuery(engine));
        filterService.saveFilter(filter);
        return filter;
    }

    private void grantAccessToFilters(String groupId, String resourceId, Permissions... permissions) {
        createAuth(groupId, resourceId, 5, permissions);
    }

    private void grantAccessToApplication(String groupId, String resourceId) {
        createAuth(groupId, resourceId, 0, Permissions.ALL);
    }

    public void createAuth(String groupId, String resourceId, int resourceTypeId, Permissions... permissions) {
        final Authorization newAuthorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
        Arrays.asList(permissions).forEach(p -> newAuthorization.addPermission(p));
        newAuthorization.setGroupId(groupId);
        newAuthorization.setResourceId(resourceId);
        newAuthorization.setResourceType(resourceTypeId);
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
