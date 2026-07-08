
package com.mewcode.skill;

import com.mewcode.conversation.Message;

import java.util.List;

/**
 * Extends {@link SkillHost} with the ability to run an isolated sub-agent.
 * Implemented by the TUI layer (which owns the LLM client + agent
 * constructor) and passed into {@link SkillExecutor#executeFork}.
 */
public interface SkillForkHost extends SkillHost {

    String runSubAgent(String body, List<Message> seed, List<String> allowedTools, String model);

    List<Message> snapshotParentMessages();
}

