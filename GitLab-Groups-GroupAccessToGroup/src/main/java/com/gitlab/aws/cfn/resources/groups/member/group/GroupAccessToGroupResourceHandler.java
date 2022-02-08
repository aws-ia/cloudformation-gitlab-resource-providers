package com.gitlab.aws.cfn.resources.groups.member.group;

import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import static com.gitlab.aws.cfn.resources.shared.GitLabUtils.fromNiceAccessLevelString;
import static com.gitlab.aws.cfn.resources.shared.GitLabUtils.toNiceAccessLevelString;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.gitlab4j.api.AbstractApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class GroupAccessToGroupResourceHandler extends AbstractGitlabCombinedResourceHandler<ResourceModel, com.gitlab.aws.cfn.resources.groups.member.group.CallbackContext, TypeConfigurationModel, GroupAccessToGroupResourceHandler> {

    public static class BaseHandlerAdapter extends BaseHandler<com.gitlab.aws.cfn.resources.groups.member.group.CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, com.gitlab.aws.cfn.resources.groups.member.group.CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, com.gitlab.aws.cfn.resources.groups.member.group.CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return new GroupAccessToGroupResourceHandler().init(proxy, request, callbackContext, logger, typeConfiguration).applyActionForHandlerClass(getClass());
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    protected void initMembershipId(ResourceModel model) {
        model.setMembershipId(model.getSharedGroupId()+"-"+model.getSharedWithGroupId());
    }

    protected ResourceModel newModelForMemberGroup(GroupMember g) {
        ResourceModel m = new ResourceModel();
        m.setSharedGroupId(model.getSharedGroupId());
        m.setSharedWithGroupId(g.groupId);
        initMembershipId(m);
        m.setAccessLevel(toNiceAccessLevelString(g.getAccessLevel()));
        return m;
    }

    protected boolean isGroupAlreadyAMember() throws GitLabApiException {
        return getGroupAlreadyAMember().isPresent();
    }

    protected Optional<GroupMember> getGroupAlreadyAMember() throws GitLabApiException {
        return getSharedWithGroups(model.getSharedGroupId()).stream().filter(m -> m.groupId.equals(model.getSharedWithGroupId())).findAny();
    }

    public static class GroupMember {
        Integer groupId;
        Integer accessLevel;
        static GroupMember of(Map m) {
            GroupMember result = new GroupMember();
            result.groupId = (Integer) m.get("group_id");
            result.accessLevel = (Integer) m.get("group_access_level");
            return result;
        }

        public AccessLevel getAccessLevel() {
            return AccessLevel.forValue(accessLevel);
        }
    }

    // not (yet) in Java API
    protected List<GroupMember> getSharedWithGroups(Integer groupId) throws GitLabApiException {
        return getSharedWithGroups(gitlab, groupId);
    }

    public static List<GroupMember> getSharedWithGroups(GitLabApi gitlab, Integer groupId) throws GitLabApiException {
        return new AbstractApi(gitlab) {
            public List<GroupMember> getSharedGroups(Integer groupId) throws GitLabApiException {
                Response r = get(Status.OK, null, "groups", groupId);
                Map map = r.readEntity(Map.class);
                Object sharedWith = map.get("shared_with_groups");
                if (sharedWith instanceof List) {
                    return ((List<Map<?,?>>) sharedWith).stream().map(GroupMember::of).collect(Collectors.toList());
                } else {
                    return Collections.emptyList();
                }
            }
        }.getSharedGroups(groupId);
    }

    protected void addMember() throws GitLabApiException {
        gitlab.getGroupApi().shareGroup(model.getSharedGroupId(), model.getSharedWithGroupId(), getAccessLevel(), null);
    }

    protected void removeMember() throws GitLabApiException {
        gitlab.getGroupApi().unshareGroup(model.getSharedGroupId(), model.getSharedWithGroupId());
    }

    protected AccessLevel getAccessLevel() {
        return fromNiceAccessLevelString(model.getAccessLevel());
    }


    // ---------------------------------

    @Override
    protected void create() throws Exception {
        if (isGroupAlreadyAMember()) {
            throw fail(HandlerErrorCode.AlreadyExists);
        }

        addMember();
        initMembershipId(model);
    }

    @Override
    protected void read() throws Exception {
        Optional<GroupMember> member = getGroupAlreadyAMember();

        if (!member.isPresent()) {
            throw fail(HandlerErrorCode.NotFound);

        } else {
            model.setAccessLevel(toNiceAccessLevelString(member.get().getAccessLevel()));
        }
    }

    @Override
    protected void update() throws Exception {
        Optional<GroupMember> member = getGroupAlreadyAMember();

        if (!member.isPresent()) {
            // does not exist; create
            addMember();

        } else if (!Objects.equals(getAccessLevel(), member.get().getAccessLevel())) {
            // change access level; for a _group_ share, i don't see how to do this apart from delete and re-create
            // (_user_ members can be updated, but not shared groups)
            removeMember();
            addMember();

        } else {
            // no changes needed
        }
    }

    @Override
    protected void delete() throws Exception {
        removeMember();
    }

    @Override
    protected void list() throws Exception {
        List<ResourceModel> groupShares = getSharedWithGroups(model.getSharedGroupId()).stream().map(this::newModelForMemberGroup).collect(Collectors.toList());

        result = ProgressEvent.<ResourceModel, com.gitlab.aws.cfn.resources.groups.member.group.CallbackContext>builder()
                .resourceModels(groupShares)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
