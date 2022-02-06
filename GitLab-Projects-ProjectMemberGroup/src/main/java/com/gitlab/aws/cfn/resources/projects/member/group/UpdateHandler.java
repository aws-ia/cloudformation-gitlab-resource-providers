package com.gitlab.aws.cfn.resources.projects.member.group;

import java.util.Objects;
import org.gitlab4j.api.models.ProjectSharedGroup;

public class UpdateHandler extends BaseHandlerResource {

    @Override
    protected void handleRequest() throws Exception {
        ProjectSharedGroup share = getGroupAlreadyAMember().orElse(null);

        if (share==null) {
            // does not exist; create
            create();

        } else if (!Objects.equals(getAccessLevel(), share.getGroupAccessLevel())) {
            // change access level; for a _group_ share, i don't see how to do this apart from delete and re-create
            // (_user_ members can be updated, but not shared groups)
            delete();
            create();

        } else {
            // no changes needed
        }
    }

}
