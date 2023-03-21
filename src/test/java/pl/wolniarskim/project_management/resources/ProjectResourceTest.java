package pl.wolniarskim.project_management.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pl.wolniarskim.project_management.models.DTO.*;
import pl.wolniarskim.project_management.models.Project;
import pl.wolniarskim.project_management.models.ProjectStatus;
import pl.wolniarskim.project_management.models.TaskStatus;
import pl.wolniarskim.project_management.repositories.*;
import pl.wolniarskim.project_management.services.ProjectService;
import pl.wolniarskim.project_management.util.AuthUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
        String authToken = authUtil.getAuthToken("test1@wp.pl");
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
    void shouldUpdateGivenProject() throws Exception {
        // given
        String authToken = authUtil.getAuthToken("test1@wp.pl");
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
    void shouldReturn403WhenUserHaveNoPermissionToEdit() throws Exception {
        // given
        String firstAuthToken = authUtil.getAuthToken("test1@wp.pl");
        String secondAuthToken = authUtil.getAuthToken("test2@wp.pl");
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

        Assertions.assertEquals(1, projectRepository.findAll().size());

        SimpleProjectReadModel simpleProjectReadModel =
                objectMapper.readValue(result.getResponse().getContentAsString(), SimpleProjectReadModel.class);

        MvcResult r = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + simpleProjectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "USER")
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andReturn();

        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        projectWriteModel.setName("Test 120");

        // when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/projects/update/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectWriteModel))
                        .header("Authorization", "Bearer " + secondAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(403)).andReturn();

        Assertions.assertEquals(1, projectRepository.findAll().size());
    }

    @Test
    void shouldCreateProjectForSpecificUser() throws Exception {
        // given
        String firstAuthToken = authUtil.getAuthToken("test1@wp.pl");
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

        Assertions.assertEquals(1, projectRepository.findAll().size());
    }

    @Test
    void shouldDeleteProjectWhenUserHasAdminRole() throws Exception {
        //given
        String firstAuthToken = authUtil.getAuthToken("test101@wp.pl");
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
        Assertions.assertEquals(1, projectRepository.findAll().size());
        //when
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/projects/delete/"+project.getId())
                .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        //todo sprawdzic czy wszystkie grupy zadan, zadania i powiazania z uzytkownikami sa usuniete
        Assertions.assertEquals(0, projectRepository.findAll().size());
    }

    @Test
    void shouldNotDeleteProjectWhenUserHasUserRole() throws Exception {
        // given
        String firstAuthToken = authUtil.getAuthToken("test1@wp.pl");
        String secondAuthToken = authUtil.getAuthToken("test2@wp.pl");
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

        Assertions.assertEquals(1, projectRepository.findAll().size());

        SimpleProjectReadModel simpleProjectReadModel =
                objectMapper.readValue(result.getResponse().getContentAsString(), SimpleProjectReadModel.class);


        MvcResult r = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + simpleProjectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "USER")
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andReturn();

        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        projectWriteModel.setName("Test 120");

        // when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/projects/delete/" + projectReadModel.getId())
                        .header("Authorization", "Bearer " + secondAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(403)).andReturn();

        Assertions.assertEquals(1, projectRepository.findAll().size());
    }

    @Test
    void shouldDeleteProjectWithAllAssociatedUsersAndTaskGroupsAndTasks() throws Exception {
        String firstAuthToken = authUtil.getAuthToken("test1@wp.pl");
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
    void shouldAddUserToProjectWhenUserHasAdminRole() throws Exception {
        // given
        String firstAuthToken = authUtil.getAuthToken("test1@wp.pl");
        String secondAuthToken = authUtil.getAuthToken("test2@wp.pl");
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

        Assertions.assertEquals(1, projectUserRepository.findAll().size());

        SimpleProjectReadModel simpleProjectReadModel =
                objectMapper.readValue(result.getResponse().getContentAsString(), SimpleProjectReadModel.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + simpleProjectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "USER")
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();


        Assertions.assertEquals(2, projectUserRepository.findAll().size());
    }

    @Test
    void shouldNotAddUserToProjectWhenUserHasUserRole() throws Exception {
        // given
        String firstAuthToken = authUtil.getAuthToken("test1@wp.pl");
        String secondAuthToken = authUtil.getAuthToken("test2@wp.pl");
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

        Assertions.assertEquals(1, projectUserRepository.findAll().size());

        SimpleProjectReadModel simpleProjectReadModel =
                objectMapper.readValue(result.getResponse().getContentAsString(), SimpleProjectReadModel.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + simpleProjectReadModel.getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "USER")
                        .header("Authorization", "Bearer " + secondAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andReturn();


        Assertions.assertEquals(1, projectUserRepository.findAll().size());
    }

    @Test
    void shouldReturnProjectWithGivenId() throws Exception {
        String firstAuthToken = authUtil.getAuthToken("test1@wp.pl");
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

        taskWriteModel.setName("Task 2");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/projects/" + projectReadModel.getId())
                        .header("Authorization", "Bearer " + firstAuthToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        ProjectReadModel projectReadModel1 = objectMapper.readValue(result2.getResponse().getContentAsString(), ProjectReadModel.class);

        Assertions.assertEquals(1, projectReadModel1.getTaskGroups().size());
        Assertions.assertEquals(2, new ArrayList<>(projectReadModel1.getTaskGroups()).get(0).getTasks().size());
    }

    void createFakeProjects() throws Exception {
        String firstAuthToken = authUtil.getAuthToken("test101@wp.pl");
        String secondAuthToken = authUtil.getAuthToken("test102@wp.pl");

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
}
