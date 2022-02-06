package com.gitlab.aws.cfn.resources.projects.member.group;

public class DeleteHandler extends BaseHandlerResource {

    @Override protected void handleRequest() throws Exception {
        delete();
    }

}
