package com.coapi.repository;

import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.coapi.entity.CitizenApiEntity;


public interface CitizenAppRepository extends JpaRepository<CitizenApiEntity, Serializable>{

}
