package com.gitlab.aws.cfn.resources.projects.member.group;

public class UpdateHandler extends BaseHandlerResource {

    @Override
    protected void handleRequest() throws Exception {
        // not updateable, unless not present
        if (!isGroupAlreadyAMember()) {

        }
    }

}
