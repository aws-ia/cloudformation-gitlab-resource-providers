package com.gitlab.aws.cfn.resources.projects.member.user;

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
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.User;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UserMemberOfProjectResourceHandler extends AbstractGitlabCombinedResourceHandler<UserMemberOfProjectResourceHandler, Member, Pair<Integer,Integer>, ResourceModel, CallbackContext, TypeConfigurationModel> {

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> implements BaseHandlerAdapterDefault<UserMemberOfProjectResourceHandler, ResourceModel, CallbackContext, TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return BaseHandlerAdapterDefault.super.handleRequest(proxy, request, callbackContext, logger, typeConfiguration);
        }

        @Override public UserMemberOfProjectResourceHandler newCombinedHandler() {
            return new UserMemberOfProjectResourceHandler();
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
        public Pair<Integer, Integer> getId(ResourceModel model) {
            return GitLabUtils.pair(model.getProjectId(), model.getUserId());
        }

        @Override
        protected Optional<Member> findExistingItemWithNonNullId(Pair<Integer,Integer> id) throws GitLabApiException {
            return gitlab.getProjectApi().getOptionalMember(id.getLeft(), id.getRight());
        }

        @Override
        public List<Member> readExistingItems() throws GitLabApiException {
            if (model==null || model.getProjectId()==null) return Collections.emptyList();
            updateModelUserFields(false);
            return gitlab.getProjectApi().getMembers(model.getProjectId());
        }

        @Override
        public void deleteItem(Member item) throws GitLabApiException {
            gitlab.getProjectApi().removeMember(model.getProjectId(), item.getId());
        }

        protected void initMembershipId(ResourceModel model) {
            model.setMembershipId(model.getProjectId()+"-"+model.getUserId());
        }

        @Override
        public ResourceModel modelFromItem(Member g) {
            ResourceModel m = new ResourceModel();
            m.setProjectId(model.getProjectId());
            m.setUserId(g.getId());
            m.setUsername(g.getUsername());
            initMembershipId(m);
            m.setAccessLevel(toNiceAccessLevelString(g.getAccessLevel()));
            return m;
        }

        @Override
        public Member createItem() throws GitLabApiException {
            return gitlab.getProjectApi().addMember(model.getProjectId(), model.getUserId(), getAccessLevel(), null);
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
                gitlab.getProjectApi().updateMember(model.getProjectId(), model.getUserId(), getAccessLevel());
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


//
//    protected void initMembershipId(ResourceModel model) {
//        model.setMembershipId(model.getProjectId()+"-"+model.getUserId());
//    }
//
//    protected ResourceModel newModelForMemberUser(Member g) {
//        ResourceModel m = new ResourceModel();
//        m.setProjectId(model.getProjectId());
//        m.setUserId(g.getId());
//        initMembershipId(m);
//        m.setAccessLevel(toNiceAccessLevelString(g.getAccessLevel()));
//        return m;
//    }
//
//    protected boolean isUserAlreadyAMember() throws GitLabApiException {
//        return getUserAlreadyAMember().isPresent();
//    }
//
//    protected Optional<Member> getUserAlreadyAMember() {
//        updateModelUserFields(false);
//
//        return gitlab.getProjectApi().getOptionalMember(model.getProjectId(), model.getUserId());
//    }
//
//    protected void updateModelUserFields(boolean checkBoth) {
//        String modelUsername = model.getUsername();
//        if (modelUsername!=null && modelUsername.startsWith("@")) modelUsername = modelUsername.substring(1);
//
//        if (model.getUserId()==null) {
//            if (modelUsername!=null) {
//                Optional<User> user = gitlab.getUserApi().getOptionalUser(modelUsername);
//                if (!user.isPresent()) {
//                    throw fail(HandlerErrorCode.NotFound, "Username '"+modelUsername+"' not found.");
//                }
//                model.setUserId(user.get().getId());
//            } else {
//                throw fail(HandlerErrorCode.InvalidRequest, "UserId or Username must be supplied.");
//            }
//        } else if (model.getUsername()==null || checkBoth) {
//            Optional<User> user = gitlab.getUserApi().getOptionalUser(model.getUserId());
//            if (user.isPresent()) {
//                if (modelUsername==null) {
//                    model.setUsername(user.get().getUsername());
//                } else {
//                    if (!modelUsername.equals(user.get().getUsername())) {
//                        throw fail(HandlerErrorCode.ResourceConflict, "Username '"+model.getUsername()+"' does not match UserId "+model.getUserId()+" ('"+user.get().getUsername()+"')");
//                    }
//                }
//            } else {
//                throw fail(HandlerErrorCode.NotFound, "UserId "+model.getUserId()+" not found.");
//            }
//        }
//    }
//
//    protected void addMember() throws GitLabApiException {
//        gitlab.getProjectApi().addMember(model.getProjectId(), model.getUserId(), getAccessLevel(), null);
//    }
//
//    protected void removeMember() throws GitLabApiException {
//        gitlab.getProjectApi().removeMember(model.getProjectId(), model.getUserId());
//    }
//
//    protected AccessLevel getAccessLevel() {
//        return fromNiceAccessLevelString(model.getAccessLevel());
//    }
//
//
//    // ---------------------------------
//
//    @Override
//    protected void create() throws Exception {
//        updateModelUserFields(true);
//
//        if (isUserAlreadyAMember()) {
//            throw fail(HandlerErrorCode.AlreadyExists, "The user is already a member.");
//        }
//
//        addMember();
//        initMembershipId(model);
//    }
//
//    @Override
//    protected void read() throws Exception {
//        Optional<Member> member = getUserAlreadyAMember();
//
//        if (!member.isPresent()) {
//            throw failNotFound();
//
//        } else {
//            model.setAccessLevel(toNiceAccessLevelString(member.get().getAccessLevel()));
//        }
//    }
//
//    @Override
//    protected void update() throws Exception {
//        Optional<Member> member = getUserAlreadyAMember();
//
//        if (!member.isPresent()) {
//            // does not exist; create
//            addMember();
//
//        } else if (!Objects.equals(getAccessLevel(), member.get().getAccessLevel())) {
//            // change access level; for a _group_ share, i don't see how to do this apart from delete and re-create
//            // (_user_ members can be updated, but not shared groups)
//            gitlab.getProjectApi().updateMember(model.getProjectId(), model.getUserId(), getAccessLevel());
//
//        } else {
//            // no changes needed
//        }
//    }
//
//    @Override
//    protected void delete() throws Exception {
//        removeMember();
//    }
//
//    @Override
//    protected void list() throws Exception {
//        List<ResourceModel> userMembers = gitlab.getProjectApi().getMembers(model.getProjectId()).stream().map(this::newModelForMemberUser).collect(Collectors.toList());
//
//        result = ProgressEvent.<ResourceModel, CallbackContext>builder()
//                .resourceModels(userMembers)
//                .status(OperationStatus.SUCCESS)
//                .build();
//    }
}
