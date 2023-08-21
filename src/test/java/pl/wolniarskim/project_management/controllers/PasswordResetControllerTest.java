package pl.wolniarskim.project_management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.wolniarskim.project_management.models.DTO.LoginCredentials;
import pl.wolniarskim.project_management.models.ResetPasswordToken;
import pl.wolniarskim.project_management.repositories.ResetPasswordTokenRepository;
import pl.wolniarskim.project_management.util.AuthUtil;

import java.time.LocalDateTime;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class PasswordResetControllerTest {

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Test
    void shouldCreateResetPasswordTokenIfAccountExistsWithGivenEmail() throws Exception {
        //given
        authUtil.getAuthToken("test1@wp.pl");
        //when
        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset")
                .param("email", "test1@wp.pl"));
        //then
        Assertions.assertEquals(1,  resetPasswordTokenRepository.count());
    }

    @Test
    void shouldNotCreateResetPasswordTokenIfAccountNotExistsWithGivenEmail() throws Exception {
        //given
        authUtil.getAuthToken("test1@wp.pl");
        //when
        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset")
                .param("email", "test2@wp.pl"));
        //then
        Assertions.assertEquals(0,  resetPasswordTokenRepository.count());
    }

    @Test
    void shouldResetPassword() throws Exception {
        //given
        authUtil.getAuthToken("test1@wp.pl");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset")
                .param("email", "test1@wp.pl"));

        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findAll().get(0);
        //when

        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset/" + resetPasswordToken.getToken())
                        .content("test"));

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .content(objectMapper.writeValueAsString(new LoginCredentials("test1@wp.pl", "test"))))
                .andExpect(MockMvcResultMatchers.status().is(200));
    }

    @Test
    void shouldNotResetPasswordIfTokenExpired() throws Exception {
        //given
        authUtil.getAuthToken("test1@wp.pl");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset")
                .param("email", "test1@wp.pl"));

        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findAll().get(0);
        resetPasswordToken.setExpiredAt(LocalDateTime.now().minusMinutes(5));
        resetPasswordTokenRepository.save(resetPasswordToken);
        //when + then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset/" + resetPasswordToken.getToken())
                .content("test"))
                .andExpect(MockMvcResultMatchers.status().is(400));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials("test1@wp.pl", "test"))))
                .andExpect(MockMvcResultMatchers.status().is(401));
    }

    @Test
    void shouldNotResetPasswordIfTokenDoesNotExists() throws Exception {
        //given
        authUtil.getAuthToken("test1@wp.pl");

        //when + then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset/123")
                        .content("test"))
                .andExpect(MockMvcResultMatchers.status().is(403));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(new LoginCredentials("test1@wp.pl", "test"))))
                .andExpect(MockMvcResultMatchers.status().is(401));
    }
}