package com.gitlab.aws.cfn.resources.projects.project;

import com.gitlab.aws.cfn.resources.shared.AbstractResourceCrudlLiveTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@Tag("Live")
public class ProjectCrudlLiveTest extends AbstractResourceCrudlLiveTest<ProjectResourceHandler,Project,Integer,ResourceModel,CallbackContext,TypeConfigurationModel> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProjectCrudlLiveTest.class);

    @Override
    protected TypeConfigurationModel newTypeConfiguration() {
        return TypeConfigurationModel.builder()
                .gitLabAccess(GitLabAccess.builder().accessToken(getAccessTokenForTests()).build())
                .build();
    }

    @Override
    protected HandlerWrapper newHandlerWrapper() {
        return new HandlerWrapper();
    }

    protected ResourceModel newModelForCreate() {
        return ResourceModel.builder().name(TEST_PREFIX+"-"+TEST_ID).build();
    }

    @Test @Order(41)
    public void testUpdateChangeName() throws Exception {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        model.setName(model.getName()+"-alt");
        ProgressEvent<ResourceModel, CallbackContext> response = invoke(Action.UPDATE);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getId()).isEqualTo(model.getId());
        assertThat(response.getResourceModel().getName()).endsWith("-alt");

        assertThat(getRealItem()).isNotNull()
                .matches(p -> p.getName().endsWith("-alt"));
    }

    @Test @Order(100)
    public void testCreateUpdatePerContract() throws Exception {
        model = ResourceModel.builder().name("cfn-test-sample-project"+TEST_ID).build();

        ProgressEvent<ResourceModel, CallbackContext> create = invoke(Action.CREATE);
        assertStatusSuccess(create);
        model = create.getResourceModel();

        model.setName("cfn-test-sample-project-renamed-"+TEST_ID);
        ProgressEvent<ResourceModel, CallbackContext> update = invoke(Action.UPDATE);
        assertStatusSuccess(update);
        model = update.getResourceModel();

        assertThat(model.getName()).isEqualTo("cfn-test-sample-project-renamed-"+TEST_ID);

        assertThat(getRealItem()).isNotNull()
                .matches(p -> p.getName().contains("-renamed-"));
    }

}
