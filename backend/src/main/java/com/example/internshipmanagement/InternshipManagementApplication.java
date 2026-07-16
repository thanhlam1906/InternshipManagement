package com.example.internshipmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class InternshipManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternshipManagementApplication.class, args);
    }


}
