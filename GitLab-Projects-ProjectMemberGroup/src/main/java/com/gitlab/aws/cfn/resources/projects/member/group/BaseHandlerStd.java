package com.gitlab.aws.cfn.resources.projects.member.group;

import javax.annotation.Nullable;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class BaseHandlerStd extends BaseHandlerGeneric<CallbackContext, TypeConfigurationModel> {

    protected GitLabApi gitlab;

    @Override
    protected void requestInit() throws Exception {
        if (gitlab==null) {
            gitlab = new GitLabApi(typeConfiguration.getGitLabAccess().getUrl(), typeConfiguration.getGitLabAccess().getAccessToken());
        }
    }

    public void setGitLabApi(GitLabApi gitLabApi) {
        this.gitlab = gitLabApi;
    }

    protected void initMembershipId(ResourceModel model) {
        model.setMembershipId(model.getProjectId()+"-"+model.getGroupId());
    }

    protected boolean isGroupAlreadyAMember() throws GitLabApiException {
        return gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups().stream().anyMatch(share -> model.getGroupId().equals(""+share.getGroupId()));
    }

    protected void create() throws GitLabApiException {
        gitlab.getProjectApi().shareProject(model.getProjectId(), Integer.parseInt(model.getGroupId()), AccessLevel.DEVELOPER, null);
    }

}
