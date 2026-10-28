package com.tanushri.crud_demo_project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tanushri.crud_demo_project.dto.UserDto;
import com.tanushri.crud_demo_project.service.CrudService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CrudController.class)
class CrudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CrudService service;

    @Test
    void testCreateUserReturnsCreatedDto() throws Exception {
        UserDto input = userDto(null, "Ada", 36);
        when(service.createUser(input)).thenReturn(userDto(1L, "Ada", 36));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ada"))
                .andExpect(jsonPath("$.age").value(36));

        ArgumentCaptor<UserDto> inputCaptor = ArgumentCaptor.forClass(UserDto.class);
        verify(service).createUser(inputCaptor.capture());
        assertUserDto(inputCaptor.getValue(), null, "Ada", 36);
    }

    @Test
    void testUpdateUserReturnsUpdatedDto() throws Exception {
        UserDto input = userDto(null, "Ada", 36);
        when(service.updateUser(1L, input)).thenReturn(userDto(1L, "Ada", 36));

        mockMvc.perform(put("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ada"))
                .andExpect(jsonPath("$.age").value(36));

        ArgumentCaptor<UserDto> inputCaptor = ArgumentCaptor.forClass(UserDto.class);
        verify(service).updateUser(eq(1L), inputCaptor.capture());
        assertUserDto(inputCaptor.getValue(), null, "Ada", 36);
    }

    @Test
    void testDeleteUserReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(service).deleteUser(1L);
    }

    @Test
    void testGetUserByIdReturnsDto() throws Exception {
        when(service.getUserById(1L)).thenReturn(userDto(1L, "Ada", 36));

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ada"))
                .andExpect(jsonPath("$.age").value(36));

        verify(service).getUserById(1L);
    }

    @Test
    void testGetUserByIdReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        when(service.getUserById(99L)).thenThrow(
                new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/users/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(service).getUserById(99L);
    }

    @Test
    void testGetAllUsersReturnsPaginatedDtos() throws Exception {
        PageRequest pageable = PageRequest.of(1, 2);
        Page<UserDto> page = new PageImpl<>(List.of(userDto(3L, "Ada", 36)), pageable, 3);
        when(service.getAllUsers(eq(pageable))).thenReturn(page);

        mockMvc.perform(get("/users").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].name").value("Ada"))
                .andExpect(jsonPath("$.content[0].age").value(36))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(2));

        verify(service).getAllUsers(pageable);
    }

    private static UserDto userDto(Long id, String name, int age) {
        return UserDto.builder().id(id).name(name).age(age).build();
    }

    private static void assertUserDto(UserDto actual, Long id, String name, int age) {
        assertEquals(id, actual.getId());
        assertEquals(name, actual.getName());
        assertEquals(age, actual.getAge());
    }
}
