package com.gearup.userauth.dto;

import com.gearup.userauth.model.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private Long id;
    private String name;
    private String resource;
    private String action;

    public static PermissionResponse fromPermission(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .resource(permission.getResource())
                .action(permission.getAction())
                .build();
    }
}
