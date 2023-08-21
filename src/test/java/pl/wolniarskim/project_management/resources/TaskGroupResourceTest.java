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
import pl.wolniarskim.project_management.models.DTO.ProjectReadModel;
import pl.wolniarskim.project_management.models.DTO.ProjectWriteModel;
import pl.wolniarskim.project_management.models.DTO.TaskGroupReadModel;
import pl.wolniarskim.project_management.models.DTO.TaskGroupWriteModel;
import pl.wolniarskim.project_management.models.Permission;
import pl.wolniarskim.project_management.models.ProjectStatus;
import pl.wolniarskim.project_management.repositories.TaskGroupRepository;
import pl.wolniarskim.project_management.util.AuthUtil;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class TaskGroupResourceTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    TaskGroupRepository taskGroupRepository;

    @Test
    void shouldReturn404WhenIsNotAuthorize() throws Exception {
        // when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/taskGroups"))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }
    @Test
    void shouldCreateTaskGroupForGivenProject() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(TASK_GROUP_CREATE, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 2");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 2");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();
        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel = new TaskGroupWriteModel();
        taskGroupWriteModel.setName("Test 1");

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/taskGroups/create/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));
        //then
        Assertions.assertEquals("Test 1", taskGroupRepository.findAll().get(0).getName());
    }

    @Test
    void shouldNotCreateTaskGroupForGivenProjectWhenUserHasNoCreatePermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_GROUP_UPDATE, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 2");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 2");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();
        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel = new TaskGroupWriteModel();
        taskGroupWriteModel.setName("Test 1");
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/taskGroups/create/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403));
        //then
        Assertions.assertEquals(0, taskGroupRepository.findAll().size());
    }

    @Test
    void shouldNotCreateTaskGroupForGivenProjectWhenUserIsFromAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getAuthToken("test1@wp.pl");
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_GROUP_CREATE, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 2");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 2");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();
        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel = new TaskGroupWriteModel();
        taskGroupWriteModel.setName("Test 1");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/taskGroups/create/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals(0, taskGroupRepository.findAll().size());
    }

    @Test
    void shouldDeleteTaskGroup() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_GROUP_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 2");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 2");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();
        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel = new TaskGroupWriteModel();
        taskGroupWriteModel.setName("Test 1");

        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/taskGroups/create/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskGroupReadModel taskGroupReadModel =
                objectMapper.readValue(result1.getResponse().getContentAsString(), TaskGroupReadModel.class);
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/taskGroups/delete/" + taskGroupReadModel.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));
        //then
        Assertions.assertEquals(0, taskGroupRepository.findAll().size());
    }

    @Test
    void shouldNotDeleteTaskGroupWhenUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_GROUP_CREATE, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 2");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 2");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();
        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel = new TaskGroupWriteModel();
        taskGroupWriteModel.setName("Test 1");

        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/taskGroups/create/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskGroupReadModel taskGroupReadModel =
                objectMapper.readValue(result1.getResponse().getContentAsString(), TaskGroupReadModel.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/taskGroups/delete/" + taskGroupReadModel.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals(1, taskGroupRepository.findAll().size());
    }

    @Test
    void shouldNotDeleteTaskGroupIfUserIsFromAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_GROUP_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_GROUP_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 2");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 2");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();
        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel = new TaskGroupWriteModel();
        taskGroupWriteModel.setName("Test 1");

        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/taskGroups/create/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskGroupReadModel taskGroupReadModel =
                objectMapper.readValue(result1.getResponse().getContentAsString(), TaskGroupReadModel.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/taskGroups/delete/" + taskGroupReadModel.getId())
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals(1, taskGroupRepository.findAll().size());
    }

    @Test
    void shouldUpdateTaskGroup() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_GROUP_CREATE, TASK_GROUP_UPDATE, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 2");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 2");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();
        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel = new TaskGroupWriteModel();
        taskGroupWriteModel.setName("Test 1");

        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/taskGroups/create/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskGroupReadModel taskGroupReadModel =
                objectMapper.readValue(result1.getResponse().getContentAsString(), TaskGroupReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel2 = new TaskGroupWriteModel();
        taskGroupWriteModel2.setName("Test 2");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/taskGroups/update/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel2))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        Assertions.assertEquals("Test 2", taskGroupRepository.findAll().get(0).getName());
    }

    @Test
    void shouldNotUpdateTaskGroupWhenUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_GROUP_CREATE, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 2");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 2");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();
        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel = new TaskGroupWriteModel();
        taskGroupWriteModel.setName("Test 1");

        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/taskGroups/create/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskGroupReadModel taskGroupReadModel =
                objectMapper.readValue(result1.getResponse().getContentAsString(), TaskGroupReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel2 = new TaskGroupWriteModel();
        taskGroupWriteModel2.setName("Test 2");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/taskGroups/update/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel2))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals("Test 1", taskGroupRepository.findAll().get(0).getName());
    }

    @Test
    void shouldNotUpdateTaskGroupWhenUserIsFromAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_GROUP_CREATE, TASK_GROUP_UPDATE, PROJECT_CREATE));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_GROUP_CREATE, TASK_GROUP_UPDATE, PROJECT_CREATE));
        ProjectWriteModel projectWriteModel = new ProjectWriteModel();
        projectWriteModel.setName("Test 2");
        projectWriteModel.setStatus(ProjectStatus.ACTIVE);
        projectWriteModel.setDescription("Test 2");
        projectWriteModel.setEndTime(LocalDate.now());
        projectWriteModel.setStartTime(LocalDate.now());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectWriteModel))
                .header("Authorization", "Bearer " + authToken)).andReturn();
        ProjectReadModel projectReadModel = objectMapper.readValue(result.getResponse().getContentAsString(), ProjectReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel = new TaskGroupWriteModel();
        taskGroupWriteModel.setName("Test 1");

        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/taskGroups/create/" + projectReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskGroupReadModel taskGroupReadModel =
                objectMapper.readValue(result1.getResponse().getContentAsString(), TaskGroupReadModel.class);

        TaskGroupWriteModel taskGroupWriteModel2 = new TaskGroupWriteModel();
        taskGroupWriteModel2.setName("Test 2");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/taskGroups/update/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskGroupWriteModel2))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals("Test 1", taskGroupRepository.findAll().get(0).getName());
    }
}
