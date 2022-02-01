package com.cloudformation.gitlab.core;


import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;

public class GitLabService {

    GitLabApi gitLabApi;

    public GitLabService(String url, String token) {
        gitLabApi = new GitLabApi(url, token);
    }

    public Project getGroup(String groupId) {
        return null;
    }


}
