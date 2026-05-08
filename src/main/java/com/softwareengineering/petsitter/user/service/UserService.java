package com.softwareengineering.petsitter.user.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    public String getCurrentUser() {
        return "localuser";
    }
}
