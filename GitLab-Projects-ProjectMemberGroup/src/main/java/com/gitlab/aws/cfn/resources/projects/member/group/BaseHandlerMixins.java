package com.gitlab.aws.cfn.resources.projects.member.group;

import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;

public interface BaseHandlerMixins<ResourceModel, CallbackContext> {

    ResourceModel getModel();

    default ProgressEvent<ResourceModel, CallbackContext> success() {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(getModel())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    default ProgressEvent<ResourceModel, CallbackContext> failure() {
        return failure(HandlerErrorCode.GeneralServiceException);
    }

    default ProgressEvent<ResourceModel, CallbackContext> failure(HandlerErrorCode code) {
        return failure(code, null);
    }

    default ProgressEvent<ResourceModel, CallbackContext> failure(String message) {
        return failure(HandlerErrorCode.GeneralServiceException, message);
    }

    default ProgressEvent<ResourceModel, CallbackContext> failure(Throwable exception) {
        return failure(""+exception);
    }

    default ProgressEvent<ResourceModel, CallbackContext> failure(HandlerErrorCode code, String message) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(getModel())
                .status(OperationStatus.FAILED)
                .errorCode(code)
                .message(message)
                .build();
    }
}
