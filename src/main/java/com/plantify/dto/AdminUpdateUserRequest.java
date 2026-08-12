package com.plantify.dto;

import com.plantify.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUpdateUserRequest {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Email address must be a valid email format")
    private String email;

    private String fullName;

    private String mobileNumber;

    private String password;

    @NotNull(message = "Role must be specified")
    private Role role;
}
