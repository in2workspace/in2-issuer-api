package es.in2.issuer.backend.oidc4vci.application.workflow.impl;

import es.in2.issuer.backend.oidc4vci.application.workflow.GetAuthorizationServerMetadataWorkflow;
import es.in2.issuer.backend.oidc4vci.domain.model.AuthorizationServerMetadata;
import es.in2.issuer.backend.oidc4vci.domain.service.AuthorizationServerMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetAuthorizationServerMetadataWorkflowImpl implements GetAuthorizationServerMetadataWorkflow {

    private final AuthorizationServerMetadataService authorizationServerMetadataService;

    @Override
    public Mono<AuthorizationServerMetadata> execute(String processId) {
        return authorizationServerMetadataService.buildAuthorizationServerMetadata(processId);
    }

}
