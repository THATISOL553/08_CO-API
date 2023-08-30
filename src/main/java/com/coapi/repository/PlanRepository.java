package com.coapi.repository;

import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.coapi.entity.PlanEntity;

public interface PlanRepository extends JpaRepository<PlanEntity, Serializable>{

}
