package com.cloudformation.gitlab.project;

import org.gitlab4j.api.models.Project;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    // API connection tests
    @Test
    public void handleRequest_ConnectionSuccess() {
        final CreateHandler handler = new CreateHandler();

        // no name supplied, so no project manipulation attempt but successful connection is still tested
        final ResourceModel model = ResourceModel.builder()
                .server("https://gitlab.com")
                .token("glpat-5YPGKq-7gtk5R3GA6stH")
                .build();

        handler.setGitLabApi(model);
        ProgressEvent<ResourceModel, CallbackContext> response = handler.checkApiConnection(model);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_ApiNoServerFailure() {
        final ResourceModel model = ResourceModel.builder()
                .token("glpat-5YPGKq-7gtk5R3GA6stH")
                .build();
        testApiFailure(model);
    }

    @Test
    public void handleRequest_ApiNoTokenFailure() {
        final ResourceModel model = ResourceModel.builder()
                .server("https://gitlab.com")
                .build();
        testApiFailure(model);
    }

    @Test
    public void handleRequest_IncorrectDetailsFailure() {
        final ResourceModel model = ResourceModel.builder()
                .server("jfhwbeiufbweuf.hsb")
                .token("glpgerat-gegreg-gggerewrgergegreg")
                .build();
        testApiFailure(model);
    }

    public void testApiFailure(ResourceModel model) {
        final CreateHandler handler = new CreateHandler();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NetworkFailure);
    }

    @Test
    public void handleRequest_ReadSuccess() {
        final ReadHandler handler = new ReadHandler();

        Instant instant = Instant.now();
        final ResourceModel model = ResourceModel.builder()
                .name("test-project-" + instant.toEpochMilli())
                .server("https://gitlab.com")
                .token("glpat-5YPGKq-7gtk5R3GA6stH")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        // create a new proj
        prepare(request);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        assertThat(response.getResourceModel()).isEqualTo(model);

        // cleanup
        cleanup(request);
    }

    public void prepare(ResourceHandlerRequest<ResourceModel> request){
        CreateHandler handler = new CreateHandler();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    }

    public void cleanup(ResourceHandlerRequest<ResourceModel> request){
        DeleteHandler handler = new DeleteHandler();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    }
}
