package com.gitlab.aws.cfn.resources.groups.group;

import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
import org.gitlab4j.api.models.Project;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class GroupResourceHandler extends AbstractGitlabCombinedResourceHandler<ResourceModel, com.gitlab.aws.cfn.resources.groups.group.CallbackContext, TypeConfigurationModel, GroupResourceHandler> {

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, com.gitlab.aws.cfn.resources.groups.group.CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, com.gitlab.aws.cfn.resources.groups.group.CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return new GroupResourceHandler().init(proxy, request, callbackContext, logger, typeConfiguration).applyActionForHandlerClass(getClass());
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    protected ResourceModel newModelForGroup(Group p) {
        ResourceModel m = new ResourceModel();
        m.setId(p.getId());
        m.setName(p.getName());
        m.setPath(p.getPath());
        m.setParentId(p.getParentId());
        return m;
    }

    // ---------------------------------

    @Override
    protected void create() throws Exception {
        GroupParams gp = new GroupParams()
                .withName(model.getName())
                .withPath(model.getPath())
                .withParentId(model.getParentId());

        Group group = gitlab.getGroupApi().createGroup(gp);
        model.setId(group.getId());
    }

    @Override
    protected void read() throws Exception {
        Group group = gitlab.getGroupApi().getGroup(model.getId());
        model.setName(group.getName());
        model.setPath(group.getPath());
        model.setParentId(group.getParentId());
    }

    @Override
    protected void update() throws Exception {
        Optional<Group> group = gitlab.getGroupApi().getOptionalGroup(model.getId());

        if (!group.isPresent()) {
            create();
        } else {
            // no other updates supported
        }
    }

    @Override
    protected void delete() throws Exception {
        gitlab.getGroupApi().deleteGroup(model.getId());
    }

    @Override
    protected void list() throws Exception {
        List<ResourceModel> groups = gitlab.getGroupApi().getGroups().stream().map(this::newModelForGroup).collect(Collectors.toList());

        result = ProgressEvent.<ResourceModel, com.gitlab.aws.cfn.resources.groups.group.CallbackContext>builder()
                .resourceModels(groups)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
