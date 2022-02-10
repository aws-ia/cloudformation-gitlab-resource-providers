package com.gitlab.aws.cfn.resources.projects.accesstoken;

import com.gitlab.aws.api.models.AccessToken;
import com.gitlab.aws.cfn.resources.shared.AbstractResourceCrudlLiveTest;
import org.apache.commons.lang3.tuple.Pair;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotSupportedException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.fail;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@Tag("Live")
public class AccessTokenCrudlLiveTest extends AbstractResourceCrudlLiveTest<AccessTokenResourceHandler, AccessToken, Pair<Integer,Integer>,ResourceModel,CallbackContext,TypeConfigurationModel> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AccessTokenCrudlLiveTest.class);
    private Project newProject;

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

    protected ResourceModel newModelForCreate()  throws Exception {
        if (newProject==null) {
            newProject = gitlab.getProjectApi().createProject(TEST_PREFIX + "-project-" + TEST_ID);
        }
        List<String> scopes = new ArrayList<>();
        scopes.add("api");
        scopes.add("read_repository");
        return ResourceModel.builder().name("cfn-test-token"+"-"+TEST_ID).projectId(newProject.getId()).scopes(scopes).build();
    }

    @Test
    @Order(41) // TODO check why my create is no executed
    public void testUpdateChangeName() {
        if (model == null) fail("Create test must succeed for this to be meaningful.");

        model.setName(model.getName()+"-alt");
        new UpdateHandler().handleRequest(proxy, newRequestObject(), null, logger, typeConfiguration);
        org.junit.jupiter.api.Assertions.assertThrows(
                NotSupportedException.class, () -> new UpdateHandler().handleRequest(proxy, newRequestObject(), null, logger, typeConfiguration),
        "A token cannot be updated!");
    }

}
