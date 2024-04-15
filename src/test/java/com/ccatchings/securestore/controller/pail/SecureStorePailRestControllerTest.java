package com.ccatchings.securestore.controller.pail;

import com.ccatchings.securestore.bootstrap.BeforeAllExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.keycloak.test.TestsHelper.importTestRealm;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith({BeforeAllExtension.class})
class SecureStorePailRestControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    @ExtendWith({BeforeAllExtension.class})
    @WithJwt("keycloak_user_token.json")
    public void testGetRootFolder() throws Exception {
        this.mvc.perform(get("/pails/firstPail").with(bearerTokenFor("secstoreuser")))
                .andExpect(status().isOk());
    }

    @Test
    @ExtendWith({BeforeAllExtension.class})
    @WithJwt("keycloak_user_token.json")
    public void testGetSubFolder() throws Exception {
        this.mvc.perform(get("/pails/firstPail/teams").with(bearerTokenFor("secstoreuser")))
                .andExpect(status().isOk());
    }

    @Test
    @WithJwt("keycloak_user_token.json")
    public void testGetPails() throws Exception{
        this.mvc.perform(get("/pails").with(bearerTokenFor("secstoreuser")))
                .andExpect(status().isOk());
    }

    @Test
    @WithJwt("keycloak_user_token.json")
    public void testGetPailFile() throws Exception{
        this.mvc.perform(get("/pails/firstPail/teams/ATL?fileName=BBogdanovic.json").with(bearerTokenFor("secstoreuser")))
                .andExpect(status().isOk());
    }

    private RequestPostProcessor bearerTokenFor(String username) {
        String token = getToken(username, username);

        return new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.addHeader("Authorization", "Bearer " + token);
                return request;
            }
        };
    }

    private String getToken(String username, String password) {
        Keycloak keycloak = Keycloak.getInstance(
                "http://localhost:8280",
                "securestoretest",
                username,
                password,
                "securestoretest",
                "secret");
        return keycloak.tokenManager().getAccessTokenString();
    }

}