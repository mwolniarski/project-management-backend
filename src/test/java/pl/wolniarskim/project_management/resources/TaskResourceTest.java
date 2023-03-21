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
import pl.wolniarskim.project_management.models.ProjectStatus;
import pl.wolniarskim.project_management.models.TaskStatus;
import pl.wolniarskim.project_management.repositories.TaskRepository;
import pl.wolniarskim.project_management.util.AuthUtil;

import java.time.LocalDate;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskResourceTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    TaskRepository taskRepository;

    @Test
    void shouldReturn404WhenIsNotAuthorize() throws Exception {
        // when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tasks"))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }
    @Test
    void shouldCreateTaskForGivenTaskGroup() throws Exception {
        //given
        String authToken = authUtil.getAuthToken("test1@wp.pl");
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

        TaskGroupReadModel taskGroupReadModel = objectMapper.readValue(result1.getResponse().getContentAsString(), TaskGroupReadModel.class);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        Assertions.assertEquals("Task 1", taskRepository.findAll().get(0).getName());
    }

    @Test
    void shouldNotCreateTaskForGivenTaskGroupWhenUserHasNotAdminRole() throws Exception {
        //given
        String authToken = authUtil.getAuthToken("test1@wp.pl");
        String secondToken = authUtil.getAuthToken("test10@wp.pl");
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

        TaskGroupReadModel taskGroupReadModel = objectMapper.readValue(result1.getResponse().getContentAsString(), TaskGroupReadModel.class);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals(0, taskRepository.findAll().size());
    }

    @Test
    void shouldDeleteTask() throws Exception {
        //given
        String authToken = authUtil.getAuthToken("test1@wp.pl");
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

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskReadModel taskReadModel =
                objectMapper.readValue(result2.getResponse().getContentAsString(), TaskReadModel.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/tasks/delete/" + taskReadModel.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        Assertions.assertEquals(0, taskRepository.findAll().size());
    }

    @Test
    void shouldNotDeleteTaskWhenUserHasNotAdminRole() throws Exception {
        //given
        String authToken = authUtil.getAuthToken("test1@wp.pl");
        String secondToken = authUtil.getAuthToken("test10@wp.pl");
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

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskReadModel taskReadModel =
                objectMapper.readValue(result2.getResponse().getContentAsString(), TaskReadModel.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/tasks/delete/" + taskReadModel.getId())
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals(1, taskRepository.findAll().size());
    }

    @Test
    void shouldUpdateTask() throws Exception {
        //given
        String authToken = authUtil.getAuthToken("test1@wp.pl");
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

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskReadModel taskReadModel =
                objectMapper.readValue(result2.getResponse().getContentAsString(), TaskReadModel.class);

        TaskWriteModel taskWriteModel1 = new TaskWriteModel();
        taskWriteModel1.setName("Task 2");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/tasks/update/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel1))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        Assertions.assertEquals("Task 2", taskRepository.findAll().get(0).getName());
    }

    @Test
    void shouldNotUpdateTaskWhenUserHasNotAdminRole() throws Exception {
        //given
        String authToken = authUtil.getAuthToken("test1@wp.pl");
        String secondToken = authUtil.getAuthToken("test10@wp.pl");
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

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskReadModel taskReadModel =
                objectMapper.readValue(result2.getResponse().getContentAsString(), TaskReadModel.class);

        TaskWriteModel taskWriteModel1 = new TaskWriteModel();
        taskWriteModel1.setName("Task 2");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/tasks/update/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel1))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals("Task 1", taskRepository.findAll().get(0).getName());
    }
}
