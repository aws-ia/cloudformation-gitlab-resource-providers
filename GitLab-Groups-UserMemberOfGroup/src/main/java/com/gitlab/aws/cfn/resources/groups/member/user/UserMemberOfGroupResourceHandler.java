package com.gitlab.aws.cfn.resources.groups.member.user;

import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import com.gitlab.aws.cfn.resources.shared.GitLabUtils;
import static com.gitlab.aws.cfn.resources.shared.GitLabUtils.fromNiceAccessLevelString;
import static com.gitlab.aws.cfn.resources.shared.GitLabUtils.toNiceAccessLevelString;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.User;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UserMemberOfGroupResourceHandler extends AbstractGitlabCombinedResourceHandler<UserMemberOfGroupResourceHandler,Member,Pair<Integer,Integer>, ResourceModel,CallbackContext,TypeConfigurationModel> {

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> implements BaseHandlerAdapterDefault<UserMemberOfGroupResourceHandler, ResourceModel,CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return BaseHandlerAdapterDefault.super.handleRequest(proxy, request, callbackContext, logger, typeConfiguration);
        }

        @Override
        public UserMemberOfGroupResourceHandler newCombinedHandler() {
            return new UserMemberOfGroupResourceHandler();
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    @Override
    public MemberHelper newHelper() {
        return new MemberHelper();
    }

    public class MemberHelper extends Helper {

        @Override
        public Pair<Integer,Integer> getId(ResourceModel model) {
            return GitLabUtils.pair(model.getGroupId(),model.getUserId());
        }

        @Override
        protected Optional<Member> findExistingItemWithNonNullId(Pair<Integer, Integer> id) throws Exception {
            return gitlab.getGroupApi().getOptionalMember(id.getLeft(), id.getRight());
        }

        @Override
        public List<Member> readExistingItems() throws GitLabApiException {
            if (model==null || model.getGroupId()==null) return Collections.emptyList();
            updateModelUserFields(false);
            return gitlab.getGroupApi().getMembers(model.getGroupId());
        }

        @Override
        public void deleteItem(Member item) throws GitLabApiException {
            gitlab.getGroupApi().removeMember(model.getGroupId(), item.getId());
        }

        protected void initMembershipId(ResourceModel model) {
            model.setMembershipId(model.getGroupId()+"-"+model.getUserId());
        }

        @Override
        public ResourceModel modelFromItem(Member g) {
            ResourceModel m = new ResourceModel();
            m.setGroupId(model.getGroupId());
            m.setUserId(g.getId());
            m.setUsername(g.getUsername());
            initMembershipId(m);
            m.setAccessLevel(toNiceAccessLevelString(g.getAccessLevel()));
            return m;
        }

        @Override
        public Member createItem() throws GitLabApiException {
            return gitlab.getGroupApi().addMember(model.getGroupId(), model.getUserId(), getAccessLevel(), null);
        }

        @Override
        public void create() throws Exception {
            updateModelUserFields(true);
            super.create();
        }

        @Override
        public void updateItem(Member existingItem, List<String> updates) throws GitLabApiException {
            if (!Objects.equals(getAccessLevel(), existingItem.getAccessLevel())) {
                updates.add("AccessLevel");
            }
            if (!updates.isEmpty()) {
                gitlab.getGroupApi().updateMember(model.getGroupId(), model.getUserId(), getAccessLevel());
            }
        }
    }

    protected void updateModelUserFields(boolean checkBoth) {
        String modelUsername = model.getUsername();
        if (modelUsername!=null && modelUsername.startsWith("@")) modelUsername = modelUsername.substring(1);

        if (model.getUserId()==null) {
            if (modelUsername!=null) {
                Optional<User> user = gitlab.getUserApi().getOptionalUser(modelUsername);
                if (!user.isPresent()) {
                    throw fail(HandlerErrorCode.NotFound, "Username '"+modelUsername+"' not found.");
                }
                model.setUserId(user.get().getId());
            } else {
                throw fail(HandlerErrorCode.InvalidRequest, "UserId or Username must be supplied.");
            }
        } else if (model.getUsername()==null || checkBoth) {
            Optional<User> user = gitlab.getUserApi().getOptionalUser(model.getUserId());
            if (user.isPresent()) {
                if (modelUsername==null) {
                    model.setUsername(user.get().getUsername());
                } else {
                    if (!modelUsername.equals(user.get().getUsername())) {
                        throw fail(HandlerErrorCode.ResourceConflict, "Username '"+model.getUsername()+"' does not match UserId "+model.getUserId()+" ('"+user.get().getUsername()+"')");
                    }
                }
            } else {
                throw fail(HandlerErrorCode.NotFound, "UserId "+model.getUserId()+" not found.");
            }
        }
    }

    protected AccessLevel getAccessLevel() {
        return fromNiceAccessLevelString(model.getAccessLevel());
    }


}
