package com.gitlab.aws.cfn.resources.groups.member.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gitlab.aws.cfn.resources.shared.RetryableCallbackContext;
import software.amazon.cloudformation.proxy.StdCallbackContext;


@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class CallbackContext extends StdCallbackContext implements RetryableCallbackContext {

    @JsonProperty("retry")
    private Integer retry;
}

