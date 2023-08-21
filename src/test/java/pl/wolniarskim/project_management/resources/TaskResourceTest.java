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
import pl.wolniarskim.project_management.services.NotificationService;
import pl.wolniarskim.project_management.util.AuthUtil;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class TaskResourceTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    TaskTimeEntryRepository taskTimeEntryRepository;
    @Autowired
    ResetPasswordTokenRepository resetPasswordTokenRepository;
    @Autowired
    NotificationService notificationService;

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
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

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
    void shouldNotCreateTaskForGivenTaskGroupWhenUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_UPDATE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals(0, taskRepository.findAll().size());
    }

    @Test
    void shouldNotCreateTaskForGivenTaskGroupWhenUserIsFromAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_GROUP_CREATE, PROJECT_CREATE));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_CREATE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

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
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

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
    void shouldNotDeleteTaskWhenUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

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
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals(1, taskRepository.findAll().size());
    }

    @Test
    void shouldNotDeleteTaskWhenUserIsFromAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_CREATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

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
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        //when
        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskReadModel taskReadModel =
                objectMapper.readValue(result2.getResponse().getContentAsString(), TaskReadModel.class);

        User byEmail = userRepository.findByEmail("test1@wp.pl").get();
        OwnerWriteModel ownerWriteModel = new OwnerWriteModel();
        ownerWriteModel.setId(byEmail.getId());
        TaskWriteModel taskWriteModel1 = new TaskWriteModel();
        taskWriteModel1.setName("Task 2");
        taskWriteModel1.setDescription("Description");
        taskWriteModel1.setStatus(TaskStatus.TO_DO);
        taskWriteModel1.setOwner(ownerWriteModel);
        taskWriteModel1.setPriority(TaskPriority.HIGH);
        taskWriteModel1.setDueDate(LocalDate.of(1999, 2,2));
        taskWriteModel1.setEstimatedWorkTime(3.5);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/tasks/update/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel1))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        Task task = taskRepository.findAll().get(0);
        Assertions.assertEquals("Task 2", task.getName());
        Assertions.assertEquals("Description", task.getDescription());
        Assertions.assertEquals(TaskStatus.TO_DO, task.getStatus());
        Assertions.assertEquals("test1@wp.pl", task.getTaskOwner().getEmail());
        Assertions.assertEquals(TaskPriority.HIGH, task.getPriority());
        Assertions.assertEquals(LocalDate.of(1999, 2,2), task.getDueDate());
        Assertions.assertEquals(3.5, task.getEstimatedWorkTime());
    }

    @Test
    void shouldNotUpdateTaskWhenUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

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

        //when
        TaskWriteModel taskWriteModel1 = new TaskWriteModel();
        taskWriteModel1.setName("Task 2");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/tasks/update/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel1))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403));
        //then
        Assertions.assertEquals("Task 1", taskRepository.findAll().get(0).getName());
    }

    @Test
    void shouldNotUpdateTaskWhenUserIsFromAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

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

        //when
        TaskWriteModel taskWriteModel1 = new TaskWriteModel();
        taskWriteModel1.setName("Task 2");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/tasks/update/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel1))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));
        //then
        Assertions.assertEquals("Task 1", taskRepository.findAll().get(0).getName());
    }

    @Test
    void shouldReturnCommentsForGivenTask() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(authorization.getResponse().getContentAsString(), TaskReadModel.class);

        MvcResult authorization2 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel2 = objectMapper.readValue(authorization2.getResponse().getContentAsString(), TaskReadModel.class);

        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(taskReadModel.getId());
        commentWriteModel.setComment("test1");

        CommentWriteModel commentWriteModel2 = new CommentWriteModel();
        commentWriteModel2.setTaskId(taskReadModel.getId());
        commentWriteModel2.setComment("test2");

        CommentWriteModel commentWriteModel3 = new CommentWriteModel();
        commentWriteModel3.setTaskId(taskReadModel2.getId());
        commentWriteModel3.setComment("test3");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel2))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel3))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //when
        MvcResult allComments = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tasks/comments/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        CommentReadModel[] commentReadModels = objectMapper.readValue(allComments.getResponse().getContentAsString(), CommentReadModel[].class);

        //then
        Assertions.assertEquals(2, commentReadModels.length);
        Assertions.assertEquals("test1", commentReadModels[0].getContent());
        Assertions.assertEquals("test2", commentReadModels[1].getContent());
    }

    @Test
    void shouldNotReturnCommentsForGivenTaskWhenUserIsFromAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tasks/comments/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    void shouldReturnTaskHistoryForGivenTasks() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TASK_HISTORY_READ));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        //when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tasks/history/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        TaskHistoryReadModel[] taskHistories = objectMapper.readValue(result.getResponse().getContentAsString(), TaskHistoryReadModel[].class);
        //then
        Assertions.assertEquals(1, taskHistories.length);
    }

    @Test
    void shouldNotReturnTaskHistoryForGivenTasksIfUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tasks/history/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andReturn();
    }

    @Test
    void shouldNotReturnTaskHistoryForGivenTasksIfUserIsFromAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TASK_HISTORY_READ));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TASK_HISTORY_READ));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tasks/history/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andReturn();
    }

    @Test
    void shouldAddTaskHistoryEntryWhenTaskIsUpdated() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TASK_HISTORY_READ));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description2");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/tasks/update/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tasks/history/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        TaskHistoryReadModel[] taskHistories = objectMapper.readValue(result.getResponse().getContentAsString(), TaskHistoryReadModel[].class);
        //then
        Assertions.assertEquals(2, taskHistories.length);
    }

    @Test
    void shouldAddTimeEntryWithGivenData() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TIME_ENTRY_ADD));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        TaskTimeEntryWriteModel taskTimeEntryWriteModel = new TaskTimeEntryWriteModel();
        taskTimeEntryWriteModel.setDescription("Description");
        taskTimeEntryWriteModel.setHoursSpent(3.5);
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        //then
        Assertions.assertEquals(1, taskTimeEntryRepository.count());
        TaskTimeEntry taskTimeEntry = taskTimeEntryRepository.findAll().get(0);
        Assertions.assertEquals(taskReadModel.getId(), taskTimeEntry.getTask().getId());
        Assertions.assertEquals("test1@wp.pl", taskTimeEntry.getUser().getEmail());
        Assertions.assertEquals("Description", taskTimeEntry.getDescription());
        Assertions.assertEquals(3.5, taskTimeEntry.getHoursSpent());
    }

    @Test
    void shouldNotAddTimeEntryWithGivenDataWhenUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        TaskTimeEntryWriteModel taskTimeEntryWriteModel = new TaskTimeEntryWriteModel();
        taskTimeEntryWriteModel.setDescription("Description");
        taskTimeEntryWriteModel.setHoursSpent(3.5);
        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andReturn();
    }

    @Test
    void shouldNotAddTimeEntryWithGivenDataWhenUserIsPartOfAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        TaskTimeEntryWriteModel taskTimeEntryWriteModel = new TaskTimeEntryWriteModel();
        taskTimeEntryWriteModel.setDescription("Description");
        taskTimeEntryWriteModel.setHoursSpent(3.5);
        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andReturn();
    }

    @Test
    void shouldDeleteTimeEntry() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TIME_ENTRY_ADD, TIME_ENTRY_REMOVE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        TaskTimeEntryWriteModel taskTimeEntryWriteModel = new TaskTimeEntryWriteModel();
        taskTimeEntryWriteModel.setDescription("Description");
        taskTimeEntryWriteModel.setHoursSpent(3.5);
        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        TaskTimeEntryReadModel taskTimeEntryReadModel = objectMapper.readValue(authorization.getResponse().getContentAsString(), TaskTimeEntryReadModel.class);
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/tasks/timesheet/" + taskTimeEntryReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        //then
        Assertions.assertEquals(0, taskTimeEntryRepository.count());
    }

    @Test
    void shouldDeleteTimeEntryWhenUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TIME_ENTRY_ADD));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        TaskTimeEntryWriteModel taskTimeEntryWriteModel = new TaskTimeEntryWriteModel();
        taskTimeEntryWriteModel.setDescription("Description");
        taskTimeEntryWriteModel.setHoursSpent(3.5);
        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        TaskTimeEntryReadModel taskTimeEntryReadModel = objectMapper.readValue(authorization.getResponse().getContentAsString(), TaskTimeEntryReadModel.class);
        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/tasks/timesheet/" + taskTimeEntryReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andReturn();
    }

    @Test
    void shouldNotDeleteTimeEntryWhenUserIsPartOfAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TIME_ENTRY_ADD, TIME_ENTRY_REMOVE));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TIME_ENTRY_ADD, TIME_ENTRY_REMOVE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        TaskTimeEntryWriteModel taskTimeEntryWriteModel = new TaskTimeEntryWriteModel();
        taskTimeEntryWriteModel.setDescription("Description");
        taskTimeEntryWriteModel.setHoursSpent(3.5);
        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        TaskTimeEntryReadModel taskTimeEntryReadModel = objectMapper.readValue(authorization.getResponse().getContentAsString(), TaskTimeEntryReadModel.class);
        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/tasks/timesheet/" + taskTimeEntryReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andReturn();
    }

    @Test
    @Transactional
    void shouldReturnAllTimeEntryOnProject() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(ORGANIZATION_ADD_USER, TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TIME_ENTRY_ADD, TIME_ENTRY_READ_ALL));
        String secondToken = addUserToOrganization(authToken, "test2@wp.pl");
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        TaskTimeEntryWriteModel taskTimeEntryWriteModel = new TaskTimeEntryWriteModel();
        taskTimeEntryWriteModel.setDescription("Description");
        taskTimeEntryWriteModel.setHoursSpent(3.5);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        Task task = taskRepository.findById(taskReadModel.getId()).orElseThrow();
        //when
        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/projects/timesheet/" + task.getTaskGroup().getProject().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        TaskTimeEntryReadModel[] taskTimeEntryReadModels = objectMapper.readValue(authorization.getResponse().getContentAsString(), TaskTimeEntryReadModel[].class);

        //then
        Assertions.assertEquals(2, taskTimeEntryReadModels.length);
    }

    @Test
    @Transactional
    void shouldReturnOnlyLoggedUserTimeEntryOnProjectWhenUserHasNoPermissionToViewAllTimeEntryOnSpecificProject() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TIME_ENTRY_ADD, ORGANIZATION_ADD_USER));
        String secondToken = addUserToOrganization(authToken, "test2@wp.pl");
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskGroupReadModel taskGroupReadModel2 = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        TaskWriteModel taskWriteModel2 = new TaskWriteModel();
        taskWriteModel2.setName("Task 2");
        MvcResult apiCall2 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel2))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel2 = objectMapper.readValue(apiCall2.getResponse().getContentAsString(), TaskReadModel.class);

        TaskTimeEntryWriteModel taskTimeEntryWriteModel = new TaskTimeEntryWriteModel();
        taskTimeEntryWriteModel.setDescription("Description");
        taskTimeEntryWriteModel.setHoursSpent(3.5);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        Task task = taskRepository.findById(taskReadModel.getId()).orElseThrow();
        //when
        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/projects/timesheet/" + task.getTaskGroup().getProject().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        TaskTimeEntryReadModel[] taskTimeEntryReadModels = objectMapper.readValue(authorization.getResponse().getContentAsString(), TaskTimeEntryReadModel[].class);

        //then
        Assertions.assertEquals(1, taskTimeEntryReadModels.length);
    }

    @Test
    @Transactional
    void shouldNotReturnAllTimeEntryOnProjectWhenUserIsPartOfAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TIME_ENTRY_ADD));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_DELETE, TASK_GROUP_CREATE, PROJECT_CREATE, TIME_ENTRY_ADD));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setDescription("Description");
        taskWriteModel.setStatus(TaskStatus.TO_DO);

        MvcResult apiCall = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        TaskReadModel taskReadModel = objectMapper.readValue(apiCall.getResponse().getContentAsString(), TaskReadModel.class);

        TaskTimeEntryWriteModel taskTimeEntryWriteModel = new TaskTimeEntryWriteModel();
        taskTimeEntryWriteModel.setDescription("Description");
        taskTimeEntryWriteModel.setHoursSpent(3.5);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/timesheet/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskTimeEntryWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        Task task = taskRepository.findById(taskReadModel.getId()).orElseThrow();
        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tasks/timesheet/" + task.getTaskGroup().getProject().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }

    private TaskGroupReadModel createTaskGroup(String authToken) throws Exception {
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
        return objectMapper.readValue(result1.getResponse().getContentAsString(), TaskGroupReadModel.class);
    }

    private String addUserToOrganization(String token, String email) throws Exception {
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

        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findAll().get(0);
        //when

        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset/" + resetPasswordToken.getToken())
                .content("test"));

        //then
        MvcResult test = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials("test2@wp.pl", "test"))))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        return objectMapper.readValue(test.getResponse().getContentAsString(), TokensResponse.class).getAccessToken();
    }

    @Test
    void shouldAddNotificationForOwnerIfStatusOfTaskChange() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        //when
        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setStatus(TaskStatus.IN_PROGRESS);

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskReadModel taskReadModel =
                objectMapper.readValue(result2.getResponse().getContentAsString(), TaskReadModel.class);

        User byEmail = userRepository.findByEmail("test1@wp.pl").get();
        OwnerWriteModel ownerWriteModel = new OwnerWriteModel();
        ownerWriteModel.setId(byEmail.getId());
        TaskWriteModel taskWriteModel1 = new TaskWriteModel();
        taskWriteModel1.setName("Task 2");
        taskWriteModel1.setDescription("Description");
        taskWriteModel1.setStatus(TaskStatus.TO_DO);
        taskWriteModel1.setOwner(ownerWriteModel);
        taskWriteModel1.setPriority(TaskPriority.HIGH);
        taskWriteModel1.setDueDate(LocalDate.of(1999, 2,2));
        taskWriteModel1.setEstimatedWorkTime(3.5);

        List<NotificationReadModel> allNotificationsBefore = notificationService.getAllNotifications(byEmail);
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/tasks/update/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel1))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        List<NotificationReadModel> allNotificationsAfter = notificationService.getAllNotifications(byEmail);

        Assertions.assertEquals(allNotificationsBefore.size() + 1, allNotificationsAfter.size());
    }

    @Test
    void shouldNotAddNotificationForOwnerIfStatusNotChange() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, TASK_UPDATE, TASK_GROUP_CREATE, PROJECT_CREATE));
        TaskGroupReadModel taskGroupReadModel = createTaskGroup(authToken);

        //when
        TaskWriteModel taskWriteModel = new TaskWriteModel();
        taskWriteModel.setName("Task 1");
        taskWriteModel.setStatus(TaskStatus.IN_PROGRESS);

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();

        TaskReadModel taskReadModel =
                objectMapper.readValue(result2.getResponse().getContentAsString(), TaskReadModel.class);

        User byEmail = userRepository.findByEmail("test1@wp.pl").get();
        OwnerWriteModel ownerWriteModel = new OwnerWriteModel();
        ownerWriteModel.setId(byEmail.getId());
        TaskWriteModel taskWriteModel1 = new TaskWriteModel();
        taskWriteModel1.setName("Task 2");
        taskWriteModel1.setDescription("Description");
        taskWriteModel1.setOwner(ownerWriteModel);
        taskWriteModel1.setStatus(TaskStatus.IN_PROGRESS);
        taskWriteModel1.setPriority(TaskPriority.HIGH);
        taskWriteModel1.setDueDate(LocalDate.of(1999, 2,2));
        taskWriteModel1.setEstimatedWorkTime(3.5);

        List<NotificationReadModel> allNotificationsBefore = notificationService.getAllNotifications(byEmail);
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/tasks/update/" + taskReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskWriteModel1))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        List<NotificationReadModel> allNotificationsAfter = notificationService.getAllNotifications(byEmail);

        Assertions.assertEquals(allNotificationsBefore.size(), allNotificationsAfter.size());
    }
}
