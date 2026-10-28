package com.tanushri.crud_demo_project.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tanushri.crud_demo_project.dto.UserDto;

public interface CrudService {

    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUser(Long id);

    UserDto getUserById(Long id);

    Page<UserDto> getAllUsers(Pageable pageable);
}
