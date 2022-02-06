package com.gitlab.aws.cfn.resources.projects.member.group;

import software.amazon.cloudformation.proxy.HandlerErrorCode;

public class CreateHandler extends BaseHandlerResource {

    @Override protected void handleRequest() throws Exception {
        if (isGroupAlreadyAMember()) {
            result = failure(HandlerErrorCode.AlreadyExists);
            return;
        }

        create();
        initMembershipId(model);
    }

}
