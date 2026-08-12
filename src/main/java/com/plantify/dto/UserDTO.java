package com.plantify.dto;

import com.plantify.entity.Role;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String mobileNumber;
    private Role role;
    private LocalDateTime createdAt;
}
