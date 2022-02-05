package com.gitlab.aws.cfn.resources.projects.member.group;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    @Override
    protected void handleRequest() throws Exception {
        // not updateable, unless not present
        if (!isGroupAlreadyAMember()) {

        }
    }

}
