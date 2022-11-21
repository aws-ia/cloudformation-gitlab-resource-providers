package com.gitlab.aws.cfn.resources.shared;

import com.gitlab.aws.cfn.resources.shared.AbstractCombinedResourceHandler.BaseHandlerAdapterDefault;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.AbstractWrapper;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.LambdaWrapper;
import software.amazon.cloudformation.loggers.JavaLogPublisher;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@Tag("Live")
public abstract class AbstractResourceCrudlLiveTest<
        CombinedHandlerT extends AbstractCombinedResourceHandler<CombinedHandlerT, ItemT, IdT, ResourceModelT, CallbackContextT, TypeConfigurationModelT>,
        ItemT, IdT, ResourceModelT, CallbackContextT extends RetryableCallbackContext, TypeConfigurationModelT>
        extends GitLabLiveTestSupport {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractResourceCrudlLiveTest.class);

    @Mock
    protected AmazonWebServicesClientProxy proxy;

    @Mock
    protected Logger logger;

    protected TypeConfigurationModelT typeConfiguration;
    protected ResourceModelT model;

    protected final String TEST_ID = UUID.randomUUID().toString();

    protected ResourceHandlerRequest<ResourceModelT> newRequestObject() {
        return ResourceHandlerRequest.<ResourceModelT>builder()
                .desiredResourceState(model)
                .build();
    }

    protected abstract TypeConfigurationModelT newTypeConfiguration() throws Exception;
    protected abstract ResourceModelT newModelForCreate() throws Exception;

    protected abstract LambdaWrapper<ResourceModelT,CallbackContextT,TypeConfigurationModelT> newHandlerWrapper();

    protected LambdaWrapper<ResourceModelT,CallbackContextT,TypeConfigurationModelT> newHandlerWrapperInitialized() {
        LambdaWrapper<ResourceModelT,CallbackContextT,TypeConfigurationModelT> wrapper = newHandlerWrapper();
        try {
            Field lp = AbstractWrapper.class.getDeclaredField("loggerProxy");
            lp.setAccessible(true);
            LoggerProxy lpr = new LoggerProxy();
            lpr.addLogPublisher(new JavaLogPublisher(LOG));
            lp.set(wrapper, lpr);
            return wrapper;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected ProgressEvent<ResourceModelT,CallbackContextT> invoke(Action action) throws Exception {
        return newHandlerWrapperInitialized().invokeHandler(proxy, newRequestObject(), action, null, typeConfiguration);
    }

    @SuppressWarnings("unchecked")
    protected AbstractCombinedResourceHandler<CombinedHandlerT, ItemT, IdT, ResourceModelT, CallbackContextT, TypeConfigurationModelT>.Helper newHandlerHelper() {
        try {
            Object hw = newHandlerWrapper();
            Field f = hw.getClass().getDeclaredField("handlers");
            f.setAccessible(true);
            AbstractCombinedResourceHandler combined = ((BaseHandlerAdapterDefault) (((Map) f.get(hw)).values().iterator().next())).newCombinedHandler();
            combined.init(proxy, newRequestObject(), null, logger, newTypeConfiguration());
            return (CombinedHandlerT.Helper) combined.newHelper();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected final ItemT getRealItem() throws Exception {
        AbstractCombinedResourceHandler<CombinedHandlerT, ItemT, IdT, ResourceModelT, CallbackContextT, TypeConfigurationModelT>.Helper helper = newHandlerHelper();
        return helper.findExistingItemWithId(helper.getId(model)).get();
    }

    protected final Optional<ItemT> getRealItem(ItemT item) throws Exception {
        AbstractCombinedResourceHandler<CombinedHandlerT, ItemT, IdT, ResourceModelT, CallbackContextT, TypeConfigurationModelT>.Helper helper = newHandlerHelper();
        return helper.findExistingItemWithId(helper.getId(helper.modelFromItem(item)));
    }

    @BeforeAll
    public void initTestHandlerItems() throws Exception {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        typeConfiguration = newTypeConfiguration();
    }

    @Test @Order(10)
    public void testCreate() throws Exception {
        model = newModelForCreate();

        ProgressEvent<ResourceModelT, CallbackContextT> response = invoke(Action.CREATE);

        assertThat(response).isNotNull();
        assertStatusSuccess(response);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isNull();

        model = response.getResourceModel();
        assertThat(model).isNotNull();

        assertThat(getRealItem()).isNotNull()
                .matches(item -> model.equals(newHandlerHelper().modelFromItem(item)));
    }

    @Test @Order(20)
    public void testRead() throws Exception {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        ProgressEvent<ResourceModelT, CallbackContextT> response = invoke(Action.READ);

        assertThat(response).isNotNull();
        assertStatusSuccess(response);
        assertThat(response.getResourceModel()).isEqualTo(model);
    }

    protected void assertStatusSuccess(ProgressEvent<ResourceModelT, CallbackContextT> response) {
        assertThat(response.getStatus()).describedAs("Response: code %s, message %s.", response.getErrorCode(), response.getMessage()).isEqualTo(OperationStatus.SUCCESS);
    }

    @Test @Order(30)
    public void testList() throws Exception {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        ProgressEvent<ResourceModelT, CallbackContextT> response = invoke(Action.LIST);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModels()).anyMatch(m -> m.equals(model));
    }

    @Test @Order(40)
    public void testUpdateNoChange() throws Exception {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        // no op
        ProgressEvent<ResourceModelT, CallbackContextT> response = invoke(Action.UPDATE);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel()).isEqualTo(model);
    }

    @Test @Order(50)
    public void testDelete() throws Exception {
        if (model==null) fail("Create test must succeed for this to be meaningful.");
        ItemT oldItem = getRealItem();

        ProgressEvent<ResourceModelT, CallbackContextT> response = invoke(Action.DELETE);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel()).isNull();

        assertDelete(oldItem);
    }

    protected void assertDelete(ItemT oldItem) throws Exception {
        assertThat(getRealItem(oldItem)).isNotPresent();
    }

    @AfterAll
    public void tearDown() {
        // nothing to do
    }

}
