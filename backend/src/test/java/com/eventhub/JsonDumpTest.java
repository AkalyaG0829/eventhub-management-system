package com.eventhub;

import com.eventhub.model.Event;
import com.eventhub.model.EventStatus;
import com.eventhub.model.EventType;
import com.eventhub.model.Role;
import com.eventhub.model.User;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JsonDumpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Test
    public void dumpJson() throws Exception {
        User organizer = userRepository.save(User.builder()
                .name("Organizer")
                .email("dumporg@test.com")
                .password("password")
                .role(Role.ORGANIZER)
                .build());

        for(int i=0; i<3; i++) {
            eventRepository.save(Event.builder()
                    .title("Sample Hackathon " + i)
                    .description("A great hackathon " + i)
                    .type(EventType.HACKATHON)
                    .organizer(organizer)
                    .startDate(LocalDateTime.now().plusDays(10))
                    .endDate(LocalDateTime.now().plusDays(12))
                    .registrationDeadline(LocalDateTime.now().plusDays(5))
                    .status(EventStatus.PUBLISHED)
                    .build());
        }

        String json = mockMvc.perform(get("/api/events"))
                .andReturn().getResponse().getContentAsString();
                
        System.out.println("JSON_OUTPUT_START");
        System.out.println(json);
        System.out.println("JSON_OUTPUT_END");
    }

    @Test
    public void dumpEvents() throws Exception {
        java.util.List<com.eventhub.model.Event> events = eventRepository.findAll();
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        for (com.eventhub.model.Event e : events) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", e.getId());
            map.put("title", e.getTitle());
            map.put("organizerEmail", e.getOrganizer().getEmail());
            list.add(map);
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        java.nio.file.Files.write(java.nio.file.Paths.get("event_dump.json"), mapper.writeValueAsBytes(list));
    }
}
