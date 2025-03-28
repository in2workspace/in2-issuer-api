package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface ConfigurationService {
    Mono<Void> saveConfiguration(String orgId, Map<String,String> settings);
    Mono<Map<String, String>> getConfigurationMapByOrganization(String orgId);
    Mono<Void> updateOrInsertKeys(String orgId, Map<String, String> updates);

}
