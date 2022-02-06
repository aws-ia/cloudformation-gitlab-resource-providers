package com.gitlab.aws.cfn.resources.projects.member.group;

import org.gitlab4j.api.models.ProjectSharedGroup;
import software.amazon.cloudformation.proxy.HandlerErrorCode;

public class ReadHandler extends BaseHandlerResource {

    @Override
    protected void handleRequest() throws Exception {
        ProjectSharedGroup share = getGroupAlreadyAMember().orElse(null);

        if (!isGroupAlreadyAMember()) {
            result = failure(HandlerErrorCode.NotFound);

        } else {
            model.setAccessLevel(toNiceAccessLevelString(share.getGroupAccessLevel()));
        }
    }

}
