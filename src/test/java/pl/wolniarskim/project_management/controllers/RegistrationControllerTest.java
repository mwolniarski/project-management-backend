package pl.wolniarskim.project_management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
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
import pl.wolniarskim.project_management.models.DTO.RegistrationRequest;
import pl.wolniarskim.project_management.models.Organization;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.repositories.OrganizationRepository;
import pl.wolniarskim.project_management.repositories.RoleRepository;
import pl.wolniarskim.project_management.repositories.UserRepository;
import pl.wolniarskim.project_management.util.AuthUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class RegistrationControllerTest {

    @Autowired
    AuthUtil authUtil;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    @Test
    void shouldRegisterUserWithGivenEmail() throws Exception {
        //given
        String authToken = authUtil.getAuthToken("test@wp.pl");

        //when + then
        Assertions.assertTrue(StringUtils.isNotBlank(authToken));
    }

    @Test
    void shouldNotRegisterUserWithEmailWhichIsAlreadyTaken() throws Exception {
        //given
        authUtil.getAuthToken("test@wp.pl");

        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/registration")
                    .content(objectMapper.writeValueAsString(new RegistrationRequest("", "", "test@wp.pl", "123", "test")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void shouldCreateOrganizationWhenCreatingAccount() throws Exception {
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/registration")
                        .content(objectMapper.writeValueAsString(new RegistrationRequest("", "", "test@wp.pl", "123", "test123")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        Organization organization = organizationRepository.findAll().get(0);
        Assertions.assertEquals("test123", organization.getName());
    }

    @Test
    void registerAccountShouldHaveSuperAdminRole() throws Exception {
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/registration")
                        .content(objectMapper.writeValueAsString(new RegistrationRequest("", "", "test@wp.pl", "123", "test")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        User user = userRepository.findAll().get(0);
        Assertions.assertEquals("SUPER_ADMIN", user.getMainRole().getName());
    }

    @Test
    void shouldCreateDefaultRolesForOrganizationAfterRegistration() throws Exception {
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/registration")
                        .content(objectMapper.writeValueAsString(new RegistrationRequest("", "", "test@wp.pl", "123", "test123")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));
        //then
        Assertions.assertEquals(4, roleRepository.count());
    }
}
