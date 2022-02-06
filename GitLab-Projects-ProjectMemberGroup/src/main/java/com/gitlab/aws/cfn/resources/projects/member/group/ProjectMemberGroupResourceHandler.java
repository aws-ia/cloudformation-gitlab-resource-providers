package com.gitlab.aws.cfn.resources.projects.member.group;

import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.ProjectSharedGroup;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ProjectMemberGroupResourceHandler extends AbstractGitlabCombinedResourceHandler<ResourceModel, CallbackContext, TypeConfigurationModel, ProjectMemberGroupResourceHandler> {

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return new ProjectMemberGroupResourceHandler().init(proxy, request, callbackContext, logger, typeConfiguration).applyActionForHandlerClass(getClass());
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    protected void initMembershipId(ResourceModel model) {
        model.setMembershipId(model.getProjectId()+"-"+model.getGroupId());
    }

    protected ResourceModel newModelForMemberGroup(ProjectSharedGroup g) {
        ResourceModel m = new ResourceModel();
        m.setProjectId(model.getProjectId());
        m.setGroupId(g.getGroupId());
        initMembershipId(m);
        m.setAccessLevel(toNiceAccessLevelString(g.getGroupAccessLevel()));
        return m;
    }

    protected boolean isGroupAlreadyAMember() throws GitLabApiException {
        return getGroupAlreadyAMember().isPresent();
    }

    protected Optional<ProjectSharedGroup> getGroupAlreadyAMember() throws GitLabApiException {
        return gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups().stream().filter(share -> model.getGroupId().equals(share.getGroupId())).findFirst();
    }

    protected void shareProject() throws GitLabApiException {
        gitlab.getProjectApi().shareProject(model.getProjectId(), model.getGroupId(), getAccessLevel(), null);
    }

    protected void unshareProject() throws GitLabApiException {
        gitlab.getProjectApi().unshareProject(model.getProjectId(), model.getGroupId());
    }

    protected AccessLevel getAccessLevel() {
        return fromNiceAccessLevelString(model.getAccessLevel());
    }

    protected AccessLevel fromNiceAccessLevelString(String s) {
        try {
            return AccessLevel.valueOf(s.toUpperCase().replace(' ', '_'));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported AccessLevel '"+model.getAccessLevel()+"'");
        }
    }

    protected String toNiceAccessLevelString(AccessLevel level) {
        return Arrays.stream(level.name().toLowerCase().
                split(" ")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    // ---------------------------------

    @Override
    protected void create() throws Exception {
        if (isGroupAlreadyAMember()) {
            result = failure(HandlerErrorCode.AlreadyExists);
            return;
        }

        shareProject();
        initMembershipId(model);
    }

    @Override
    protected void read() throws Exception {
        ProjectSharedGroup share = getGroupAlreadyAMember().orElse(null);

        if (!isGroupAlreadyAMember()) {
            result = failure(HandlerErrorCode.NotFound);

        } else {
            model.setAccessLevel(toNiceAccessLevelString(share.getGroupAccessLevel()));
        }
    }

    @Override
    protected void update() throws Exception {
        ProjectSharedGroup share = getGroupAlreadyAMember().orElse(null);

        if (share==null) {
            // does not exist; create
            shareProject();

        } else if (!Objects.equals(getAccessLevel(), share.getGroupAccessLevel())) {
            // change access level; for a _group_ share, i don't see how to do this apart from delete and re-create
            // (_user_ members can be updated, but not shared groups)
            unshareProject();
            shareProject();

        } else {
            // no changes needed
        }
    }

    @Override
    protected void delete() throws Exception {
        unshareProject();
    }

    @Override
    protected void list() throws Exception {
        List<ResourceModel> groupShares = gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups().stream().map(this::newModelForMemberGroup).collect(Collectors.toList());

        result = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(groupShares)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
