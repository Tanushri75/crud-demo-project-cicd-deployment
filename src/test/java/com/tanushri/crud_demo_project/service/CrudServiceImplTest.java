package com.tanushri.crud_demo_project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tanushri.crud_demo_project.dto.UserDto;
import com.tanushri.crud_demo_project.entity.User;
import com.tanushri.crud_demo_project.repository.MyCrudRepository;

@ExtendWith(MockitoExtension.class)
class CrudServiceImplTest {

    @Mock
    private MyCrudRepository repository;

    @InjectMocks
    private CrudServiceImpl service;

    @Test
    void testCreateUserSavesAndReturnsDto() {
        UserDto input = userDto(null, "Ada", 36);
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserDto result = service.createUser(input);

        assertUserDto(result, 1L, "Ada", 36);
        verify(repository).save(any(User.class));
    }

    @Test
    void testUpdateUserUpdatesAndReturnsExistingUser() {
        User existing = user(1L, "Old name", 20);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UserDto result = service.updateUser(1L, userDto(null, "New name", 25));

        assertUserDto(result, 1L, "New name", 25);
        verify(repository).save(existing);
    }

    @Test
    void testUpdateUserThrowsNotFoundWhenUserDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateUser(1L, userDto(null, "Ada", 36)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository).findById(1L);
    }

    @Test
    void testDeleteUserDeletesExistingUser() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteUser(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void testDeleteUserThrowsNotFoundWhenUserDoesNotExist() {
        when(repository.existsById(1L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.deleteUser(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository).existsById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void testGetUserByIdReturnsDtoWhenUserExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(user(1L, "Ada", 36)));

        UserDto result = service.getUserById(1L);

        assertUserDto(result, 1L, "Ada", 36);
        verify(repository).findById(1L);
    }

    @Test
    void testGetUserByIdThrowsNotFoundWhenUserDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getUserById(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository).findById(1L);
    }

    @Test
    void testGetAllUsersMapsResultsAndPreservesPagination() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> users = new PageImpl<>(List.of(user(1L, "Ada", 36)), pageable, 1);
        when(repository.findAll(pageable)).thenReturn(users);

        Page<UserDto> result = service.getAllUsers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(pageable, result.getPageable());
        assertUserDto(result.getContent().get(0), 1L, "Ada", 36);
        verify(repository).findAll(pageable);
    }

    private static void assertUserDto(UserDto actual, Long id, String name, int age) {
        assertEquals(id, actual.getId());
        assertEquals(name, actual.getName());
        assertEquals(age, actual.getAge());
    }

    private static User user(Long id, String name, int age) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        return user;
    }

    private static UserDto userDto(Long id, String name, int age) {
        return UserDto.builder().id(id).name(name).age(age).build();
    }

}
