package com.gitlab.aws.cfn.resources.projects.project;

import com.gitlab.aws.cfn.resources.projects.project.BaseHandler;
import com.gitlab.aws.cfn.resources.projects.project.CallbackContext;
import com.gitlab.aws.cfn.resources.projects.project.ResourceModel;
import com.gitlab.aws.cfn.resources.projects.project.TypeConfigurationModel;
import com.gitlab.aws.cfn.resources.shared.AbstractCombinedResourceHandler;
import com.gitlab.aws.cfn.resources.shared.AbstractCombinedResourceHandler.Helper;
import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import com.gitlab.aws.cfn.resources.shared.GitLabUtils;
import java.util.ArrayList;
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
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ProjectResourceHandler extends AbstractGitlabCombinedResourceHandler<Project, ResourceModel, com.gitlab.aws.cfn.resources.projects.project.CallbackContext, TypeConfigurationModel, ProjectResourceHandler> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProjectResourceHandler.class);

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, com.gitlab.aws.cfn.resources.projects.project.CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, com.gitlab.aws.cfn.resources.projects.project.CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return new ProjectResourceHandler().init(proxy, request, callbackContext, logger, typeConfiguration).applyActionForHandlerClass(getClass());
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    @Override
    public ProjectHelper newHelper() {
        return new ProjectHelper();
    }

    public class ProjectHelper extends Helper<Project> {

        @Override
        public Optional<Project> readExistingItem() {
            if (model==null || model.getId()==null) return Optional.empty();
            return gitlab.getProjectApi().getOptionalProject(model.getId());
        }

        @Override
        public List<Project> readExistingItems() throws GitLabApiException {
            return gitlab.getProjectApi().getOwnedProjects();
        }

        @Override
        public void deleteItem(Project item) throws GitLabApiException {
            gitlab.getProjectApi().deleteProject(item.getId());
        }

        @Override
        public ResourceModel modelFromItem(Project item) {
            ResourceModel m = new ResourceModel();
            m.setId(item.getId());
            m.setName(item.getName());
            return m;
        }

        @Override
        public Project createItem() throws GitLabApiException {
            Project projectSpec = new Project()
                    .withName(model.getName())
                    .withIssuesEnabled(true)
                    .withMergeRequestsEnabled(true)
                    .withWikiEnabled(true)
                    .withSnippetsEnabled(true)
                    .withPublic(true);  // TODO make this configurable
            return gitlab.getProjectApi().createProject(projectSpec);
        }

        @Override
        public void updateItem(Project existingItem, List<String> updates) throws GitLabApiException {
            if (!Objects.equals(model.getName(), existingItem.getName())) {
                existingItem.setName(model.getName());
                updates.add("Name");
            }
            if (!updates.isEmpty()) {
                //LOG.info("Changing item: " + existingItem);
                gitlab.getProjectApi().updateProject(existingItem);
            }
        }
    }

}
