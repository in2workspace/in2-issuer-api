package es.in2.issuer.backend.backoffice.domain.service.impl;

import es.in2.issuer.backend.backoffice.domain.service.VaultService;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.ReactiveVaultKeyValueOperations;
import org.springframework.vault.core.ReactiveVaultOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.support.VaultResponseSupport;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class VaultServiceImpl implements VaultService {
    private final ReactiveVaultKeyValueOperations vaultOperations;
    private static final String BACKEND ="kv";


    public VaultServiceImpl(ReactiveVaultOperations reactiveVaultOperations){
        this.vaultOperations=reactiveVaultOperations.opsForKeyValue(BACKEND, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2);
    }

    @Override
    public Mono<Void> saveSecrets(String secretRelativePath, Map<String, String> secrets){

        return vaultOperations.put(secretRelativePath,secrets).then();
    }

    @Override
    public Mono<Map<String,Object>>getSecrets(String secretRelativePath){
        return vaultOperations.get(secretRelativePath, Map.class)
                .mapNotNull(VaultResponseSupport::getData)
                .map(data->(Map<String, Object>) data);
    }

    @Override
    public Mono<Void> deleteSecret(String secretRelativePath) {
        return vaultOperations.delete(secretRelativePath).then();
    }

    @Override
    public Mono<Void> patchSecrets(String secretRelativePath, Map<String, String> partialUpdate) {
        return getSecrets(secretRelativePath)
                .defaultIfEmpty(new HashMap<>())
                .map(currentSecrets -> {
                    currentSecrets.putAll(partialUpdate);
                    return currentSecrets;
                })
                .flatMap(updatedSecrets ->
                        vaultOperations.put(secretRelativePath, updatedSecrets).then()
                );
    }

}