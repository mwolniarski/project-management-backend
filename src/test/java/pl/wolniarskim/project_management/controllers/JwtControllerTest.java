package pl.wolniarskim.project_management.controllers;

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
import pl.wolniarskim.project_management.models.DTO.RegistrationRequest;
import pl.wolniarskim.project_management.models.DTO.TokensResponse;
import pl.wolniarskim.project_management.util.AuthUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JwtControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthUtil authUtil;
    @Test
    void shouldReturnNewAccessTokenAfterRefreshing() throws Exception {

        String refreshToken = authUtil.getRefreshToken("test@wp.pl");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get("/refreshToken")
                .header("Authorization", "Bearer " + refreshToken)
                .content(objectMapper.writeValueAsString(new RegistrationRequest("", "", "test@wp.pl", "123")))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();

        TokensResponse tokensResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TokensResponse.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/projects")
                        .header("Authorization", "Bearer " + tokensResponse.getAccessToken()))
                .andExpect(MockMvcResultMatchers.status().is(200));
    }
}
