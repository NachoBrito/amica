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

package es.nachobrito.amica.infrastructure.lucene;

import es.nachobrito.amica.domain.model.agent.tool.Tool;
import es.nachobrito.amica.domain.model.agent.tool.ToolManager;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ToolManager implementation that uses Lucene to identify relevant tools for a given query. A
 * MemoryIndex instance is created for each tool and used to determine if it is relevant to the user query.
 *
 * @author nacho
 * @see <a href=https://lucene.apache.org/core/10_1_0/memory/org/apache/lucene/index/memory/MemoryIndex.html>MemoryIndex</a>
 */
public class LuceneToolManager implements ToolManager {
    private static final String FIELD_DESCRIPTION = "description";

    private static final Analyzer analyzer = new EnglishAnalyzer();
    private static final QueryParser parser = new QueryParser(FIELD_DESCRIPTION, analyzer);

    private Map<String, IndexedTool> toolIndex;

    private record IndexedTool(String name, Tool<?, ?> tool, MemoryIndex memoryIndex) {
        static IndexedTool of(Tool<?, ?> tool) {
            var idx = new MemoryIndex();
            idx.addField(FIELD_DESCRIPTION, tool.getDescription(), analyzer);
            return new IndexedTool(tool.getDescription(), tool, idx);
        }

        boolean matches(Query query) {
            var score = memoryIndex.search(query);
            return score > .0f;
        }
    }

    public LuceneToolManager(Set<? extends Tool<?, ?>> tools) {
        loadTools(tools);
    }

    private void loadTools(Set<? extends Tool<?, ?>> tools) {
        toolIndex = tools.stream()
                .collect(Collectors.toMap(Tool::getName, IndexedTool::of));
    }

    @Override
    public Set<Tool<?, ?>> getRelevantTools(String userQuery) {
        try {
            //We consider relevant any tool with a description containing any of the words in the query
            //Note that using Lucene's English analyzer will remove stop-words, and tokenize the others.
            final var queryText = prepareQuery(userQuery);
            final var query = parser.parse(queryText);
            return toolIndex
                    .values()
                    .stream()
                    .filter(it -> it.matches(query))
                    .map(IndexedTool::tool)
                    .collect(Collectors.toSet());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    private String prepareQuery(String userQuery) {
        final var regexLines = Pattern.compile("\\n\\s+");
        final var regexSpaces = Pattern.compile("\\s");
        final var regexReserved = Pattern.compile("([\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\\\/])");
        var result = regexLines.matcher(userQuery.trim()).replaceAll(" ");
        result = regexReserved.matcher(result.trim()).replaceAll("\\\\$1");
        result = regexSpaces.matcher(result).replaceAll(" OR ");
        return result;
    }

    @Override
    public Optional<Tool<?, ?>> getTool(String name) {
        return Optional.ofNullable(toolIndex.get(name)).map(IndexedTool::tool);
    }
}
