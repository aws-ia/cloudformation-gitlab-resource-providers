package com.gitlab.aws.cfn.resources.projects.project;

import com.gitlab.aws.cfn.resources.shared.GitLabLiveTestSupport;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.gitlab4j.api.GitLabApiException;
import org.junit.jupiter.api.AfterAll;
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
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@Tag("Live")
public class ProjectCrudlLiveTest extends GitLabLiveTestSupport {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProjectCrudlLiveTest.class);

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    TypeConfigurationModel typeConfiguration;
    ResourceModel model;
    ResourceHandlerRequest<ResourceModel> request;

    final String TEST_ID = UUID.randomUUID().toString();

    @Test @Order(0)
    public void testCreate() throws GitLabApiException {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        typeConfiguration = TypeConfigurationModel.builder()
                .gitLabAccess(GitLabAccess.builder().accessToken(getAccessTokenForTests()).build())
                .build();
        model = ResourceModel.builder().name(TEST_PREFIX+"-"+TEST_ID).build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = new CreateHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).describedAs("Create failed; code %s, message %s.", response.getErrorCode(), response.getMessage()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isNull();

        assertThat(gitlab.getProjectApi().getProject(model.getId())).isNotNull();
    }

    @Test @Order(1)
    public void testRead() {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        ProgressEvent<ResourceModel, CallbackContext> response = new ReadHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getId()).isEqualTo(model.getId());
        assertThat(response.getResourceModel().getName()).isEqualTo(model.getName());
    }

    @Test @Order(2)
    public void testList() {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        // no op
        ProgressEvent<ResourceModel, CallbackContext> response = new ListHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModels()).anyMatch(m -> m.getId().equals(model.getId()));
    }

    @Test @Order(3)
    public void testUpdateNoChange() {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        // no op
        ProgressEvent<ResourceModel, CallbackContext> response = new UpdateHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getId()).isEqualTo(model.getId());
    }

    @Test @Order(4)
    public void testUpdateChangeName() throws GitLabApiException {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        model.setName(model.getName()+"-alt");
        ProgressEvent<ResourceModel, CallbackContext> response = new UpdateHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getId()).isEqualTo(model.getId());
        assertThat(response.getResourceModel().getName()).endsWith("-alt");

        assertThat(gitlab.getProjectApi().getProject(model.getId())).isNotNull()
                .matches(p -> p.getName().endsWith("-alt"));
    }

    @Test @Order(5)
    public void testDelete() throws GitLabApiException {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        ProgressEvent<ResourceModel, CallbackContext> response = new DeleteHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getId()).isEqualTo(model.getId());

        assertThat(gitlab.getProjectApi().getOptionalProject(model.getId()).isPresent()).isEqualTo(false);
    }

    @AfterAll
    public void tearDown() {
        // nothing to do
    }

}
