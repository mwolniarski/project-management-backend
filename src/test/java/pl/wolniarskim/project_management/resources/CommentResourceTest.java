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
import pl.wolniarskim.project_management.models.Comment;
import pl.wolniarskim.project_management.models.DTO.*;
import pl.wolniarskim.project_management.models.Notification;
import pl.wolniarskim.project_management.models.ProjectStatus;
import pl.wolniarskim.project_management.repositories.CommentRepository;
import pl.wolniarskim.project_management.repositories.NotificationRepository;
import pl.wolniarskim.project_management.repositories.ProjectRepository;
import pl.wolniarskim.project_management.util.AuthUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.*;
import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.TASK_ADD_COMMENT;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class CommentResourceTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    ProjectRepository projectRepository;

    @Test
    void shouldReturn404WhenIsNotAuthorize() throws Exception {
        // when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments")
                        .content(objectMapper.writeValueAsString(new CommentReadModel())))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    void shouldCreateCommentForGivenTask() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT));
        TaskReadModel task = createTask(authToken);

        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(task.getId());
        commentWriteModel.setComment("test1");

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        Assertions.assertEquals(1, commentRepository.count());
        Comment comment = commentRepository.findAll().get(0);
        Assertions.assertEquals(task.getId(), comment.getTask().getId());
        Assertions.assertEquals("test1", comment.getContent());
    }

    @Test
    void shouldNotCreateCommentForGivenTaskIfUserHasNoPermission() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT));
        TaskReadModel task = createTask(authToken);

        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(task.getId());
        commentWriteModel.setComment("test1");

        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    void shouldNotCreateCommentForGivenTaskIfUserIsPartOfAnotherOrganization() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE));
        TaskReadModel task = createTask(authToken);

        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(task.getId());
        commentWriteModel.setComment("test1");
        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    void shouldUpdateComment() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT, TASK_EDIT_COMMENT));
        TaskReadModel task = createTask(authToken);

        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(task.getId());
        commentWriteModel.setComment("test1");

        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        CommentReadModel commentReadModel = objectMapper.readValue(authorization.getResponse().getContentAsString(), CommentReadModel.class);

        CommentWriteModel updateModel = new CommentWriteModel();
        updateModel.setComment("test2");

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/comments/update/" + commentReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));
        //then
        Comment comment = commentRepository.findAll().get(0);
        Assertions.assertEquals(task.getId(), comment.getTask().getId());
        Assertions.assertEquals("test2", comment.getContent());
    }

    @Test
    void shouldNotUpdateCommentIfUserHasNoPermission() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT));
        TaskReadModel task = createTask(authToken);

        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(task.getId());
        commentWriteModel.setComment("test1");

        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        CommentReadModel commentReadModel = objectMapper.readValue(authorization.getResponse().getContentAsString(), CommentReadModel.class);

        CommentWriteModel updateModel = new CommentWriteModel();
        updateModel.setComment("test2");

        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/comments/update/" + commentReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    void shouldNotUpdateCommentIfUserIsPartOfAnotherOrganization() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT, TASK_EDIT_COMMENT));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT, TASK_EDIT_COMMENT));
        TaskReadModel task = createTask(authToken);

        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(task.getId());
        commentWriteModel.setComment("test1");

        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        CommentReadModel commentReadModel = objectMapper.readValue(authorization.getResponse().getContentAsString(), CommentReadModel.class);

        CommentWriteModel updateModel = new CommentWriteModel();
        updateModel.setComment("test2");

        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/comments/update/" + commentReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateModel))
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    void shouldDeleteComment() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT, TASK_EDIT_COMMENT, TASK_DELETE_COMMENT));
        TaskReadModel task = createTask(authToken);

        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(task.getId());
        commentWriteModel.setComment("test1");

        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        CommentReadModel commentReadModel = objectMapper.readValue(authorization.getResponse().getContentAsString(), CommentReadModel.class);
        Assertions.assertEquals(1, commentRepository.count());
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/comments/delete/" + commentReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));
        //then
        Assertions.assertEquals(0, commentRepository.count());
    }

    @Test
    void shouldNotDeleteCommentIfUserHasNoPermission() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT, TASK_EDIT_COMMENT));
        TaskReadModel task = createTask(authToken);

        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(task.getId());
        commentWriteModel.setComment("test1");

        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        CommentReadModel commentReadModel = objectMapper.readValue(authorization.getResponse().getContentAsString(), CommentReadModel.class);
        Assertions.assertEquals(1, commentRepository.count());
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/comments/delete/" + commentReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403));
        //then
        Assertions.assertEquals(1, commentRepository.count());
    }

    @Test
    void shouldNotDeleteCommentIfUserIsPartOfAnotherOrganization() throws Exception {
        // given
        String authToken = authUtil.getUserWithRole("test1@wp.pl", List.of(ORGANIZATION_ADD_USER, TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT, TASK_EDIT_COMMENT, TASK_DELETE_COMMENT));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(TASK_CREATE, PROJECT_CREATE, TASK_GROUP_CREATE, TASK_ADD_COMMENT, TASK_EDIT_COMMENT, TASK_DELETE_COMMENT));
        TaskReadModel task = createTask(authToken);

        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(task.getId());
        commentWriteModel.setComment("test1");

        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        CommentReadModel commentReadModel = objectMapper.readValue(authorization.getResponse().getContentAsString(), CommentReadModel.class);
        Assertions.assertEquals(1, commentRepository.count());
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/comments/delete/" + commentReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(MockMvcResultMatchers.status().is(403));
        //then
        Assertions.assertEquals(1, commentRepository.count());
    }

    @Test
    void shouldCreateNotificationForOtherUserInProjectWhenUserIsMentionedInComment() throws Exception {
        // given
        String authToken = authUtil.getAuthToken("test1@wp.pl");
        TaskReadModel task = createTask(authToken);

        UserWriteModel userWriteModel = new UserWriteModel();
        userWriteModel.setFirstName("Test1");
        userWriteModel.setLastName("Test2");
        userWriteModel.setEmail("test2@wp.pl");
        userWriteModel.setRoleId(1);

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/organizations/users")
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(userWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/projects/addUser/" + projectRepository.findAll().get(0).getId())
                        .param("email", "test2@wp.pl")
                        .param("role", "INTERNAL")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(task.getId());
        commentWriteModel.setComment("@@@@test2@wp.pl@@@@");

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        Notification notification = notificationRepository.findAll().stream().filter(n -> n.getNotificationContent().equals("You got mark in task comment -> 1")).collect(Collectors.toList()).get(0);
        Assertions.assertEquals("test2@wp.pl", notification.getRelatedTo().getEmail());
    }

    @Test
    void shouldNotCreateNotificationForOtherUserInProjectIfUserIsNotPartOfProjectWhenUserIsMentionedInComment() throws Exception {
        // given
        String authToken = authUtil.getAuthToken("test1@wp.pl");
        authUtil.getAuthToken("test2@wp.pl");
        TaskReadModel task = createTask(authToken);

        createTask(authToken);
        CommentWriteModel commentWriteModel = new CommentWriteModel();
        commentWriteModel.setTaskId(task.getId());
        commentWriteModel.setComment("@@@@test2@wp.pl@@@@");

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteModel))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andReturn();

        //then
        List<Notification> collect = notificationRepository.findAll().stream().filter(n -> n.getNotificationContent().equals("You got mark in task comment -> 1")).collect(Collectors.toList());
        Assertions.assertEquals(0, collect.size());
    }

    private TaskReadModel createTask(String authToken) throws Exception {
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

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tasks/create/" + taskGroupReadModel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskWriteModel()))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(MockMvcResultMatchers.status().is(200)).andReturn();
        return objectMapper.readValue(result2.getResponse().getContentAsString(), TaskReadModel.class);
    }
}