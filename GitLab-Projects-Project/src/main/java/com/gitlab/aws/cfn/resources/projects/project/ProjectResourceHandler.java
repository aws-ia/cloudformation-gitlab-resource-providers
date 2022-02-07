package com.gitlab.aws.cfn.resources.projects.project;

import com.gitlab.aws.cfn.resources.projects.project.BaseHandler;
import com.gitlab.aws.cfn.resources.projects.project.CallbackContext;
import com.gitlab.aws.cfn.resources.projects.project.ResourceModel;
import com.gitlab.aws.cfn.resources.projects.project.TypeConfigurationModel;
import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import com.gitlab.aws.cfn.resources.shared.GitLabUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProjectSharedGroup;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ProjectResourceHandler extends AbstractGitlabCombinedResourceHandler<ResourceModel, com.gitlab.aws.cfn.resources.projects.project.CallbackContext, TypeConfigurationModel, ProjectResourceHandler> {

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, com.gitlab.aws.cfn.resources.projects.project.CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, com.gitlab.aws.cfn.resources.projects.project.CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return new ProjectResourceHandler().init(proxy, request, callbackContext, logger, typeConfiguration).applyActionForHandlerClass(getClass());
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    protected ResourceModel newModelForProject(Project p) {
        ResourceModel m = new ResourceModel();
        m.setId(p.getId());
        m.setName(p.getName());
        return m;
    }

    // ---------------------------------

    @Override
    protected void create() throws Exception {
        Project projectSpec = new Project()
                .withName(model.getName())
                .withIssuesEnabled(true)
                .withMergeRequestsEnabled(true)
                .withWikiEnabled(true)
                .withSnippetsEnabled(true)
                .withPublic(true);  // TODO make this configurable
        Project project = gitlab.getProjectApi().createProject(projectSpec);
        model.setId(project.getId());
    }

    @Override
    protected void read() throws Exception {
        Project project = gitlab.getProjectApi().getProject(model.getId());
        model.setName(project.getName());
    }

    @Override
    protected void update() throws Exception {
        Project project = gitlab.getProjectApi().getProject(model.getId());

        if (!Objects.equals(model.getName(), project.getName())) {
            project.setName(model.getName());
            gitlab.getProjectApi().updateProject(project);
        } else {
            // no changes needed
        }
    }

    @Override
    protected void delete() throws Exception {
        gitlab.getProjectApi().deleteProject(model.getId());
    }

    @Override
    protected void list() throws Exception {
        List<ResourceModel> groupShares = gitlab.getProjectApi().getOwnedProjects().stream().map(this::newModelForProject).collect(Collectors.toList());

        result = ProgressEvent.<ResourceModel, com.gitlab.aws.cfn.resources.projects.project.CallbackContext>builder()
                .resourceModels(groupShares)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
