package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.service.MarketplaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceServiceImpl implements MarketplaceService {
    //todo: implementar la obtencion del token y el POST de la VC al marketplace
    @Override
    public Mono<Void> sendVerifiableCertificationToMarketplace(String verifiableCertification) {
        log.info("Sending VerifiableCertification to marketplace: {}", verifiableCertification);
        return Mono.empty();
    }
}
