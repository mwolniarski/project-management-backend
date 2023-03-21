package pl.wolniarskim.project_management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pl.wolniarskim.project_management.util.AuthUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RegistrationControllerTest {


    @Autowired
    AuthUtil authUtil;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldRegisterUserWithGivenEmail() throws Exception {
        String authToken = authUtil.getAuthToken("test@wp.pl");

        Assertions.assertTrue(StringUtils.isNotBlank(authToken));
    }

    @Test
    void shouldNotRegisterUserWithEmailWhichIsAlreadyTaken() throws Exception {
        authUtil.getAuthToken("test@wp.pl");

        mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/registration")
                    .content(objectMapper.writeValueAsString(new RegistrationRequest("", "", "test@wp.pl", "123")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(400));
    }
}
