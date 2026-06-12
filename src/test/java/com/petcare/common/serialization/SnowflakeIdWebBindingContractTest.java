package com.petcare.common.serialization;

import com.petcare.common.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SnowflakeIdWebBindingContractTest.TestController.class)
class SnowflakeIdWebBindingContractTest {

    private static final String BIG_ID = "9007199254740993";

    @Autowired
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/api/v1/test/snowflake-id")
    static class TestController {

        @GetMapping("/path/{id}")
        ApiResponse<String> path(@PathVariable Long id) {
            return ApiResponse.ok(id.toString());
        }

        @GetMapping("/query")
        ApiResponse<String> query(@RequestParam Long id) {
            return ApiResponse.ok(id.toString());
        }

        @PostMapping("/body")
        ApiResponse<String> body(@RequestBody IdRequest request) {
            return ApiResponse.ok(request.id().toString());
        }
    }

    record IdRequest(Long id) {
    }

    @Test
    @WithMockUser
    void stringPathQueryAndBodyIds_bindToExactJavaLong() throws Exception {
        mockMvc.perform(get("/api/v1/test/snowflake-id/path/" + BIG_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(BIG_ID));

        mockMvc.perform(get("/api/v1/test/snowflake-id/query").queryParam("id", BIG_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(BIG_ID));

        mockMvc.perform(post("/api/v1/test/snowflake-id/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + BIG_ID + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(BIG_ID));
    }

    @Test
    @WithMockUser
    void invalidPathId_returnsControlled400WithoutInternalDetails() throws Exception {
        mockMvc.perform(get("/api/v1/test/snowflake-id/path/not-a-long"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("java."))))
                .andExpect(content().string(not(containsString("stack"))));
    }

    @Test
    @WithMockUser
    void invalidQueryId_returnsControlled400WithoutInternalDetails() throws Exception {
        mockMvc.perform(get("/api/v1/test/snowflake-id/query").queryParam("id", "not-a-long"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("java."))))
                .andExpect(content().string(not(containsString("stack"))));
    }

    @Test
    @WithMockUser
    void invalidBodyId_returnsControlled400WithoutInternalDetails() throws Exception {
        mockMvc.perform(post("/api/v1/test/snowflake-id/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"not-a-long\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("java."))))
                .andExpect(content().string(not(containsString("stack"))));
    }
}
