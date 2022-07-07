package com.gitlab.aws.api.models;

import org.gitlab4j.api.utils.JacksonJson;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AccessToken {
    private String userId;
    private List<String> scopes;
    private String name;
    private Date expiresAt;
    private Integer id;
    private Boolean active;
    private Date createdAt;
    private Boolean revoked;
    private String token;
    private Integer accessLevel;

    public AccessToken() {
    }

    public static AccessToken of(Map<?,?> m) {
        AccessToken result = new AccessToken();
        result.id = (Integer) m.get("id");
        result.name = (String) m.get("name");
        result.userId = (String) m.get("user_id");
        result.scopes = (List<String>) m.get("scopes");
        result.expiresAt = (Date) m.get("expires_at");
        result.createdAt = (Date) m.get("created_at");
        result.active = (Boolean) m.get("active");
        result.revoked = (Boolean) m.get("revoked");
        result.accessLevel = (Integer) m.get("access_level");
        return result;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public AccessToken withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getName() {
        return name;
    }
    public AccessToken withName(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public AccessToken withScopes(List<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public AccessToken withExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public AccessToken withAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
        return this;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}

