package com.gitlab.aws.cfn.resources.projects.member.group;

import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import com.gitlab.aws.cfn.resources.shared.GitLabUtils;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.ProjectSharedGroup;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class GroupAccessToProjectResourceHandler extends AbstractGitlabCombinedResourceHandler<ProjectSharedGroup,ResourceModel, CallbackContext, TypeConfigurationModel, GroupAccessToProjectResourceHandler> {

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return new GroupAccessToProjectResourceHandler().init(proxy, request, callbackContext, logger, typeConfiguration).applyActionForHandlerClass(getClass());
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    @Override
    public ProjectSharedGroupHelper newHelper() {
        return new ProjectSharedGroupHelper();
    }

    public class ProjectSharedGroupHelper extends Helper<ProjectSharedGroup> {

        @Override
        public Optional<ProjectSharedGroup> readExistingItem() throws GitLabApiException {
            return gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups().stream().filter(share -> model.getGroupId().equals(share.getGroupId())).findFirst();
        }

        @Override
        public List<ProjectSharedGroup> readExistingItems() throws GitLabApiException {
            if (model==null || model.getProjectId()==null) return Collections.emptyList();
            return gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups();
        }

        @Override
        public void deleteItem(ProjectSharedGroup item) throws GitLabApiException {
            gitlab.getProjectApi().unshareProject(model.getProjectId(), model.getGroupId());
        }

        @Override
        public ResourceModel modelFromItem(ProjectSharedGroup g) {
            ResourceModel m = new ResourceModel();
            m.setProjectId(model.getProjectId());
            m.setGroupId(g.getGroupId());
            initMembershipId(m);
            m.setAccessLevel(GitLabUtils.toNiceAccessLevelString(g.getGroupAccessLevel()));
            return m;
        }

        protected void initMembershipId(ResourceModel model) {
            model.setMembershipId(model.getProjectId()+"-"+model.getGroupId());
        }

        @Override
        public ProjectSharedGroup createItem() throws GitLabApiException {
            gitlab.getProjectApi().shareProject(model.getProjectId(), model.getGroupId(), getAccessLevel(), null);
            return readExistingItem().get();
        }

        @Override
        public void updateItem(ProjectSharedGroup existingItem, List<String> updates) throws GitLabApiException {
            if (!Objects.equals(getAccessLevel(), existingItem.getGroupAccessLevel())) {
                updates.add("AccessLevel");
            }
            if (!updates.isEmpty()) {
                deleteItem(existingItem);
                createItem();
            }
        }
    }

    protected AccessLevel getAccessLevel() {
        return GitLabUtils.fromNiceAccessLevelString(model.getAccessLevel());
    }

}
