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

package es.nachobrito.amica.domain.model.agent;

import java.util.regex.Pattern;

/**
 * @author nacho
 */
public record AgentDetails(String agentId, String agentName) {
    private static final Pattern idPattern = Pattern.compile("^[a-z0-9\\-]{5,}$");

    public AgentDetails {
        validateAgentId(agentId);

    }

    private void validateAgentId(String agentId) {
        if (!idPattern.matcher(agentId).matches()) {
            throw new IllegalArgumentException("Agent id does not match %s".formatted(idPattern.pattern()));
        }
    }
}
