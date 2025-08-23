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

package es.nachobrito.amica.agent.minutetaker.infrastructure.langchain4j.agent;

import dev.langchain4j.service.*;

/**
 * @author nacho
 */
public interface AIAssistant {
  @UserMessage(
"""
# Identity

You are an assistant specialized in generating minutes from conversations between the user and other agents. Your
speciality is to create a document describing a conversation.

# Instructions

Generate minutes in a narrative style, using sentences starting with "The user asked ", or "The agent response was ".

You MUST include in the minute EVERY MESSAGE in the conversation. All the user messages and the agent responses.

Respond using the following JSON format:

{
  "actors": an array with the names of the participants,
  "summary": a one-sentence summary, what the conversation was about,
  "messages": an array with the original messages in the conversation
}

DON'T include any additional text, just the JSON.

Generate minute for the following conversation:

----

{{conversation}}

----

""")
  TokenStream chat(@V("conversation") String conversation);
}
