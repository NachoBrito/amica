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

package es.nachobrito.amica.infrastructure.langchain4j;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;
import es.nachobrito.amica.domain.model.agent.tool.Tool;
import es.nachobrito.amica.domain.model.agent.tool.ToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nacho
 */
public class LangChain4JToolManager implements ToolManager {
    private Logger log = LoggerFactory.getLogger(getClass());
    private static final double SIMILARITY_THRESHOLD = .65;
    private final Map<String, Tool<?, ?>> tools = new HashMap<>();
    private final Map<String, Embedding> embeddings = new HashMap<>();

    private EmbeddingModel embeddingModel;

    private EmbeddingModel getEmbeddingModel() {
        if (embeddingModel == null) {
            embeddingModel = new BgeSmallEnV15EmbeddingModel();
        }
        return embeddingModel;
    }

    public void addTools(Set<? extends Tool<?, ?>> tools) {
        tools.forEach(this::addTool);
    }

    public void addTool(Tool<?, ?> tool) {
        var embedding = getEmbedding(tool.getDescription());
        tools.put(tool.getName(), tool);
        embeddings.put(tool.getName(), embedding);
    }

    @Override
    public List<? extends Tool<?, ?>> getRelevantTools(String userQuery) {
        var queryEmbedding = getEmbedding(userQuery);
        var sortedToolNames = getSortedToolNames(queryEmbedding);
        return sortedToolNames
                .keySet()
                .stream()
                .map(this.tools::get)
                .toList();
    }

    private Embedding getEmbedding(String text) {
        log.info("Calculate embedding for \"{}\"", text);
        return getEmbeddingModel().embed(text).content();
    }

    private LinkedHashMap<String, Double> getSortedToolNames(Embedding queryEmbedding) {
        return this
                .embeddings
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> CosineSimilarity.between(entry.getValue(), queryEmbedding)
                )).entrySet()
                .stream()
                .filter(entry -> entry.getValue() > SIMILARITY_THRESHOLD)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o1, LinkedHashMap::new));
    }

    @Override
    public Optional<Tool<?, ?>> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }
}
