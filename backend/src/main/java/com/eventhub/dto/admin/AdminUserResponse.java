package com.eventhub.dto.admin;

import com.eventhub.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String college;
    private String phone;
    private LocalDateTime createdAt;
    private boolean enabled;
}
