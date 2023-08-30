package com.coapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coapi.binding.CoResponse;
import com.coapi.service.CoService;

@RestController
public class CoController {

	@Autowired
	private CoService coService;
	
	@GetMapping("/process")
	public CoResponse processTriggers() {
		 return coService.processPendingTriggers();
	}
}
