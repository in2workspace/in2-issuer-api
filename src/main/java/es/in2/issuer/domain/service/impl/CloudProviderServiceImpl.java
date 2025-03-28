package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.model.entities.CloudProvider;
import es.in2.issuer.domain.service.CloudProviderService;
import es.in2.issuer.infrastructure.repository.CloudProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudProviderServiceImpl implements CloudProviderService {
    private final CloudProviderRepository repository;

    @Override
    public Mono<CloudProvider> save(CloudProvider provider){
        return repository.save(provider);
    }

    @Override
    public Flux<CloudProvider> findAll(){
        return repository.findAll();
    }

    @Override
    public Mono<Boolean> requiresTOTP(UUID cloudProviderId) {
        return repository.findById(cloudProviderId)
                .map(CloudProvider::isRequiresTOTP)
                .defaultIfEmpty(false);
    }
}
