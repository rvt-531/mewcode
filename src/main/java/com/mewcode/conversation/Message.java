

package com.mewcode.conversation;

import java.util.List;

public class Message {

    private String role;   // "user" / "assistant" / "system"
    private String content;   // 文本内容

    private List<ThinkingBlock> thinkingBlocks;   // 模型的思考过程（Claude thinking）
    private List<ToolUseBlock> toolUses;    // 模型请求调用的工具

    private List<ToolResultBlock> toolResults;   // 工具执行后的结果

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<ThinkingBlock> getThinkingBlocks() { return thinkingBlocks; }

    public void setThinkingBlocks(List<ThinkingBlock> thinkingBlocks) { this.thinkingBlocks = thinkingBlocks; }

    public List<ToolUseBlock> getToolUses() { return toolUses; }

    public void setToolUses(List<ToolUseBlock> toolUses) { this.toolUses = toolUses; }

    public List<ToolResultBlock> getToolResults() { return toolResults; }
    public void setToolResults(List<ToolResultBlock> toolResults) { this.toolResults = toolResults; }
}
