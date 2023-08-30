package com.coapi.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coapi.entity.CoTriggerEntity;

public interface CoTriggerRepository extends JpaRepository<CoTriggerEntity, Serializable> {

	public List<CoTriggerEntity> findByTrgStatus(String staus);
	
	public CoTriggerEntity findByCaseNum(Long caseNum);
}
