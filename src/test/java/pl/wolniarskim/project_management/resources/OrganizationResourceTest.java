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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.wolniarskim.project_management.models.DTO.LoginCredentials;
import pl.wolniarskim.project_management.models.DTO.OrganizationWriteModel;
import pl.wolniarskim.project_management.models.DTO.UserWriteModel;
import pl.wolniarskim.project_management.models.Organization;
import pl.wolniarskim.project_management.models.ResetPasswordToken;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.repositories.OrganizationRepository;
import pl.wolniarskim.project_management.repositories.ResetPasswordTokenRepository;
import pl.wolniarskim.project_management.repositories.UserRepository;
import pl.wolniarskim.project_management.util.AuthUtil;

import java.util.List;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class OrganizationResourceTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Test
    void shouldReturn403IfUserIsNoLogged() throws Exception {
        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/organizations/123")
                        .content(objectMapper.writeValueAsString(new OrganizationWriteModel()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    void shouldAddUserToOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_ADD_USER));

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

        User user = userRepository.findByEmail("test2@wp.pl").get();
        //then
        Assertions.assertEquals("Test1", user.getFirstName());
        Assertions.assertEquals("Test2", user.getLastName());
        Assertions.assertEquals("test2@wp.pl", user.getEmail());
        Assertions.assertTrue(user.isEnabled());
        Assertions.assertEquals(1, user.getOrganization().getOrgId());
    }

    @Test
    void addedUserShouldBeAbleToLogInWhenHeResetThePassword() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_ADD_USER));

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

        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findAll().get(0);
        //when

        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset/" + resetPasswordToken.getToken())
                .content("test"));

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials("test2@wp.pl", "test"))))
                .andExpect(MockMvcResultMatchers.status().is(200));
    }

    @Test
    void shouldNotAddUserToOrganizationWhenUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_DELETE_USER));

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
                .andExpect(MockMvcResultMatchers.status().is(403));

        //then
        Assertions.assertTrue(userRepository.findByEmail("test2@wp.pl").isEmpty());
    }

    @Test
    void shouldDeleteUserFromOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_DELETE_USER, ORGANIZATION_ADD_USER));

        UserWriteModel userWriteModel = new UserWriteModel();
        userWriteModel.setFirstName("Test1");
        userWriteModel.setLastName("Test2");
        userWriteModel.setEmail("test2@wp.pl");
        userWriteModel.setRoleId(1);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/organizations/users")
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(userWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/organizations/users")
                        .header("Authorization", "Bearer " + authToken)
                        .param("email", "test2@wp.pl")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        Assertions.assertTrue(userRepository.findByEmail("test2@wp.pl").isEmpty());
    }

    @Test
    void deletedUserShouldNotBeAbleToLogIn() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_ADD_USER, ORGANIZATION_DELETE_USER));

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

        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findAll().get(0);


        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset/" + resetPasswordToken.getToken())
                .content("test"));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials("test2@wp.pl", "test"))))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/organizations/users")
                        .header("Authorization", "Bearer " + authToken)
                        .param("email", "test2@wp.pl")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));
        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials("test2@wp.pl", "test"))))
                .andExpect(MockMvcResultMatchers.status().is(401));
    }

    @Test
    void shouldNotDeleteUserFromOrganizationWhenUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_ADD_USER));

        UserWriteModel userWriteModel = new UserWriteModel();
        userWriteModel.setFirstName("Test1");
        userWriteModel.setLastName("Test2");
        userWriteModel.setEmail("test2@wp.pl");
        userWriteModel.setRoleId(1);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/organizations/users")
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(userWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/organizations/users")
                        .header("Authorization", "Bearer " + authToken)
                        .param("email", "test2@wp.pl")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(403));

        //then
        Assertions.assertTrue(userRepository.findByEmail("test2@wp.pl").isPresent());
    }

    @Test
    void shouldNotDeleteUserFromOrganizationWhenUserIsFromAnotherOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_ADD_USER, ORGANIZATION_DELETE_USER));
        String secondToken = authUtil.getUserWithRole("test3@wp.pl", List.of(ORGANIZATION_ADD_USER, ORGANIZATION_DELETE_USER));

        UserWriteModel userWriteModel = new UserWriteModel();
        userWriteModel.setFirstName("Test1");
        userWriteModel.setLastName("Test2");
        userWriteModel.setEmail("test2@wp.pl");
        userWriteModel.setRoleId(1);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/organizations/users")
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(userWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/organizations/users")
                        .header("Authorization", "Bearer " + secondToken)
                        .param("email", "test2@wp.pl")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(403));

        //then
        Assertions.assertTrue(userRepository.findByEmail("test2@wp.pl").isPresent());
    }

    @Test
    void shouldUpdateOrganizationName() throws Exception {
        //given
        String expected = "test123";
        OrganizationWriteModel organizationWriteModel = new OrganizationWriteModel();
        organizationWriteModel.setName(expected);

        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_UPDATE));

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/organizations/" + organizationRepository.findAll().get(0).getOrgId())
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(organizationWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        Assertions.assertEquals(1, organizationRepository.count());
        Assertions.assertEquals(expected, organizationRepository.findAll().get(0).getName());
    }

    @Test
    void shouldNotUpdateOrganizationIfUserHasNoPermission() throws Exception {
        //given
        String expected = "test";
        String toChange = "test123";
        OrganizationWriteModel organizationWriteModel = new OrganizationWriteModel();
        organizationWriteModel.setName(toChange);

        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_DELETE));

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/organizations/" + organizationRepository.findAll().get(0).getOrgId())
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(organizationWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(403));

        //then
        Assertions.assertEquals(expected, organizationRepository.findAll().get(0).getName());
    }

    @Test
    void shouldNotUpdateOrganizationIfUserIsFromAnotherOrganization() throws Exception {
        //given
        String expected = "test";
        String toChange = "test123";
        OrganizationWriteModel organizationWriteModel = new OrganizationWriteModel();
        organizationWriteModel.setName(toChange);

        authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_UPDATE));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(ORGANIZATION_UPDATE));

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/organizations/" + organizationRepository.findById(1L).get().getOrgId())
                        .header("Authorization", "Bearer " + secondToken)
                        .content(objectMapper.writeValueAsString(organizationWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(403));

        //then
        Assertions.assertEquals(expected, organizationRepository.findById(1L).get().getName());
    }

    @Test
    void shouldDeleteOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_UPDATE, ORGANIZATION_DELETE));

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/organizations/" + organizationRepository.findAll().get(0).getOrgId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        Assertions.assertEquals(1, organizationRepository.count());
        Assertions.assertEquals(Organization.OrgStatus.DELETED, organizationRepository.findAll().get(0).getOrgStatus());
    }

    @Test
    void usersShouldNotBeAbleToLogInAfterDeletingOrganization() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_ADD_USER, ORGANIZATION_UPDATE, ORGANIZATION_DELETE));

        UserWriteModel userWriteModel = new UserWriteModel();
        userWriteModel.setFirstName("Test1");
        userWriteModel.setLastName("Test2");
        userWriteModel.setEmail("test2@wp.pl");
        userWriteModel.setRoleId(1);

        // create user in organization
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/organizations/users")
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(userWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));

        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findAll().get(0);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset/" + resetPasswordToken.getToken())
                .content("test"));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials("test2@wp.pl", "test"))))
                .andExpect(MockMvcResultMatchers.status().is(200));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials("test@wp.pl", "123"))))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/organizations/" + organizationRepository.findAll().get(0).getOrgId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials("test2@wp.pl", "test"))))
                .andExpect(MockMvcResultMatchers.status().is(401));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials("test@wp.pl", "123"))))
                .andExpect(MockMvcResultMatchers.status().is(401));
    }

    @Test
    void shouldNotDeleteOrganizationWhenUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_UPDATE));

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/organizations/" + organizationRepository.findAll().get(0).getOrgId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(403));

        //then
        Assertions.assertEquals(1, organizationRepository.count());
    }

    @Test
    void shouldNotDeleteOrganizationWhenUserIsFromAnotherOrganization() throws Exception {
        //given
        authUtil.getUserWithRole("test@wp.pl", List.of(ORGANIZATION_UPDATE, ORGANIZATION_DELETE));
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(ORGANIZATION_UPDATE, ORGANIZATION_DELETE));

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/organizations/" + organizationRepository.findAll().get(0).getOrgId())
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(403));

        //then
        Assertions.assertEquals(2, organizationRepository.count());
    }

}