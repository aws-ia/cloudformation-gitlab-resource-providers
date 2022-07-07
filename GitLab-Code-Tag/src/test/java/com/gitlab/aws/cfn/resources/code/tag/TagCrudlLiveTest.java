package com.gitlab.aws.cfn.resources.code.tag;

import com.gitlab.aws.cfn.resources.shared.AbstractResourceCrudlLiveTest;
import org.apache.commons.lang3.tuple.Pair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.CommitAction;
import org.gitlab4j.api.models.CommitAction.Action;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Tag;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.ProgressEvent;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@org.junit.jupiter.api.Tag("Live")
public class TagCrudlLiveTest extends AbstractResourceCrudlLiveTest<TagResourceHandler,Tag,Pair<Integer,String>, ResourceModel, CallbackContext, TypeConfigurationModel> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TagCrudlLiveTest.class);
    private Project newProject;
    private Commit initialCommit;

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

    protected ResourceModel newModelForCreate() throws Exception {
        if (newProject==null) {
            newProject = gitlab.getProjectApi().createProject(TEST_PREFIX+"-project-" + TEST_ID);
            CommitAction commitAction = new CommitAction()
                    .withAction(Action.CREATE)
                    .withContent("This is data added for a test.")
                    .withFilePath("sample.txt");
            initialCommit = gitlab.getCommitsApi().createCommit(newProject.getId(), "main", "Sample commit", null, null, null, commitAction);
        }
        return ResourceModel.builder().name("cfn-test-tag-"+TEST_ID).projectId(newProject.getId()).ref("main").message("Sample tag").build();
    }


    @Test
    @Order(41)
    public void testUpdateChangeRefAndMessage() throws Exception {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        // before commits, tag should point at head of branch
        Tag tag0 = getRealItem();
        assertThat(tag0.getCommit().getId()).isEqualTo(initialCommit.getId());
        assertThat(model.getRef()).isEqualTo("main");
        assertThat(model.getCommitId()).isEqualTo(tag0.getCommit().getId());

        CommitAction commitAction2 = new CommitAction()
                .withAction(Action.CREATE)
                .withContent("This is data added for a test.")
                .withFilePath("sample2.txt");
        Commit commit2 = gitlab.getCommitsApi().createCommit(newProject.getId(), "main", "Sample commit", null, null, null, commitAction2);

        CommitAction commitAction3 = new CommitAction()
                .withAction(Action.CREATE)
                .withContent("This is data added for a test.")
                .withFilePath("sample3txt");
        Commit commit3 = gitlab.getCommitsApi().createCommit(newProject.getId(), "main", "Sample commit", null, null, null, commitAction3);

        // after commits, tag should point at head of branch
        Tag tag1 = getRealItem();
        assertThat(tag1.getCommit().getId()).isEqualTo(initialCommit.getId());

        model.setMessage("Tag updated to point directly at commit 2");
        if (TagResourceHandler.ALLOW_REF_UPDATES) {
            model.setRef(commit2.getId());
        }
        ProgressEvent<ResourceModel, CallbackContext> response = new UpdateHandler().handleRequest(proxy, newRequestObject(), null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertStatusSuccess(response);

        Tag tag2 = getRealItem();
        assertThat(tag2.getCommit().getId()).isEqualTo(
            TagResourceHandler.ALLOW_REF_UPDATES ? commit2.getId() : initialCommit.getId());
    }

}
