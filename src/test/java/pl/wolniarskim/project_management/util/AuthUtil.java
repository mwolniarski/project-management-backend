package pl.wolniarskim.project_management.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.wolniarskim.project_management.models.DTO.*;
import pl.wolniarskim.project_management.models.Permission;
import pl.wolniarskim.project_management.models.Role;
import pl.wolniarskim.project_management.models.User;
import pl.wolniarskim.project_management.repositories.RoleRepository;
import pl.wolniarskim.project_management.repositories.UserRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthUtil {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    public String getAuthToken(String email) throws Exception {
        // rejestracja użytkownika
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/registration")
                .content(objectMapper.writeValueAsString(new RegistrationRequest("", "", email, "123", "test")))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();

        // strzał do serwisu o zalogowanie
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials(email,"123")))
                        .contentType(MediaType.APPLICATION_JSON)).andReturn();
        TokensResponse tokensResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TokensResponse.class);
        return tokensResponse.getAccessToken();
    }

    public String getRefreshToken(String email) throws Exception {
        // rejestracja użytkownika
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/registration")
                .content(objectMapper.writeValueAsString(new RegistrationRequest("", "", email, "123", "test")))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();

        // strzał do serwisu o zalogowanie
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post("/login")
                .content(objectMapper.writeValueAsString(new LoginCredentials(email,"123")))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        TokensResponse tokensResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TokensResponse.class);
        return tokensResponse.getRefreshToken();
    }

    public String getUserWithRole(String email, List<Permission.PermissionEnum> permissions) throws Exception {
        String authToken = getAuthToken(email);
        RoleWriteModel roleWriteModel = new RoleWriteModel();
        roleWriteModel.setName("TEST");
        roleWriteModel.setPermissions(permissions.stream().map(Permission::new).collect(Collectors.toList()));
        //when
        MvcResult authorization = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/roles")
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(roleWriteModel))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();

        RoleReadModel roleReadModel = objectMapper.readValue(authorization.getResponse().getContentAsString(), RoleReadModel.class);

        User user = userRepository.findByEmail(email).get();
        Role role = roleRepository.findById(roleReadModel.getId()).get();
        user.setMainRole(role);
        userRepository.save(user);
        return authToken;
    }
}
