package com.gitlab.aws.cfn.resources.projects.member.group;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class BaseHandlerGeneric<CallbackContext, TypeConfigurationModel> extends BaseHandler<CallbackContext, TypeConfigurationModel> {

    protected ResourceHandlerRequest<ResourceModel> request;
    protected CallbackContext callbackContext;
    protected Logger logger;
    protected TypeConfigurationModel typeConfiguration;
    protected ResourceModel model;

    protected ProgressEvent<ResourceModel, CallbackContext> result = null;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            Logger logger,
            TypeConfigurationModel typeConfiguration) {

        this.request = request;
        this.callbackContext = callbackContext;
        this.logger = logger;
        this.typeConfiguration = typeConfiguration;
        this.model = request.getDesiredResourceState();

        try {
            requestInit();

            handleRequest();
            if (result==null) result = success();

            return result;

        } catch (Exception e) {

            return failure(e);
        }
    }

    protected abstract void handleRequest() throws Exception;

    protected abstract void requestInit() throws Exception;

    protected ProgressEvent<ResourceModel, CallbackContext> success() {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }


    protected ProgressEvent<ResourceModel, CallbackContext> failure() {
        return failure(HandlerErrorCode.GeneralServiceException);
    }

    protected ProgressEvent<ResourceModel, CallbackContext> failure(HandlerErrorCode code) {
        return failure(code, null);
    }

    protected ProgressEvent<ResourceModel, CallbackContext> failure(String message) {
        return failure(HandlerErrorCode.GeneralServiceException, message);
    }

    protected ProgressEvent<ResourceModel, CallbackContext> failure(Throwable exception) {
        return failure(""+exception);
    }

    protected ProgressEvent<ResourceModel, CallbackContext> failure(HandlerErrorCode code, String message) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.FAILED)
                .errorCode(code)
                .message(message)
                .build();
    }

}
