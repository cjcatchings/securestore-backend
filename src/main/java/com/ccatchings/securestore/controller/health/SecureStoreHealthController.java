package com.ccatchings.securestore.controller.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecureStoreHealthController {

    @GetMapping("/public/health")
    public ResponseEntity<String> getServerHealth(){
        return ResponseEntity.ok("healthy");
    }
}
