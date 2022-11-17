package com.gitlab.aws.cfn.resources.projects.member.group;

import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import com.gitlab.aws.cfn.resources.shared.GitLabUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.ProjectSharedGroup;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class GroupAccessToProjectResourceHandler extends AbstractGitlabCombinedResourceHandler<GroupAccessToProjectResourceHandler,ProjectSharedGroup, Pair<Integer,Integer>, ResourceModel,CallbackContext,TypeConfigurationModel> {

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> implements BaseHandlerAdapterDefault<GroupAccessToProjectResourceHandler, ResourceModel,CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return BaseHandlerAdapterDefault.super.handleRequest(proxy, request, callbackContext, logger, typeConfiguration);
        }

        @Override
        public GroupAccessToProjectResourceHandler newCombinedHandler() {
            return new GroupAccessToProjectResourceHandler();
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

    @Override
    protected CallbackContext newCallbackContext(int retries) {
        return new CallbackContext(retries);
    }

    public class ProjectSharedGroupHelper extends Helper {
        @Override
        public Pair<Integer,Integer> getId(ResourceModel model) {
            return GitLabUtils.pair(model.getProjectId(), model.getGroupId());
        }

        @Override
        protected Optional<ProjectSharedGroup> findExistingItemWithNonNullId(Pair<Integer,Integer> id) throws Exception {
            return findExistingItemWithIdDefaultInefficiently(id);
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
        public ProjectSharedGroup createItem() throws Exception {
            gitlab.getProjectApi().shareProject(model.getProjectId(), model.getGroupId(), getAccessLevel(), null);
            return findExistingItemMatchingModel().get();
        }

        @Override
        public void updateItem(ProjectSharedGroup existingItem, List<String> updates) throws Exception {
            throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, request.getLogicalResourceIdentifier());
        }
    }

    protected AccessLevel getAccessLevel() {
        return GitLabUtils.fromNiceAccessLevelString(model.getAccessLevel());
    }

}
