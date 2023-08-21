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
import pl.wolniarskim.project_management.models.DTO.RoleReadModel;
import pl.wolniarskim.project_management.models.DTO.RoleWriteModel;
import pl.wolniarskim.project_management.models.Permission;
import pl.wolniarskim.project_management.models.Role;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.repositories.PermissionRepository;
import pl.wolniarskim.project_management.repositories.RoleRepository;
import pl.wolniarskim.project_management.repositories.UserRepository;
import pl.wolniarskim.project_management.util.AuthUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pl.wolniarskim.project_management.models.Permission.PermissionEnum.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class RoleResourceTest {

    @Autowired
    AuthUtil authUtil;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PermissionRepository permissionRepository;
    @Autowired
    UserRepository userRepository;
    @Test
    void shouldAddNewRole() throws Exception {
        //given
        String authToken = authUtil.getAuthToken("test@wp.pl");
        RoleWriteModel roleWriteModel = new RoleWriteModel();
        roleWriteModel.setName("TEST");
        roleWriteModel.setPermissions(Arrays.asList(
                new Permission(ROLE_DELETE),
                new Permission(ROLE_DELETE)
        ));
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/roles")
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(roleWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        List<Role> all = roleRepository.findAll();
        Optional<Role> test = all.stream().filter(role -> role.getName().equals("TEST")).findFirst();
        Assertions.assertTrue(test.isPresent());
    }

    @Test
    void shouldReturnActivePermissions() throws Exception {
        //given
        String authToken = authUtil.getAuthToken("test@wp.pl");
        //when
        MvcResult permissions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/roles/permissions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        Permission[] allPermissions = objectMapper.readValue(permissions.getResponse().getContentAsString(), Permission[].class);

        //then
        Assertions.assertEquals(Permission.PermissionEnum.values().length, allPermissions.length);
    }

    @Test
    void shouldNotAddRoleIfUsersHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", Collections.singletonList(ROLE_DELETE));
        RoleWriteModel roleWriteModel = new RoleWriteModel();
        roleWriteModel.setName("TEST");
        roleWriteModel.setPermissions(Arrays.asList(
                new Permission(ROLE_DELETE),
                new Permission(ORGANIZATION_UPDATE)
        ));

        //when + then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/roles")
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(roleWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    void shouldDeleteRole() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", Collections.singletonList(ROLE_DELETE));
        long countOfRoles = roleRepository.count();
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/roles/1")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200));

        //then
        Assertions.assertEquals(countOfRoles-1, roleRepository.count());
    }

    @Test
    void shouldNotDeleteRoleIfUserHasNoPermission() throws Exception {
        //given
        String authToken = authUtil.getUserWithRole("test@wp.pl", Collections.singletonList(ROLE_CREATE));

        //then + when
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/roles/1")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    void shouldNotDeleteRoleIfUserIsFromAnotherOrganization() throws Exception {
        //given
        authUtil.getUserWithRole("test@wp.pl", List.of(ROLE_CREATE, ROLE_DELETE));
        List<Role> all = roleRepository.findAll();
        String secondToken = authUtil.getUserWithRole("test2@wp.pl", List.of(ROLE_CREATE, ROLE_DELETE));
        List<Role> all2 = roleRepository.findAll();
        //then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/roles/1")
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(403));
    }
}