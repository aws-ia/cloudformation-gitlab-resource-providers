package com.gitlab.aws.api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class AccessToken {
    private Integer userId;
    private List<String> scopes;
    private String name;
    private Date expiresAt;
    private Integer id;
    private Boolean active;
    private Date createdAt;
    private Boolean revoked;
    private String token;
    private Integer accessLevel;

    public static AccessToken of(Map<?, ?> m) {
        AccessToken result = new AccessToken();
        result.id = (Integer) m.get("id");
        result.name = (String) m.get("name");
        result.userId = (Integer) m.get("user_id");
        result.scopes = (List<String>) m.get("scopes");
        result.active = (Boolean) m.get("active");
        result.revoked = (Boolean) m.get("revoked");
        result.accessLevel = (Integer) m.get("access_level");
        return result;
    }
}

