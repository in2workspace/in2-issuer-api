package es.in2.issuer.backend.infrastructure.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class SwaggerConfig {

    private static final String GROUP_NAME_PUBLIC = "Public API";
    private static final String GROUP_NAME_PRIVATE = "Private API";
    private static final String ALL_PATHS_MATCHER = "/**";
    public static final String TAG_PUBLIC = "Public";
    public static final String TAG_PRIVATE = "Private";

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .addOpenApiCustomizer(publicTagOpenApiCustomizer())
                .group(GROUP_NAME_PUBLIC)
                .pathsToMatch(ALL_PATHS_MATCHER)
                .build();
    }

    @Bean
    public GroupedOpenApi privateApi() {
        return GroupedOpenApi.builder()
                .addOpenApiCustomizer(privateTagOpenApiCustomizer())
                .group(GROUP_NAME_PRIVATE)
                .pathsToMatch(ALL_PATHS_MATCHER)
                .build();
    }

    private OpenApiCustomizer publicTagOpenApiCustomizer() {
        return tagOpenApiCustomizer(Set.of(TAG_PUBLIC));
    }

    private OpenApiCustomizer privateTagOpenApiCustomizer() {
        return tagOpenApiCustomizer(Set.of(TAG_PRIVATE));
    }

    private OpenApiCustomizer tagOpenApiCustomizer(Set<String> tagsToInclude) {
        return openApi -> {
            openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                Set<String> operationTags = operation.getTags() != null ? new HashSet<>(operation.getTags()) : Collections.emptySet();
                if (operationTags.stream().noneMatch(tagsToInclude::contains)) {
                    // Remove operations that do not have the specified tag(s)
                    pathItem.operation(httpMethod, null);
                }
            }));
            openApi.getPaths().entrySet().removeIf(entry -> entry.getValue().readOperationsMap().isEmpty());
        };
    }

}
