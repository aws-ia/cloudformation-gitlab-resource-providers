package com.gitlab.aws.cfn.resources.projects.member.group;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;

public abstract class BaseHandlerGitLab<CallbackContext> extends BaseHandlerGeneric<CallbackContext, TypeConfigurationModel> {

    public final static String DEFAULT_URL = "https://gitlab.com/";

    protected GitLabApi gitlab;

    @Override
    protected void requestInit() throws Exception {
        if (gitlab==null) {
            String url = typeConfiguration.getGitLabAccess().getUrl();
            if (url==null) url = DEFAULT_URL;
            gitlab = new GitLabApi(url, typeConfiguration.getGitLabAccess().getAccessToken());
        }
    }

    public void setGitLabApi(GitLabApi gitLabApi) {
        this.gitlab = gitLabApi;
    }

}
