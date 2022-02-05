package com.gitlab.aws.cfn.resources.projects.member.group;

import org.gitlab4j.api.GitLabApiException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    @Override
    protected void handleRequest() throws Exception {
        if (!isGroupAlreadyAMember()) {
            result = failure(HandlerErrorCode.NotFound);
        }
        // nothing to do
    }

}
