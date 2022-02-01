package com.cloudformation.gitlab.core;

import java.util.List;
import java.util.Optional;

public interface GitLabService<T> {

    List<T> getAll();

    Optional<T> getById(Integer id);

}
