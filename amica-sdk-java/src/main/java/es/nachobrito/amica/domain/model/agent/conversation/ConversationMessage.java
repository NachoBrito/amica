/*
 *    Copyright 2025 Nacho Brito
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package es.nachobrito.amica.domain.model.agent.conversation;

import es.nachobrito.amica.domain.model.agent.AgentDetails;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * @author nacho
 */
public record ConversationMessage(
        String source,
        String text,
        ConversationMessageType messageType,
        ZonedDateTime timestamp,
        String agentThinking,
        Set<ToolExecutionRequest> executionRequests
) {

    public record ToolExecutionRequest(String id, String toolName, String toolArguments) {
    }

    public static ConversationMessage ofUserMessage(String userName, String text) {
        return new ConversationMessage(userName, text, ConversationMessageType.USER_MESSAGE, ZonedDateTime.now(ZoneOffset.UTC), null, null);
    }

    public static ConversationMessage ofAgentResponse(AgentDetails agentDetails, String text, String thinking, Set<ToolExecutionRequest> executionRequests) {
        return new ConversationMessage(agentDetails.agentId(), text, ConversationMessageType.AGENT_MESSAGE, ZonedDateTime.now(ZoneOffset.UTC), thinking, executionRequests);
    }

    public static ConversationMessage ofToolExecution(ToolExecutionRequest executionRequest, String result) {
        return new ConversationMessage(executionRequest.toolName(), result, ConversationMessageType.TOOL_EXECUTION, ZonedDateTime.now(ZoneOffset.UTC), null, Set.of(executionRequest));
    }
}
