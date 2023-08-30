package com.coapi.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coapi.entity.ChildrenEntity;


public interface ChildRepository extends JpaRepository<ChildrenEntity, Serializable>{
	
	public List<ChildrenEntity> findByCaseNum(Long caseNum);
}
