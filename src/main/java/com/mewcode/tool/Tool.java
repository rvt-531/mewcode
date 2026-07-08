
package com.mewcode.tool;

import java.util.Map;

public interface Tool {

    String name();

    String description();

    ToolCategory category();

    Map<String, Object> schema();

    ToolResult execute(Map<String, Object> args);

    default boolean shouldDefer() {
        return false;
    }
}

