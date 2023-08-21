package pl.wolniarskim.project_management.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.wolniarskim.project_management.models.*;
import pl.wolniarskim.project_management.models.DTO.*;
import pl.wolniarskim.project_management.repositories.*;
import pl.wolniarskim.project_management.services.ProjectService;
import pl.wolniarskim.project_management.util.AuthUtil;

import java.time.LocalDate;
import java.util.List;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class ProjectResourceTest {


    @Autowired
    MockMvc mockMvc;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ProjectService projectService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectUserRepository projectUserRepository;
    @Autowired
    TaskGroupRepository taskGroupRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Test
    void shouldReturn404WhenIsNotAuthorize() throws Exception {
        // when + then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/projects"))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    void shouldReturnAllProjectsForGivenUser() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, PROJECT_READ));
        createFakeProjects();
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        ProjectWriteModel projectWriteModel2 = new ProjectWriteModel();
        projectWriteModel2.setName("Test 2");
        projectWriteModel2.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel2.setDescription("Test 2");
        projectWriteModel2.setEndTime(LocalDate.now());
        projectWriteModel2.setStartTime(LocalDate.now());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken));

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .content(objectMapper.writeValueAsString(projectWriteModel2))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken));
        // when
        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/projects")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        SimpleProjectReadModel[] readModels = objectMapper.readValue(authorization.getResponse().getContentAsString(), SimpleProjectReadModel[].class);
        // then
        Assertions.assertEquals(2, readModels.length);
    }

    @Test
    void shouldReturnProjectOnlyConnectedToUser() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, PROJECT_READ, ORGANIZATION_ADD_USER, PROJECT_ADD_USER));
        addUserToOrganization(authToken, "test2@wp.pl");
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        ProjectWriteModel projectWriteModel2 = new ProjectWriteModel();
        projectWriteModel2.setName("Test 2");
        projectWriteModel2.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel2.setDescription("Test 2");
        projectWriteModel2.setEndTime(LocalDate.now());
        projectWriteModel2.setStartTime(LocalDate.now());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken));

        MvcResult project = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .content(objectMapper.writeValueAsString(projectWriteModel2))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken)).andReturn();

        ProjectReadModel projectReadModel = objectMapper.readValue(project.getResponse().getContentAsString(), ProjectReadModel.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + projectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "INTERNAL")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findAll().get(0);


        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset/" + resetPasswordToken.getToken())
                .content("test"));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials("test2@wp.pl", "test"))))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TokensResponse tokensResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TokensResponse.class);

        // when
        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/projects")
                        .header("Authorization", "Bearer " + tokensResponse.getAccessToken()))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        SimpleProjectReadModel[] readModels = objectMapper.readValue(authorization.getResponse().getContentAsString(), SimpleProjectReadModel[].class);
        // then
        Assertions.assertEquals(1, readModels.length);
    }

    @Test
    void shouldUpdateGivenProject() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, PROJECT_UPDATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andReturn();

        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        projectWriteModel.setName("Test 120");

        // when + then
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/projects/update/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andReturn();

        Assertions.assertEquals("Test 120", objectMapper.readValue(response.getResponse().getContentAsString(), ProjectReadModel.class).getName());
    }

    @Test
    void shouldNotUpdateProjectWhenUserHasNoPermission() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();

        Assertions.assertEquals(1, projectRepository.findAll().size());

        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);
        projectWriteModel.setName("Test 120");

        // when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/projects/update/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals("Test 1", projectRepository.findAll().get(0).getName());
    }

    @Test
    void shouldNotUpdateProjectWhenUserIsFromAnotherOrganization() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, PROJECT_UPDATE));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(PROJECT_CREATE, PROJECT_UPDATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();

        Assertions.assertEquals(1, projectRepository.findAll().size());

        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        projectWriteModel.setName("Test 120");

        // when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/projects/update/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectWriteModel))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403)).andReturn();

        Assertions.assertEquals("Test 1", projectRepository.findAll().get(0).getName());
    }


    @Test
    void shouldCreateProject() throws Exception {
        // given
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + firstAuthToken)).andReturn();

        Assertions.assertEquals(1, projectRepository.count());
    }

    @Test
    void shouldNotCreateProjectWhenUserHasNoPermission() throws Exception {
        // given
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_UPDATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals(0, projectRepository.count());
    }

    @Test
    void shouldDeleteProject() throws Exception {
        //given
        String firstAuthToken = authUtil.getUserWithRole("test101@wp.pl", List.of(PROJECT_DELETE, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 100");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 100");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + firstAuthToken)).andReturn();
        Project project = projectRepository.findAll().get(0);
        Assertions.assertEquals(1, projectRepository.count());
        //when
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/projects/delete/"+project.getId())
                .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        //todo sprawdzic czy wszystkie grupy zadan, zadania i powiazania z uzytkownikami sa usuniete
        Assertions.assertEquals(0, projectRepository.count());
    }

    @Test
    void shouldNotDeleteProjectWhenUserHasNoPermission() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test101@wp.pl", List.of(PROJECT_READ, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();

        Assertions.assertEquals(1, projectRepository.count());

        SimpleProjectReadModel simpleProjectReadModel =
                objectMapper.readValue(result.getResponse().getContentAsString(), SimpleProjectReadModel.class);


        MvcResult r = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + simpleProjectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "USER")
                        .header("Authorization", "Bearer " + authToken))
                .andReturn();

        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        projectWriteModel.setName("Test 120");

        // when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/projects/delete/" + projectReadModel.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403)).andReturn();

        Assertions.assertEquals(1, projectRepository.count());
    }

    @Test
    void shouldNotDeleteProjectWhenUserIsFromAnotherOrganization() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test101@wp.pl", List.of(PROJECT_DELETE, PROJECT_CREATE));
        String secondToken = authUtil.getUserWithRole("test102@wp.pl", List.of(PROJECT_DELETE, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();

        Assertions.assertEquals(1, projectRepository.count());

        SimpleProjectReadModel simpleProjectReadModel =
                objectMapper.readValue(result.getResponse().getContentAsString(), SimpleProjectReadModel.class);


        MvcResult r = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + simpleProjectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "USER")
                        .header("Authorization", "Bearer " + authToken))
                .andReturn();

        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        projectWriteModel.setName("Test 120");

        // when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/projects/delete/" + projectReadModel.getId())
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403)).andReturn();

        Assertions.assertEquals(1, projectRepository.count());
    }

    @Test
    void shouldDeleteProjectWithAllAssociatedUsersAndTaskGroupsAndTasks() throws Exception {
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, TASK_GROUP_CREATE, TASK_CREATE, PROJECT_DELETE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + firstAuthToken)).andReturn();
        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel = new TaskGroupWriteModel();
        taskGroupWriteModel.setName("Test 1");

        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/taskGroups/create/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel))
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskGroupReadModel taskGroupReadModel = objectMapper.readValue(result1.getResponse().getContentAsString(), TaskGroupReadModel.class);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/projects/delete/" + projectReadModel.getId())
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200));


        Assertions.assertEquals(0, projectRepository.findAll().size());
        Assertions.assertEquals(0, taskRepository.findAll().size());
        Assertions.assertEquals(0, taskGroupRepository.findAll().size());
        Assertions.assertEquals(0, projectUserRepository.findAll().size());
    }

    @Test
    void shouldReturnProjectWithGivenId() throws Exception {
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_READ, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_CREATE));

        ProjectReadModel fakeProject = createFakeProject(firstAuthToken);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/projects/" + fakeProject.getId())
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        Assertions.assertEquals(1, projectReadModel.getTaskGroups().size());
        Assertions.assertEquals(2, projectReadModel.getTaskGroups().stream().findFirst().get().getTasks().size());
    }

    @Test
    void shouldNotReturnProjectWithGivenIdWhenUserHasNoPermission() throws Exception {
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, TASK_GROUP_CREATE, TASK_CREATE));

        ProjectReadModel fakeProject = createFakeProject(firstAuthToken);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/projects/" + fakeProject.getId())
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(403)).andReturn();
    }

    @Test
    void shouldNotReturnProjectWithGivenIdWhenUserIsFromAnotherOrganization() throws Exception {
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_READ, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_CREATE));
        String secondAuthToken = authUtil.getUserWithRole("test2@wp.pl", List.of(PROJECT_READ, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_CREATE));

        ProjectReadModel fakeProject = createFakeProject(firstAuthToken);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/projects/" + fakeProject.getId())
                        .header("Authorization", "Bearer " + secondAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(403)).andReturn();
    }

    @Test
    void shouldAddUserToProject() throws Exception {
        //given
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, PROJECT_ADD_USER, ORGANIZATION_ADD_USER));

        addUserToOrganization(firstAuthToken, "test2@wp.pl");

        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + firstAuthToken)).andReturn();


        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        Assertions.assertEquals(1, projectUserRepository.findAll().size());

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + projectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "INTERNAL")
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        //then
        Assertions.assertEquals(1, projectRepository.findAllByUserId(userRepository.findByEmail("test2@wp.pl").get().getId()).stream().count());
    }

    @Test
    void shouldNotAddUserToProjectWhenUserHasNoPermission() throws Exception {
        //given
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, ORGANIZATION_ADD_USER));

        addUserToOrganization(firstAuthToken, "test2@wp.pl");

        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + firstAuthToken)).andReturn();


        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        Assertions.assertEquals(1, projectUserRepository.findAll().size());

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + projectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "INTERNAL")
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(403)).andReturn();

        //then
        Assertions.assertEquals(0, projectRepository.findAllByUserId(userRepository.findByEmail("test2@wp.pl").get().getId()).stream().count());
    }

    @Test
    void shouldNotAddUserToProjectWhenUserIsFromAnotherOrganization() throws Exception {
        //given
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, ORGANIZATION_ADD_USER));
        authUtil.getUserWithRole("test2@wp.pl", List.of(PROJECT_CREATE, ORGANIZATION_ADD_USER));

        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + firstAuthToken)).andReturn();


        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        Assertions.assertEquals(1, projectUserRepository.findAll().size());

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + projectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "INTERNAL")
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(403)).andReturn();

        //then
        Assertions.assertEquals(0, projectRepository.findAllByUserId(userRepository.findByEmail("test2@wp.pl").get().getId()).stream().count());
    }

    @Test
    void shouldNotAddUserToProjectWhenProjectIsFromAnotherOrganization() throws Exception {
        //given
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, ORGANIZATION_ADD_USER));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(PROJECT_CREATE, ORGANIZATION_ADD_USER));

        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + secondToken)).andReturn();


        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        Assertions.assertEquals(1, projectUserRepository.findAll().size());

        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + projectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "INTERNAL")
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(403)).andReturn();
    }

    @Test
    void shouldDeleteUserFromProject() throws Exception {
        //given
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, ORGANIZATION_ADD_USER, PROJECT_ADD_USER, PROJECT_REMOVE_USER));

        addUserToOrganization(firstAuthToken, "test2@wp.pl");

        ProjectReadModel projectReadModel = createProject(firstAuthToken);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + projectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "INTERNAL")
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        Assertions.assertEquals(1, projectRepository.findAllByUserId(userRepository.findByEmail("test2@wp.pl").get().getId()).stream().count());

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/projects/deleteUser/" + projectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        //then
        Assertions.assertEquals(0, projectRepository.findAllByUserId(userRepository.findByEmail("test2@wp.pl").get().getId()).stream().count());
    }

    @Test
    void shouldNotDeleteUserFromProjectWhenUserHasNoPermission() throws Exception {
        //given
        String firstAuthToken = authUtil.getUserWithRole("test1@wp.pl", List.of(PROJECT_CREATE, ORGANIZATION_ADD_USER, PROJECT_ADD_USER));

        addUserToOrganization(firstAuthToken, "test2@wp.pl");

        ProjectReadModel projectReadModel = createProject(firstAuthToken);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + projectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "INTERNAL")
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        Assertions.assertEquals(1, projectRepository.findAllByUserId(userRepository.findByEmail("test2@wp.pl").get().getId()).stream().count());

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/projects/deleteUser/" + projectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(403)).andReturn();

        //then
        Assertions.assertEquals(1, projectRepository.findAllByUserId(userRepository.findByEmail("test2@wp.pl").get().getId()).stream().count());
    }

    @Test
    void shouldReturnCorrectOverviewOfProject() throws Exception {
        //given
        String firstAuthToken = authUtil.getAuthToken("test1@wp.pl");

        ProjectReadModel fakeProject = createFakeProject(firstAuthToken);

        //then
        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/projects/overview/" + fakeProject.getId())
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        ProjectMetaData projectMetaData = objectMapper.readValue(authorization.getResponse().getContentAsString(), ProjectMetaData.class);

        //then
        Assertions.assertEquals(2, projectMetaData.getNumberOfAllTasks());
        Assertions.assertEquals(1, projectMetaData.getNumberOfCompletedTasks());
        Assertions.assertEquals(0, projectMetaData.getTaskByPriority().getNumberOfTasksWithLowPriority());
        Assertions.assertEquals(0, projectMetaData.getTaskByPriority().getNumberOfTasksWithNormalPriority());
        Assertions.assertEquals(1, projectMetaData.getTaskByPriority().getNumberOfTasksWithHighPriority());
        Assertions.assertEquals(1, projectMetaData.getTaskByPriority().getNumberOfTasksWithUrgentPriority());
    }


    void createFakeProjects() throws Exception {
        String firstAuthToken = authUtil.getUserWithRole("test101@wp.pl", List.of(PROJECT_CREATE));
        String secondAuthToken = authUtil.getUserWithRole("test102@wp.pl", List.of(PROJECT_CREATE));

        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 100");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 100");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        ProjectWriteModel projectWriteModel2 = new ProjectWriteModel();
        projectWriteModel2.setName("Test 102");
        projectWriteModel2.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel2.setDescription("Test 102");
        projectWriteModel2.setEndTime(LocalDate.now());
        projectWriteModel2.setStartTime(LocalDate.now());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + firstAuthToken));

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .content(objectMapper.writeValueAsString(projectWriteModel2))
                .header("Authorization", "Bearer " + firstAuthToken));

        ProjectWriteModel projectWriteModel3 = new ProjectWriteModel();
        projectWriteModel3.setName("Test 103");
        projectWriteModel3.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel3.setDescription("Test 103");
        projectWriteModel3.setEndTime(LocalDate.now());
        projectWriteModel3.setStartTime(LocalDate.now());

        ProjectWriteModel projectWriteModel4 = new ProjectWriteModel();
        projectWriteModel4.setName("Test 104");
        projectWriteModel4.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel4.setDescription("Test 104");
        projectWriteModel4.setEndTime(LocalDate.now());
        projectWriteModel4.setStartTime(LocalDate.now());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .content(objectMapper.writeValueAsString(projectWriteModel3))
                .header("Authorization", "Bearer " + secondAuthToken));

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .content(objectMapper.writeValueAsString(projectWriteModel4))
                .header("Authorization", "Bearer " + secondAuthToken));
    }

    private ProjectReadModel createFakeProject(String auth) throws Exception {
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + auth)).andReturn();
        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel = new TaskGroupWriteModel();
        taskGroupWriteModel.setName("Test 1");

        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/taskGroups/create/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel))
                        .header("Authorization", "Bearer " + auth))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskGroupReadModel taskGroupReadModel = objectMapper.readValue(result1.getResponse().getContentAsString(), TaskGroupReadModel.class);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setPriority(TaskPriority.HIGH);
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + auth))
                .andExpect(MockMvcResultMatchers.status().is(200));

        taskWriteModel.setName("Task 2");
        taskWriteModel.setPriority(TaskPriority.URGENT);
        taskWriteModel.setStatus(TaskStatus.DONE);
        taskWriteModel.setName("Task 2");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + auth))
                .andExpect(MockMvcResultMatchers.status().is(200));
        return projectReadModel;
    }

    private void addUserToOrganization(String token, String email) throws Exception {
        UserWriteModel userWriteModel = new UserWriteModel();
        userWriteModel.setFirstName("Test1");
        userWriteModel.setLastName("Test2");
        userWriteModel.setEmail(email);
        userWriteModel.setRoleId(1);

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/organizations/users")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(userWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));
    }

    private ProjectReadModel createProject(String token) throws Exception {
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 1");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 1");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + token)).andReturn();


        return objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);
    }
}
