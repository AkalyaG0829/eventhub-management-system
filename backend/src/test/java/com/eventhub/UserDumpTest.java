package com.eventhub;

import com.eventhub.model.User;
import com.eventhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserDumpTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void dumpUsers() throws Exception {
        List<User> users = userRepository.findAll();
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        java.nio.file.Files.write(java.nio.file.Paths.get("user_dump.json"), mapper.writeValueAsBytes(users));
    }
}
