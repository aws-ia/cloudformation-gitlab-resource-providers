package com.gitlab.aws.cfn.resources.groups.member.user;

import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import static com.gitlab.aws.cfn.resources.shared.GitLabUtils.fromNiceAccessLevelString;
import static com.gitlab.aws.cfn.resources.shared.GitLabUtils.toNiceAccessLevelString;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.User;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UserMemberOfGroupResourceHandler extends AbstractGitlabCombinedResourceHandler<ResourceModel, CallbackContext, TypeConfigurationModel, UserMemberOfGroupResourceHandler> {

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return new UserMemberOfGroupResourceHandler().init(proxy, request, callbackContext, logger, typeConfiguration).applyActionForHandlerClass(getClass());
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    protected void initMembershipId(ResourceModel model) {
        model.setMembershipId(model.getGroupId()+"-"+model.getUserId());
    }

    protected ResourceModel newModelForMemberUser(Member g) {
        ResourceModel m = new ResourceModel();
        m.setGroupId(model.getGroupId());
        m.setUserId(g.getId());
        initMembershipId(m);
        m.setAccessLevel(toNiceAccessLevelString(g.getAccessLevel()));
        return m;
    }

    protected boolean isUserAlreadyAMember() throws GitLabApiException {
        return getUserAlreadyAMember().isPresent();
    }

    protected Optional<Member> getUserAlreadyAMember() {
        updateModelUserFields(false);

        return gitlab.getGroupApi().getOptionalMember(model.getGroupId(), model.getUserId());
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

    protected void addMember() throws GitLabApiException {
        gitlab.getGroupApi().addMember(model.getGroupId(), model.getUserId(), getAccessLevel(), null);
    }

    protected void removeMember() throws GitLabApiException {
        gitlab.getGroupApi().removeMember(model.getGroupId(), model.getUserId());
    }

    protected AccessLevel getAccessLevel() {
        return fromNiceAccessLevelString(model.getAccessLevel());
    }


    // ---------------------------------

    @Override
    protected void create() throws Exception {
        updateModelUserFields(true);

        if (isUserAlreadyAMember()) {
            throw fail(HandlerErrorCode.AlreadyExists, "User is already a member.");
        }

        addMember();
        initMembershipId(model);
    }

    @Override
    protected void read() throws Exception {
        Optional<Member> member = getUserAlreadyAMember();

        if (!member.isPresent()) {
            throw failNotFound();

        } else {
            model.setAccessLevel(toNiceAccessLevelString(member.get().getAccessLevel()));
        }
    }

    @Override
    protected void update() throws Exception {
        Optional<Member> member = getUserAlreadyAMember();

        if (!member.isPresent()) {
            // does not exist; create
            addMember();

        } else if (!Objects.equals(getAccessLevel(), member.get().getAccessLevel())) {
            // change access level; for a _group_ share, i don't see how to do this apart from delete and re-create
            // (_user_ members can be updated, but not shared groups)
            gitlab.getGroupApi().updateMember(model.getGroupId(), model.getUserId(), getAccessLevel());

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
        List<ResourceModel> groupShares = gitlab.getGroupApi().getMembers(model.getGroupId()).stream().map(this::newModelForMemberUser).collect(Collectors.toList());

        result = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(groupShares)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
