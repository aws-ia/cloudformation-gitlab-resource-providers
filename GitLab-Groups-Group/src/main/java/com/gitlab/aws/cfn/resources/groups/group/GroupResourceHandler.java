package com.gitlab.aws.cfn.resources.groups.group;

import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.ProjectSharedGroup;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.*;

public class GroupResourceHandler extends AbstractGitlabCombinedResourceHandler<GroupResourceHandler,Group,Integer, ResourceModel,CallbackContext,TypeConfigurationModel> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GroupResourceHandler.class);

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> implements BaseHandlerAdapterDefault<GroupResourceHandler, ResourceModel,CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return BaseHandlerAdapterDefault.super.handleRequest(proxy, request, callbackContext, logger, typeConfiguration);
        }

        @Override
        public GroupResourceHandler newCombinedHandler() {
            return new GroupResourceHandler();
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    @Override
    public GroupHelper newHelper() {
        return new GroupHelper();
    }

    @Override
    protected CallbackContext newCallbackContext(int retries) {
        return new CallbackContext(retries);
    }

    public class GroupHelper extends Helper {

        @Override
        public Integer getId(ResourceModel model) {
            return model.getId();
        }

        @Override
        protected Optional<Group> findExistingItemWithNonNullId(Integer id) throws Exception {
            return gitlab.getGroupApi().getOptionalGroup(id);
        }

        @Override
        public List<Group> readExistingItems() throws GitLabApiException {
            return gitlab.getGroupApi().getGroups();
        }

        @Override
        public void deleteItem(Group item) throws GitLabApiException {
            gitlab.getGroupApi().deleteGroup(item.getId());
        }

        @Override
        public ResourceModel modelFromItem(Group p) {
            ResourceModel m = new ResourceModel();
            m.setId(p.getId());
            m.setName(p.getName());
            m.setPath(p.getPath());
            m.setParentId(p.getParentId());
            return m;
        }

        @Override
        public Group createItem() throws GitLabApiException {
            GroupParams gp = new GroupParams()
                    .withName(model.getName())
                    .withPath(model.getPath())
                    .withParentId(model.getParentId());
            return gitlab.getGroupApi().createGroup(gp);
        }

        @Override
        public void updateItem(Group existingItem, List<String> updates) throws GitLabApiException {
            result = failure(HandlerErrorCode.NotUpdatable);
        }
    }

}
