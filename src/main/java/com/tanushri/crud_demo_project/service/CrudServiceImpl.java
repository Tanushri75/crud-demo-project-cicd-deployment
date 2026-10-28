package com.tanushri.crud_demo_project.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tanushri.crud_demo_project.dto.UserDto;
import com.tanushri.crud_demo_project.entity.User;
import com.tanushri.crud_demo_project.repository.MyCrudRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CrudServiceImpl implements CrudService {

    private final MyCrudRepository repository;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setAge(userDto.getAge());
        return toDto(repository.save(user));
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
        user.setName(userDto.getName());
        user.setAge(userDto.getAge());
        return toDto(repository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public UserDto getUserById(Long id) {
        return repository.findById(id)
                .map(CrudServiceImpl::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    @Override
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return repository.findAll(pageable).map(CrudServiceImpl::toDto);
    }

    private static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .age(user.getAge())
                .build();
    }
}
