package com.gitlab.aws.cfn.resources.projects.member.group;

import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class BaseHandlerGeneric<CallbackContext, TypeConfigurationModel> extends BaseHandler<CallbackContext, TypeConfigurationModel> implements BaseHandlerMixins<ResourceModel, CallbackContext> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BaseHandlerGeneric.class);

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
            // only available when run locally
            LOG.error("Error in request: "+e, e);

            return failure(e);
        }
    }

    @Override
    public ResourceModel getModel() { return model; }

    protected abstract void handleRequest() throws Exception;

    protected abstract void requestInit() throws Exception;

}
