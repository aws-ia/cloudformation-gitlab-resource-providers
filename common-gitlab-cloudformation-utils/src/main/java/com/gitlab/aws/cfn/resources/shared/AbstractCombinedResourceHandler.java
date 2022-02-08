package com.gitlab.aws.cfn.resources.shared;

import com.gitlab.aws.cfn.resources.shared.AbstractCombinedResourceHandler.Helper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.function.FailableRunnable;
import org.gitlab4j.api.models.Project;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudformation.model.ResourceChange;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class AbstractCombinedResourceHandler<ItemT,  ResourceModel, CallbackContext, TypeConfigurationModel, This extends AbstractCombinedResourceHandler<ItemT, ResourceModel, CallbackContext, TypeConfigurationModel, This>>
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
            LOG.error("Error in request init: "+e);
            LOG.debug("Trace for error: "+e, e);

            if (e instanceof FailureToSetInResult) {
                result = (ProgressEvent<ResourceModel, CallbackContext>) ((FailureToSetInResult)e).getResult();
            } else {
                result = failure(e);
            }
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

    @SuppressWarnings("unchecked")
    protected <T extends Throwable> ProgressEvent<ResourceModel, CallbackContext> safely(FailableRunnable<T> task) {
        if (result!=null) return result;

        try {
            task.run();
            if (result==null) result = success();

        } catch (Throwable e) {
            // only available when run locally
            LOG.warn("Error in request: "+e);
            LOG.debug("Trace for error: "+e, e);

            if (e instanceof FailureToSetInResult) {
                result = (ProgressEvent<ResourceModel, CallbackContext>) ((FailureToSetInResult)e).getResult();
            } else {
                result = failure(e);
            }
        }

        return result;
    }

    public ProgressEvent<ResourceModel, CallbackContext> safeCreate() { return safely(this::create); }
    public ProgressEvent<ResourceModel, CallbackContext> safeRead() { return safely(this::read); }
    public ProgressEvent<ResourceModel, CallbackContext> safeUpdate() { return safely(this::update); }
    public ProgressEvent<ResourceModel, CallbackContext> safeDelete() { return safely(this::delete); }
    public ProgressEvent<ResourceModel, CallbackContext> safeList() { return safely(this::list); }

    protected void doInit() throws Exception {
        // nothing here, but can be overridden
    }

    protected void create() throws Exception { newHelper().create(); }
    protected void read() throws Exception { newHelper().read(); };
    protected void update() throws Exception { newHelper().update(); };
    protected void delete() throws Exception { newHelper().delete(); };
    protected void list() throws Exception { newHelper().list(); };

    public abstract Helper<ItemT> newHelper();

    public abstract class Helper<ItemT> {
        public abstract Optional<ItemT> readExistingItem() throws Exception;
        public abstract List<ItemT> readExistingItems() throws Exception;
        public abstract ResourceModel modelFromItem(ItemT item);
        public abstract ItemT createItem() throws Exception;
        public abstract void updateItem(ItemT item, List<String> updates) throws Exception;
        public abstract void deleteItem(ItemT item) throws Exception;

        public void create() throws Exception {
            Optional<ItemT> item = readExistingItem();
            if (item.isPresent()) fail(HandlerErrorCode.AlreadyExists, "Resource already exists.");
            model = modelFromItem(createItem());
        }

        public void read() throws Exception {
            Optional<ItemT> item = readExistingItem();
            if (!item.isPresent()) failNotFound();
            model = modelFromItem(item.get());
        }

        public void update() throws Exception {
            Optional<ItemT> item = readExistingItem();
            if (!item.isPresent()) failNotFound();
            List<String> updates = new ArrayList<>();
            updateItem(item.get(), updates);
            if (!updates.isEmpty()) item = readExistingItem();
            model = modelFromItem(item.get());
            result = success(updates.isEmpty()
                    ? "No changes"
                    : "Changes: "+updates);
        }

        public void delete() throws Exception {
            Optional<ItemT> item = readExistingItem();
            if (!item.isPresent()) failNotFound();
            deleteItem(item.get());
            model = null;
        }

        public void list() throws Exception {
            List<ResourceModel> models = readExistingItems().stream().map(this::modelFromItem).collect(Collectors.toList());
            result = ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModels(models)
                    .status(OperationStatus.SUCCESS)
                    .build();
        }
    }
}
