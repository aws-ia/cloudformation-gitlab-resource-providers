package com.gitlab.aws.cfn.resources.groups.group;

import com.gitlab.aws.cfn.resources.shared.AbstractResourceCrudlLiveTest;
import static org.assertj.core.api.Assertions.assertThat;
import org.gitlab4j.api.Pager;
import org.gitlab4j.api.models.Group;
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

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@Tag("Live")
public class GroupCrudlLiveTest extends AbstractResourceCrudlLiveTest<GroupResourceHandler,Group,Integer, ResourceModel,CallbackContext,TypeConfigurationModel> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GroupCrudlLiveTest.class);

    @Override
    protected TypeConfigurationModel newTypeConfiguration() {
        return TypeConfigurationModel.builder()
                .gitLabAccess(GitLabAccess.builder().accessToken(getAccessTokenForTests()).build())
                .build();
    }

    protected ResourceModel newModelForCreate() throws Exception {
        Pager<Group> groups = gitlab.getGroupApi().getGroups(5);
        if (groups.current().isEmpty()) throw new IllegalStateException("Test requires at least one group already defined. (GitLab does not allow creating top-level groups.)");
        Integer parentId = groups.current().iterator().next().getId();

        return ResourceModel.builder()
                .name(TEST_PREFIX+"-"+TEST_ID)
                .path(TEST_PREFIX+"-path-"+TEST_ID)
                .parentId(parentId)
                .build();
    }

    @Override
    protected HandlerWrapper newHandlerWrapper() {
        return new HandlerWrapper();
    }

    @Override @Test @Order(10)
    public void testCreate() throws Exception {
        super.testCreate();
        assertThat(getRealItem())
                .matches(g -> g.getName().equals(model.getName()))
                .matches(g -> g.getPath().equals(model.getPath()));
    }

    protected void assertDelete(Group oldItem) throws Exception {
        assertSoon(() -> assertThat(getRealItem(oldItem))
                .matches(t -> !t.isPresent() || t.get().getMarkedForDeletionOn()!=null));
    }
}
