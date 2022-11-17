package com.gitlab.aws.cfn.resources.projects.project;

import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;

import java.util.*;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Visibility;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ProjectResourceHandler extends AbstractGitlabCombinedResourceHandler<ProjectResourceHandler,Project,Integer, ResourceModel,CallbackContext,TypeConfigurationModel> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProjectResourceHandler.class);

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> implements BaseHandlerAdapterDefault<ProjectResourceHandler, ResourceModel,CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return BaseHandlerAdapterDefault.super.handleRequest(proxy, request, callbackContext, logger, typeConfiguration);
        }

        @Override
        public ProjectResourceHandler newCombinedHandler() {
            return new ProjectResourceHandler();
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

    @Override
    protected CallbackContext newCallbackContext(int retries) {
        return new CallbackContext(retries);
    }

    public class ProjectHelper extends Helper {
        @Override
        public Integer getId(ResourceModel model) {
            return model.getId();
        }

        @Override
        public Optional<Project> findExistingItemWithNonNullId(Integer id) throws Exception {
            return gitlab.getProjectApi().getOptionalProject(id);
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
            m.setPath(item.getPath());
            m.setPublic_(firstNonNull(item.getPublic(), Visibility.PUBLIC.equals(item.getVisibility())));
            return m;
        }

        @Override
        public Project createItem() throws GitLabApiException {
            Project projectSpec = new Project()
                    .withName(model.getName())
                    .withPath(model.getPath())

                    // v3 and v4 api support
                    .withPublic(firstNonNull(model.getPublic_(), false))
                    .withVisibility(Boolean.TRUE.equals(model.getPublic_()) ? Visibility.PUBLIC : Visibility.PRIVATE)

                    .withIssuesEnabled(true)
                    .withMergeRequestsEnabled(true)
                    .withWikiEnabled(true)
                    .withSnippetsEnabled(true)
                    ;
            try {
                return gitlab.getProjectApi().createProject(projectSpec);
            } catch (GitLabApiException e) {
                String validationErrorsMessage  = "";
                Map<String, List<String>> validationErrors = e.getValidationErrors();
                if(!validationErrors.isEmpty()) {
                    validationErrorsMessage = validationErrors.entrySet().stream().map(stringListEntry -> stringListEntry.getKey() + stringListEntry.getValue().stream().collect(Collectors.joining(","))).collect(Collectors.joining(","));
                }
                throw new RuntimeException(validationErrorsMessage, e);
            }
        }

        public boolean isPublic(Project item) {
            if (item.getPublic()!=null) return item.getPublic();
            return Visibility.PUBLIC.equals(item.getVisibility());
        }

        @Override
        public void updateItem(Project existingItem, List<String> updates) throws GitLabApiException {
            if (!Objects.equals(model.getName(), existingItem.getName())) {
                existingItem.setName(model.getName());
                updates.add("Name");
            }
            if (model.getPublic_()!=null && !Objects.equals(model.getPublic_(), isPublic(existingItem))) {
                existingItem.setPublic(model.getPublic_());
                existingItem.setVisibility(model.getPublic_() ? Visibility.PUBLIC : Visibility.PRIVATE);
                updates.add("Public");
            }
            if (!updates.isEmpty()) {
                //LOG.info("Changing item: " + existingItem);
                gitlab.getProjectApi().updateProject(existingItem);
            }
        }
    }

}
