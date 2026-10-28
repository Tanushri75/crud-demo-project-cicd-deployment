package com.tanushri.crud_demo_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tanushri.crud_demo_project.entity.User;

public interface MyCrudRepository extends JpaRepository<User, Long>{

}
