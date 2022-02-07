package com.gitlab.aws.cfn.resources.shared;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.models.AccessLevel;

public class GitLabUtils {

    public static AccessLevel fromNiceAccessLevelString(String s) {
        try {
            return AccessLevel.valueOf(s.toUpperCase().replace(' ', '_'));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported AccessLevel '"+s+"'");
        }
    }

    public static String toNiceAccessLevelString(AccessLevel level) {
        return Arrays.stream(level.name().toLowerCase().
                split(" ")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

}
