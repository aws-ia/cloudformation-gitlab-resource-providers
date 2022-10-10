package com.gitlab.aws.cfn.resources.groups.member.group;

import com.gitlab.aws.cfn.resources.groups.member.group.GroupAccessToGroupResourceHandler.GroupMember;
import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import com.gitlab.aws.cfn.resources.shared.GitLabUtils;
import static com.gitlab.aws.cfn.resources.shared.GitLabUtils.fromNiceAccessLevelString;
import static com.gitlab.aws.cfn.resources.shared.GitLabUtils.toNiceAccessLevelString;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.tuple.Pair;
import org.gitlab4j.api.AbstractApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Group;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class GroupAccessToGroupResourceHandler extends AbstractGitlabCombinedResourceHandler<GroupAccessToGroupResourceHandler,GroupMember,Pair<Integer,Integer>, ResourceModel,CallbackContext,TypeConfigurationModel> {

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> implements BaseHandlerAdapterDefault<GroupAccessToGroupResourceHandler, ResourceModel,CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return BaseHandlerAdapterDefault.super.handleRequest(proxy, request, callbackContext, logger, typeConfiguration);
        }

        @Override
        public GroupAccessToGroupResourceHandler newCombinedHandler() {
            return new GroupAccessToGroupResourceHandler();
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    @Override
    public GroupMemberHelper newHelper() {
        return new GroupMemberHelper();
    }

    public class GroupMemberHelper extends Helper {

        @Override
        public Pair<Integer, Integer> getId(ResourceModel model) {
            return GitLabUtils.pair(model.getSharedGroupId(), model.getSharedWithGroupId());
        }

        @Override
        protected Optional<GroupMember> findExistingItemWithNonNullId(Pair<Integer, Integer> id) throws Exception {
            return findExistingItemWithIdDefaultInefficiently(id);
        }

        @Override
        public List<GroupMember> readExistingItems() throws GitLabApiException {
            if (model==null || model.getSharedGroupId()==null) return Collections.emptyList();
            return getSharedWithGroups(model.getSharedGroupId());
        }

        @Override
        public void deleteItem(GroupMember item) throws GitLabApiException {
            gitlab.getGroupApi().unshareGroup(model.getSharedGroupId(), model.getSharedWithGroupId());
        }

        @Override
        public ResourceModel modelFromItem(GroupMember g) {
            ResourceModel m = new ResourceModel();
            m.setSharedGroupId(model.getSharedGroupId());
            m.setSharedWithGroupId(g.groupId);
            initMembershipId(m);
            m.setAccessLevel(toNiceAccessLevelString(g.getAccessLevel()));
            return m;
        }

        protected void initMembershipId(ResourceModel model) {
            model.setMembershipId(model.getSharedGroupId()+"-"+model.getSharedWithGroupId());
        }


        @Override
        public GroupMember createItem() throws GitLabApiException {
            Group sharedWith = gitlab.getGroupApi().shareGroup(model.getSharedGroupId(), model.getSharedWithGroupId(), getAccessLevel(), null);
            return GroupMember.of(model);
        }

        @Override
        public void updateItem(GroupMember existingItem, List<String> updates) throws GitLabApiException {
            throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, request.getLogicalResourceIdentifier());
        }
    }


    public static class GroupMember {
        Integer groupId;
        Integer accessLevel;
        static GroupMember of(Map<?,?> m) {
            GroupMember result = new GroupMember();
            result.groupId = (Integer) m.get("group_id");
            result.accessLevel = (Integer) m.get("group_access_level");
            return result;
        }
        static GroupMember of(ResourceModel m) {
            GroupMember result = new GroupMember();
            result.groupId = m.getSharedWithGroupId();
            result.accessLevel = GitLabUtils.fromNiceAccessLevelString(m.getAccessLevel()).toValue();
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

    @SuppressWarnings("unchecked")
    public static List<GroupMember> getSharedWithGroups(GitLabApi gitlab, Integer groupId) throws GitLabApiException {
        return new AbstractApi(gitlab) {
            public List<GroupMember> getSharedGroups(Integer groupId) throws GitLabApiException {
                Response r = get(Status.OK, null, "groups", groupId);
                Map<?,?> map = r.readEntity(Map.class);
                Object sharedWith = map.get("shared_with_groups");
                if (sharedWith instanceof List) {
                    return ((List<Map<?,?>>) sharedWith).stream().map(GroupMember::of).collect(Collectors.toList());
                } else {
                    return Collections.emptyList();
                }
            }
        }.getSharedGroups(groupId);
    }

    protected AccessLevel getAccessLevel() {
        return fromNiceAccessLevelString(model.getAccessLevel());
    }

}
