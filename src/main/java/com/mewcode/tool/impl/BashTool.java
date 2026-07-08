package com.mewcode.tool.impl;

import com.mewcode.tool.Tool;
import com.mewcode.tool.ToolCategory;
import com.mewcode.tool.ToolResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BashTool implements Tool {

    private static final int MAX_TIMEOUT = 600;

    private static final String DESCRIPTION = """
            Execute a shell command and return stdout and stderr.

            IMPORTANT: Avoid using this tool to run cat, head, tail, sed, awk, or echo commands. \
            Instead use the dedicated ReadFile, EditFile, or WriteFile tools which provide a better experience.

            Usage notes:
            - The working directory persists between commands, but shell state does not.
            - Always quote file paths containing spaces with double quotes.
            - Try to maintain your current working directory using absolute paths; avoid cd unless the user explicitly requests it.
            - Optional timeout in seconds (max 600). Default is 120s.
            - When issuing multiple independent commands, make separate parallel tool calls instead of chaining with &&.
            - Use && to chain sequential dependent commands. Use ; only when you don't care if earlier commands fail.
            - DO NOT use newlines to separate commands.

            Git Safety Protocol:
            - NEVER run destructive git commands (push --force, reset --hard, checkout ., clean -f, branch -D) unless the user explicitly requests it.
            - NEVER skip hooks (--no-verify) unless the user explicitly requests it.
            - Prefer creating a new commit rather than amending an existing one.
            - Before running destructive operations, consider safer alternatives.

            Avoid unnecessary sleep commands. Do not retry failing commands in a sleep loop — diagnose the root cause instead.
            When using find, search from "." or a specific path, not "/" — scanning the full filesystem is too expensive.""";

    @Override
    public String name() {
        return "Bash";
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public ToolCategory category() {
        return ToolCategory.COMMAND;
    }

    @Override
    public Map<String, Object> schema() {
        return Map.of(
                "name", name(),
                "description", description(),
                "input_schema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "command", Map.of("type", "string", "description", "Shell command to execute"),
                                "timeout", Map.of("type", "integer", "description", "Timeout in seconds (max 600)", "default", 120)
                        ),
                        "required", List.of("command")
                )
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> args) {
        String command = stringArg(args, "command", "");
        if (command.isEmpty()) {
            return ToolResult.error("Error: command is required");
        }

        int timeout = intArg(args, "timeout", 120);
        if (timeout > MAX_TIMEOUT) {
            timeout = MAX_TIMEOUT;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.redirectErrorStream(false);
            Process process = pb.start();

            // Read stdout and stderr concurrently to avoid blocking
            String stdout;
            String stderr;
            try (InputStream stdoutStream = process.getInputStream();
                 InputStream stderrStream = process.getErrorStream()) {
                byte[] stdoutBytes = stdoutStream.readAllBytes();
                stderr = new String(stderrStream.readAllBytes());
                stdout = new String(stdoutBytes);
            }

            boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return ToolResult.error("Error: command timed out after " + timeout + "s");
            }

            int exitCode = process.exitValue();

            var sb = new StringBuilder();
            sb.append("$ ").append(command).append('\n');
            if (!stdout.isEmpty()) {
                sb.append(stdout);
                if (!stdout.endsWith("\n")) {
                    sb.append('\n');
                }
            }
            if (!stderr.isEmpty()) {
                sb.append("STDERR: ").append(stderr);
                if (!stderr.endsWith("\n")) {
                    sb.append('\n');
                }
            }
            sb.append("(exit code ").append(exitCode).append(')');

            return new ToolResult(sb.toString(), exitCode != 0);

        } catch (IOException e) {
            return ToolResult.error("Error executing command: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ToolResult.error("Error: command interrupted");
        }
    }

    private static String stringArg(Map<String, Object> args, String key, String def) {
        var v = args.get(key);
        return v instanceof String s ? s : def;
    }

    private static int intArg(Map<String, Object> args, String key, int def) {
        var v = args.get(key);
        if (v instanceof Number n) return n.intValue();
        return def;
    }
}
