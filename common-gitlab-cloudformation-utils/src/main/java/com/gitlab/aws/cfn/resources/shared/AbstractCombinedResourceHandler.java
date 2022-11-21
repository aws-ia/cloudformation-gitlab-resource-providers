package com.gitlab.aws.cfn.resources.shared;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.function.FailableRunnable;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class AbstractCombinedResourceHandler<
        This extends AbstractCombinedResourceHandler<This, ItemT, IdT, ResourceModelT, CallbackContextT, TypeConfigurationModelT>,
        ItemT, IdT, ResourceModelT, CallbackContextT extends RetryableCallbackContext, TypeConfigurationModelT>
        implements HandlerMixins<ResourceModelT, CallbackContextT> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractCombinedResourceHandler.class);

    protected ResourceHandlerRequest<ResourceModelT> request;
    protected CallbackContextT callbackContext;
    protected Logger logger;
    protected TypeConfigurationModelT typeConfiguration;
    protected ResourceModelT model;

    protected ProgressEvent<ResourceModelT, CallbackContextT> result = null;

    public interface BaseHandlerAdapterDefault<This extends AbstractCombinedResourceHandler<This,?,?, ResourceModel, CallbackContext, TypeConfigurationModel>, ResourceModel, CallbackContext extends RetryableCallbackContext, TypeConfigurationModel> {
        This newCombinedHandler();

        default ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return newCombinedHandler().init(proxy, request, callbackContext, logger, typeConfiguration).applyActionForHandlerClass(getClass());
        }
    }

    @SuppressWarnings("unchecked")
    public This init(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModelT> request,
            CallbackContextT callbackContext,
            Logger logger,
            TypeConfigurationModelT typeConfiguration) {

        this.request = request;
        this.callbackContext = callbackContext;
        this.logger = logger;
        this.typeConfiguration = typeConfiguration;
        // TODO should be more careful between "previous" and "desired"
        this.model = request.getDesiredResourceState();
        // TODO need to capture -- request.getPreviousResourceState() -- and use it in places?  e.g. merge them
        /*
        eg:

          when creating ProjectToken

            we set the ApiToken into the state

          on a subsequent update from the user

            ApiToken will be in PreviousState but probably not in DesiredState
            should it be in the result?

          is this also a problem with fields like ID?
          (masked by the fact that we regenerate)
         */


        try {
            doInit();

        } catch (Exception e) {
            // only available when run locally
            LOG.error("Error in request init: "+e);
            LOG.debug("Trace for error: "+e, e);
            logger.log("Error in request init: "+toStackTrace(e));

            if (e instanceof FailureToSetInResult) {
                result = (ProgressEvent<ResourceModelT, CallbackContextT>) ((FailureToSetInResult)e).getResult();
            } else {
                result = failure(e);
            }
        }

        return (This) this;
    }

    public ResourceModelT getModel() { return model; }

    protected static String toStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    private final Map<String,Supplier<ProgressEvent<ResourceModelT, CallbackContextT>>> actionClazzNames = new LinkedHashMap<>();
    {
        actionClazzNames.put("CreateHandler", this::safeCreate);
        actionClazzNames.put("ReadHandler", this::safeRead);
        actionClazzNames.put("UpdateHandler", this::safeUpdate);
        actionClazzNames.put("DeleteHandler", this::safeDelete);
        actionClazzNames.put("ListHandler", this::safeList);
    }
    public ProgressEvent<ResourceModelT, CallbackContextT> applyActionForHandlerClass(Class<?> clazz) {
        return actionClazzNames.get(clazz.getSimpleName()).get();
    }

    @SuppressWarnings("unchecked")
    protected <T extends Throwable> ProgressEvent<ResourceModelT, CallbackContextT> safely(FailableRunnable<T> task) {
        if (result!=null) return result;

        try {
            task.run();
            if (result==null) result = success();

        } catch (Throwable e) {
            // only available when run locally
            LOG.warn("Error in request: "+e);
            LOG.debug("Trace for error: "+e, e);
            logger.log("Error in request: "+toStackTrace(e));

            if (e instanceof FailureToSetInResult) {
                result = (ProgressEvent<ResourceModelT, CallbackContextT>) ((FailureToSetInResult)e).getResult();
            } else {
                result = failure(e);
            }
        }

        return result;
    }

    public ProgressEvent<ResourceModelT, CallbackContextT> safeCreate() { return safely(this::create); }
    public ProgressEvent<ResourceModelT, CallbackContextT> safeRead() { return safely(this::read); }
    public ProgressEvent<ResourceModelT, CallbackContextT> safeUpdate() { return safely(this::update); }
    public ProgressEvent<ResourceModelT, CallbackContextT> safeDelete() { return safely(this::delete); }
    public ProgressEvent<ResourceModelT, CallbackContextT> safeList() { return safely(this::list); }

    protected void doInit() throws Exception {
        // nothing here, but can be overridden
    }

    protected void create() throws Exception { newHelper().create(); }
    protected void read() throws Exception { newHelper().read(); };
    protected void update() throws Exception { newHelper().update(); };
    protected void delete() throws Exception { newHelper().delete(); };
    protected void list() throws Exception { newHelper().list(); };

    public abstract Helper newHelper();

    public abstract class Helper {
        public abstract IdT getId(ResourceModelT model);
        public final Optional<ItemT> findExistingItemMatchingModel() throws Exception {
            if (model==null) return Optional.empty();
            return findExistingItemWithId(getId(model));
        }

        public final Optional<ItemT> findExistingItemWithId(IdT id) throws Exception {
            if (id==null) return Optional.empty();
            return findExistingItemWithNonNullId(id);
        }
        protected abstract Optional<ItemT> findExistingItemWithNonNullId(IdT id) throws Exception;

        protected Optional<ItemT> findExistingItemWithIdDefaultInefficiently(IdT id) throws Exception {
            return readExistingItems().stream().filter(item -> Objects.equals(id, getId(modelFromItem(item)))).findAny();
        }

        public abstract List<ItemT> readExistingItems() throws Exception;
        public abstract ResourceModelT modelFromItem(ItemT item);
        public abstract ItemT createItem() throws Exception;
        public abstract void updateItem(ItemT item, List<String> updates) throws Exception;
        public abstract void deleteItem(ItemT item) throws Exception;

        public void create() throws Exception {
            try {
                Optional<ItemT> item = findExistingItemMatchingModel();
                if (item.isPresent()) fail(HandlerErrorCode.AlreadyExists, "Resource already exists.");
                model = modelFromItem(createItem());
            } catch (Exception e) {
                logger.log("Exception caught creating model, rethrowing. " + e);
                throw e;
            }
        }

        public void read() throws Exception {
            try {
                Optional<ItemT> item = findExistingItemMatchingModel();
                if (!item.isPresent()) failNotFound();
                model = modelFromItem(item.get());
            } catch (Exception e) {
                logger.log("Exception caught reading model, rethrowing. " + e);
                throw e;
            }
        }

        public void update() throws Exception {
            try {
                Optional<ItemT> item = findExistingItemMatchingModel();
                if (!item.isPresent()) failNotFound();
                List<String> updates = new ArrayList<>();
                updateItem(item.get(), updates);
                if (!updates.isEmpty()) item = findExistingItemMatchingModel();
                model = modelFromItem(item.get());
                result = success(updates.isEmpty()
                        ? "No changes"
                        : "Changes: " + updates);
            } catch (Exception e) {
                logger.log("Exception caught updating model, rethrowing. " + e);
                throw e;
            }
        }

        public void delete() throws Exception {
            try {
                Optional<ItemT> item = findExistingItemMatchingModel();
                if(callbackContext != null) {
                    // Delete has already been called and hopefully the project has now been deleted
                    if (item.isPresent()) {
                        // Deletion Failed or not yet done?
                        if (callbackContext.getRetry() <= 0) {
                            result = failure(HandlerErrorCode.NotStabilized, "Resource not deleted after 5 retries");
                        } else {
                            deleteItem(item.get());
                            result = inProgress(newCallbackContext(callbackContext.getRetry() - 1));
                        }
                    }
                    model = null;
                    return;
                }

                // First attempt at deletion
                if (!item.isPresent()) failNotFound();
                deleteItem(item.get());
                result = inProgress(newCallbackContext(5));
            } catch (Exception e) {
                logger.log("Exception caught deleting model, rethrowing. " + e);
                throw e;
            }
        }

        public void list() throws Exception {
            try {
                List<ResourceModelT> models = readExistingItems().stream().map(this::modelFromItem).collect(Collectors.toList());
                result = ProgressEvent.<ResourceModelT, CallbackContextT>builder()
                        .resourceModels(models)
                        .status(OperationStatus.SUCCESS)
                        .build();
            } catch (Exception e) {
                logger.log("Exception caught listing model, rethrowing. " + e);
                throw e;
            }
        }
    }

    protected abstract CallbackContextT newCallbackContext(int retries);
}
