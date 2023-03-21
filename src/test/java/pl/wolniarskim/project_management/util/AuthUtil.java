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
import pl.wolniarskim.project_management.models.DTO.LoginCredentials;
import pl.wolniarskim.project_management.models.DTO.RegistrationRequest;
import pl.wolniarskim.project_management.models.DTO.TokensResponse;
import pl.wolniarskim.project_management.models.DTO.UserReadModel;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthUtil {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    public String getAuthToken(String email) throws Exception {
        // rejestracja użytkownika
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/registration")
                .content(objectMapper.writeValueAsString(new RegistrationRequest("", "", email, "123")))
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
                .content(objectMapper.writeValueAsString(new RegistrationRequest("", "", email, "123")))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();

        // strzał do serwisu o zalogowanie
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post("/login")
                .content(objectMapper.writeValueAsString(new LoginCredentials(email,"123")))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        TokensResponse tokensResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TokensResponse.class);
        return tokensResponse.getRefreshToken();
    }
}
