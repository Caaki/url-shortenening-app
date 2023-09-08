package com.ares.urlshortening.forms;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordForm {

    @NotEmpty(message = "Current password can't be empty")
    private String currentPassword;
    @NotEmpty(message = "New password can't be empty")
    private String newPassword;
    @NotEmpty(message = "Confirm password can't be empty")
    private String confirmPassword;

}
