package com.cloudformation.gitlab.core;

import org.gitlab4j.api.GitLabApi;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GitLabService<T> {

    Optional<T> create(Map<String,Object> data);

    void delete(Integer id);

    void update(Map<String,Object> data);

    Optional<T> read(Integer id);

    Optional<List<T>> list();

    Optional<T> getById(Integer id);

    boolean verifyConnection();

}
