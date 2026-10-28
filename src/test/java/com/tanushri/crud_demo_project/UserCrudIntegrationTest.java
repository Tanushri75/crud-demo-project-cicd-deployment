package com.tanushri.crud_demo_project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.tanushri.crud_demo_project.dto.UserDto;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class UserCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testUserCrudLifecycleThroughHttpAndDatabase() throws Exception {
        UserDto createRequest = UserDto.builder()
                .name("Integration Test User")
                .age(31)
                .build();

        MvcResult createResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UserDto createdUser = objectMapper.readValue(createResult.getResponse().getContentAsString(), UserDto.class);
        assertNotNull(createdUser.getId());
        assertEquals("Integration Test User", createdUser.getName());
        assertEquals(31, createdUser.getAge());

        Long userId = createdUser.getId();
        UserDto updateRequest = UserDto.builder()
                .name("Updated Integration User")
                .age(32)
                .build();
        MvcResult updateResult = mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andReturn();
        assertUser(readUser(updateResult), userId, "Updated Integration User", 32);

        MvcResult findResult = mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andReturn();
        assertUser(readUser(findResult), userId, "Updated Integration User", 32);

        MvcResult pageResult = mockMvc.perform(get("/users").param("page", "0").param("size", "1000"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode page = objectMapper.readTree(pageResult.getResponse().getContentAsString());
        assertEquals(0, page.path("number").asInt());
        assertEquals(1000, page.path("size").asInt());
        assertTrue(containsUser(page.path("content"), userId));

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    private UserDto readUser(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
    }

    private static void assertUser(UserDto actual, Long id, String name, int age) {
        assertNotNull(actual);
        assertEquals(id, actual.getId());
        assertEquals(name, actual.getName());
        assertEquals(age, actual.getAge());
    }

    private static boolean containsUser(JsonNode users, Long userId) {
        for (JsonNode user : users) {
            if (user.path("id").asLong() == userId) {
                assertEquals("Updated Integration User", user.path("name").asText());
                assertEquals(32, user.path("age").asInt());
                return true;
            }
        }
        return false;
    }
}
