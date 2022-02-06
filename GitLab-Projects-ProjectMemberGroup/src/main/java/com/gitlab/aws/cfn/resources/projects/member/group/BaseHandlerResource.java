package com.gitlab.aws.cfn.resources.projects.member.group;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.ProjectSharedGroup;

public abstract class BaseHandlerResource extends BaseHandlerGitLab<CallbackContext> {

    protected void initMembershipId(ResourceModel model) {
        model.setMembershipId(model.getProjectId()+"-"+model.getGroupId());
    }

    protected boolean isGroupAlreadyAMember() throws GitLabApiException {
        return getGroupAlreadyAMember().isPresent();
    }

    protected Optional<ProjectSharedGroup> getGroupAlreadyAMember() throws GitLabApiException {
        return gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups().stream().filter(share -> model.getGroupId().equals(share.getGroupId())).findFirst();
    }

    protected void create() throws GitLabApiException {
        gitlab.getProjectApi().shareProject(model.getProjectId(), model.getGroupId(), getAccessLevel(), null);
    }

    protected void delete() throws GitLabApiException {
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

}
