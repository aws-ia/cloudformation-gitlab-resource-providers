package com.gitlab.aws.cfn.resources.shared;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.commons.lang3.function.FailableRunnable;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class AbstractCombinedResourceHandler<ResourceModel, CallbackContext, TypeConfigurationModel, This extends AbstractCombinedResourceHandler<ResourceModel, CallbackContext, TypeConfigurationModel, This>>
        implements HandlerMixins<ResourceModel, CallbackContext> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractCombinedResourceHandler.class);

    protected ResourceHandlerRequest<ResourceModel> request;
    protected CallbackContext callbackContext;
    protected Logger logger;
    protected TypeConfigurationModel typeConfiguration;
    protected ResourceModel model;

    protected ProgressEvent<ResourceModel, CallbackContext> result = null;

    @SuppressWarnings("unchecked")
    public This init(
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
            doInit();

        } catch (Exception e) {
            // only available when run locally
            LOG.error("Error in request init: "+e, e);

            result = failure(e);
        }

        return (This) this;
    }

    public ResourceModel getModel() { return model; }

    private final Map<String,Supplier<ProgressEvent<ResourceModel, CallbackContext>>> actionClazzNames = new LinkedHashMap<>();
    {
        actionClazzNames.put("CreateHandler", this::safeCreate);
        actionClazzNames.put("ReadHandler", this::safeRead);
        actionClazzNames.put("UpdateHandler", this::safeUpdate);
        actionClazzNames.put("DeleteHandler", this::safeDelete);
        actionClazzNames.put("ListHandler", this::safeList);
    }
    public ProgressEvent<ResourceModel, CallbackContext> applyActionForHandlerClass(Class<?> clazz) {
        return actionClazzNames.get(clazz.getSimpleName()).get();
    }

    protected <T extends Throwable> ProgressEvent<ResourceModel, CallbackContext> safely(FailableRunnable<T> task) {
        if (result!=null) return result;

        try {
            task.run();
            if (result==null) result = success();

            return result;

        } catch (Throwable e) {
            // only available when run locally
            LOG.error("Error in request: "+e, e);

            return failure(e);
        }
    }

    public ProgressEvent<ResourceModel, CallbackContext> safeCreate() { return safely(this::create); }
    public ProgressEvent<ResourceModel, CallbackContext> safeRead() { return safely(this::read); }
    public ProgressEvent<ResourceModel, CallbackContext> safeUpdate() { return safely(this::update); }
    public ProgressEvent<ResourceModel, CallbackContext> safeDelete() { return safely(this::delete); }
    public ProgressEvent<ResourceModel, CallbackContext> safeList() { return safely(this::list); }

    protected void doInit() throws Exception {
        // nothing here, but can be overridden
    }

    protected abstract void create() throws Exception;
    protected abstract void read() throws Exception;
    protected abstract void update() throws Exception;
    protected abstract void delete() throws Exception;
    protected abstract void list() throws Exception;

}
