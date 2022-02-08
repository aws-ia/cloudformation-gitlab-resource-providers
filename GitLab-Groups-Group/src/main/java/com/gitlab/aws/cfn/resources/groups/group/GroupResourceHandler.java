package com.gitlab.aws.cfn.resources.groups.group;

import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
import org.gitlab4j.api.models.Project;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class GroupResourceHandler extends AbstractGitlabCombinedResourceHandler<Group, ResourceModel, com.gitlab.aws.cfn.resources.groups.group.CallbackContext, TypeConfigurationModel, GroupResourceHandler> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GroupResourceHandler.class);

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, com.gitlab.aws.cfn.resources.groups.group.CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, com.gitlab.aws.cfn.resources.groups.group.CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return new GroupResourceHandler().init(proxy, request, callbackContext, logger, typeConfiguration).applyActionForHandlerClass(getClass());
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

    public class GroupHelper extends Helper<Group> {

        @Override
        public Optional<Group> readExistingItem() {
            if (model==null || model.getId()==null) return Optional.empty();
            return gitlab.getGroupApi().getOptionalGroup(model.getId());
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
            // no updates supported
        }
    }

}
