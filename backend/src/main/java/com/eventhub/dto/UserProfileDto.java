package com.eventhub.dto;

import com.eventhub.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String college;
    private String profileInformation;
}
