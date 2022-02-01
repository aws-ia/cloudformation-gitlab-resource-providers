package com.cloudformation.gitlab.core;


import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GitLabGroupService implements GitLabService<Group> {

    GitLabApi gitLabApi;

    public GitLabGroupService(String url, String token) {
        gitLabApi = new GitLabApi(url, token);
    }

    @Override
    public List<Group> getAll() {
        //TODO
        return new ArrayList<>();
    }

    @Override
    public Optional<Group> getById(Integer id) {
        try {
            return  Optional.of(gitLabApi.getGroupApi().getGroup(id));
            // if null -> Optional.empty
        } catch (GitLabApiException e) {
            e.printStackTrace();
            // Throws  404 -> Optional.empty
            // anythign else throw GitLabServiceException("Error reteiving Group with id" + id, e);
        }
        return Optional.empty();
    }

}
