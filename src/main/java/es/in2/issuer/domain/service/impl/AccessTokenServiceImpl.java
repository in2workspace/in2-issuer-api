package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.InvalidTokenException;
import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.infrastructure.config.properties.VerifierProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Map;
import java.util.stream.Collectors;

import static es.in2.issuer.domain.util.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

    private final WebClient oauth2VerifierWebClient;
    private final VerifierProperties verifierProperties;

    @Override
    public Mono<String> getCleanBearerToken(String authorizationHeader) {
        return Mono.just(authorizationHeader)
                .flatMap(header -> {
                    if (header.startsWith(BEARER_PREFIX)) {
                        return Mono.just(header.replace(BEARER_PREFIX, "").trim());
                    } else {
                        return Mono.just(header);
                    }
                });
    }

    @Override
    public Mono<String> getUserId(String authorizationHeader) {
        return getCleanBearerToken(authorizationHeader)
                .flatMap(token -> {
                    try {
                        SignedJWT parsedVcJwt = SignedJWT.parse(token);
                        JsonNode jsonObject = new ObjectMapper().readTree(parsedVcJwt.getPayload().toString());
                        return Mono.just(jsonObject.get("sub").asText());
                    } catch (ParseException | JsonProcessingException e) {
                        return Mono.error(e);
                    }
                })
                .switchIfEmpty(Mono.error(new InvalidTokenException()));
    }

    @Override
    public Mono<String> getOrganizationId(String authorizationHeader) {
        return getCleanBearerToken(authorizationHeader)
                .flatMap(token -> {
                    try {
                        SignedJWT parsedVcJwt = SignedJWT.parse(token);
                        JsonNode jsonObject = new ObjectMapper().readTree(parsedVcJwt.getPayload().toString());
                        return Mono.just(jsonObject.get("organizationIdentifier").asText());
                    } catch (ParseException | JsonProcessingException e) {
                        return Mono.error(e);
                    }
                })
                .switchIfEmpty(Mono.error(new InvalidTokenException()));
    }

    @Override
    public Mono<String> getOrganizationIdFromCurrentSession() {
        return getTokenFromCurrentSession()
                .flatMap(this::getCleanBearerToken)
                .flatMap(token -> {
                    try {
                        SignedJWT parsedVcJwt = SignedJWT.parse(token);
                        JsonNode jsonObject = new ObjectMapper().readTree(parsedVcJwt.getPayload().toString());
                        return Mono.just(jsonObject.get("organizationIdentifier").asText());
                    } catch (ParseException | JsonProcessingException e) {
                        return Mono.error(e);
                    }
                })
                .switchIfEmpty(Mono.error(new InvalidTokenException()));
    }

    @Override
    public Mono<VerifierOauth2AccessToken> getM2MToken() {
        String clientId = "did:key:zDnaeeZ66JBhk2h6s1yrAwh2QUHUu8o3npP2mcb2kW8PYqj9Y"; //TODO Generate didkey
        String clientAssertion = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJzdWIiOiJkaWQ6a2V5OnpEbmFlZVo2NkpCaGsyaDZzMXlyQXdoMlFVSFV1OG8zbnBQMm1jYjJrVzhQWXFqOVkiLCJ2cF90b2tlbiI6ImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSkZVekkxTmlKOS5leUp6ZFdJaU9pSmthV1E2YTJWNU9ucEVibUZsY0dKUmEweHVlWGRZTkVkSWMyNXdWSFZIVVd0Q1NIUktORlZ1UjAwNU4zUnpOMWgxVFRJM00wd3hhV2dpTENKdVltWWlPakUzTVRjME16Z3dNRE1zSW1semN5STZJbVJwWkRwclpYazZla1J1WVdWd1lsRnJURzU1ZDFnMFIwaHpibkJVZFVkUmEwSklkRW8wVlc1SFRUazNkSE0zV0hWTk1qY3pUREZwYUNJc0luWndJanA3SWtCamIyNTBaWGgwSWpwYkltaDBkSEJ6T2k4dmQzZDNMbmN6TG05eVp5OHlNREU0TDJOeVpXUmxiblJwWVd4ekwzWXhJbDBzSW1odmJHUmxjaUk2SW1ScFpEcHJaWGs2ZWtSdVlXVndZbEZyVEc1NWQxZzBSMGh6Ym5CVWRVZFJhMEpJZEVvMFZXNUhUVGszZEhNM1dIVk5NamN6VERGcGFDSXNJbWxrSWpvaU5ERmhZMkZrWVRNdE5qZGlOQzAwT1RSbExXRTJaVE10WlRBNU5qWTBORGxtTWpWa0lpd2lkSGx3WlNJNld5SldaWEpwWm1saFlteGxVSEpsYzJWdWRHRjBhVzl1SWwwc0luWmxjbWxtYVdGaWJHVkRjbVZrWlc1MGFXRnNJanBiSW1WNVNqQmxXRUZwVDJsS1MxWXhVV2xNUTBwb1lrZGphVTlwU2taVmVra3hUbWxKYzBsdVp6RlplVWsyVjNsS1RsTlZiRWhVTTNCRVVUQktWRlF5WkVKa01HeERVVmRrU2xaVk5XMVBSRTVyVGtWd2FtUnRUbFJpTUZaR1RVVTRNVnBFUmpKUlZGSkRVV3hXZUdNelpFVlZWbXhMVXpJNVlWTlhhREpaTURWQ1ZWVldUMUZzUmtKa01tUnBVbGhvU21GclJtNVJiV1JQVm10S1FsUlZNVWhXVmtwTFZXcENjMVpXU2xaa01tUlhVbXN4YmxWV1ZsTldNVVpXVGxWU1UxWldSbTVWVkVKR1dqRktObEpZYUVaaGEwWlNVVzFrVDFaclNrSldWbEpFVmxWcmQxUnVjRkpOUlRVMlZsUktUbEpGVm5sVVZVNXlVakJGZUZaVlZrUmtNMlJ3Vld0V2MxTkdUbGRWYTFwVlVUQktWbFpZYkVOU1JrcFhVMnhXVkZaV2NFdFZWRUpIVmxaT1ZrOVZPVXBTVlZwWFZtdFdiMVZHVm5KaVJsWllWa1ZXZGxSVlRscFNNRVY0VmxWV1JGb3paRzFWYTFaelUwWk9WMVZyV2xWUk1FcFJWa2RzUTFaV1ZuTldiRkpYVWxaYVJsTlZXazlTYkZaelYydHdVazFHV2xWVFZWcFBWRlphVlZKV1VrNVJhMVpJVVZSR1ZsSlZTak5rTUhSWFlsVmFlbGxyWkVkaE1rbDVaVWhDWVZKRlZrMVVWVVp5VWpCRmVGWlZWa05oUlRGRVZXeGFUbVF3YUc5Wk1EVk9ZV3hHTTFSc1VrcGxSVEZGWVROd1RsWkZSalpXTW1ocVZHc3hjVmt6WkU5V1JXdzBWRlZTY21Wck1WVlJXR3hZWVd0T1EyUlljRVphUlRGRFl6QmtRazFXVmtaUldHUXpWbFUxVlZkVVJrOWhiRlY1Vkd4U1lWVlZiRVpqUjNocVRURmFObE5WV2t0TlYwWlpZak5vU0ZKRlJsaFJiV1JQVm10S1FsWldVa1ZOUjNoR1ZWUkNWMVpGZUZWV1ZFcFBWa1pyZUZSdGNGWk5iRlpGVWxVNVRsRllaRWhSVkVaV1VsVjBibVF3V2xSaE1WcFZWbXhhVG1WRlVsVlJWWGhEV2pBMVYxRnJSbEpVVlVwSFUyeGFWRlp0T1RSVFNIQkNXa1ZLYmxSc1drTlJXRTVPVW0xMFUxVkdVbFpXVjJSU1RUQndjMWRyWkZka1YxSklZa2RvYVZFd1NrdFplazVQVFZad1dWTllhRWhTUlVaWVVXMWtUMVpyU2toU1ZURkZUVlp3UTFaclZsZFdSWGhYVWxoa1RsSkZSak5VVlZKQ1pERk9jVkpXY0U1UmJVNUlVVlJHVmxKVlRtNWtNVVpUVWxSc1QxVnNUa05VYkd4WlUyNUtZVmRHU2pOWmEyUkhZV3h3VlZKVmVFNVJWM1JJVVZSR1ZsSlZTbTlVVlU1VFZtc3hNMW95WkVwaFZURkNUVVZrUkZVelJraFZNR3hwVFRCU1VsSlZTa0pWVmxaQ1VWUlNTbEV3VWpOUldHUnVXakJzVEZGWE9VcFJNRVpTVWtad05FOVdXa2RXU0VaSldWWldRbFpzVWtaWFIyY3hZekIzTVZJelduWmpTRnBEVWpGTk1HRjZWbXRhTWxseVpFZEtRMUpzUWpGTk1qZzFTM2s0ZGxOdFRYWlRhMUpJVlhwYVUyRjZXa1JOTWxwcVRtdHpkbVZYYUcxVlJVcEVWVmhrUlZSWVpHeFphMk15VVZad1FtSldWWHBVUkdoWlRWZFdjMkpJV2tKU1Z6VmFVV3BLZDJReFRtaGtiV1JwWTBoR1RFMXJVbUZpTTJSVlpFZHNhVTB3YUZsVk1FWklTekZrUWxSNlNsRmpNRFF3V1Zac1ZWTklSakJVYTNNMFdqRndlV0pHVmtaT2JHYzFXa2N4Tm1GR1VsTlJWV3g0WXpCNE5VMVlXa1JSV0ZwMVZqQjRjRkZxVlhwa1UzTjJaRmMxU2sxck5ERlRWM0J0VmpGU1dXUlRkSFpOVlRrMFlqRlZjazlZV201bFNHY3hWbGRrTm1ReVdtRldWVFZwWVZWNGExZFZTbUZSTVUwelRsVndSMlJYT1VKV1JsWmFaV3RTVW1KdFkzWlZNMnQyVGxoTmRsRXpXWGRhYkZaNVUyeFNNVTFWYnpKWk0xVjVWVVJLTTB4NlZqVk5iVkpJVWpCM01FMXNRbEpTVjFadVdWaHNXbUpUT0RKWFZHaENXV3BTYjJONlJrWlZNV3R5VDFWNFJGTnRaRlJOYmtreVVWUk9XV0pHVGtsaVYzaFBZbTFPVm1ReFZqTldiVkpOVkd4Q01VMUhlREpPUlVaeVZGVjBkbEpJVFhka1NFSlBWMjB4YkUxdFRqSk9SMlEwWVRBMWRGWkdhelZqYWxZd1lVVmtUMVJJY0VOalZrWnFVekZPYjA1V1pGVkxNREZTVkVkV01GUnRiR3hqYTI4d1lWaENVbFZZWjNoVFNHaE9Ua1ZhVWxOWVFuSmlibU4zVTFodk5GcFVaRkphYVhSTlZESkdUVTVyV2tKWmJFWXlWMFJXUlZWRVJqSlJiV3g0VlZoS01sUkZlRWhsYVhOeVRVUlNSMW93YkZabFJFcGFWbXhXVFZwWFJqQk5SR1JQVFVkU2JHUlhhSEppZVhSU1ZHcGFNMU15WjNsTlJHZzJXbTF3ZDFGV1ZrdFVSRkpXVjBWT2JVMUdjR2hWTWpsVFVtMUdkMlZEZEZOVlJrWkRXbXBzVDJSSGNGUmxSV016Vm10MFRrc3pUbEZPYWxwd1kwWm9VVTVYYkRSa1JtaERZVE5HVDA5RlZsbFpibFpQVmxWa1dHRXlSa2hqTURGTlZtMXZORlpJUm5OYU0zQTJaR3BHZDFsWVVteGlSV3hJWlVac1NGVklWalpsYWsxNlkzazVORlF3T1c1VVJHUjZUakJuTTFNeWRFNWlSRUoyWkRKYVJHTldXa2RVUlZaWlV6RlNhazlGUmxCVFIyaHZUa1phZEU1cGRHcGtWWEJaVVc1b1UxVXlSa3BhYW1SV1YxWm9UR0pyYUc5WlZsRjNVM3BXY2swelduVlRTRzl5VERCU1NWa3hhRWxYUjI4eFZYcGtVRkp1VmtOV2EyOTVWMmwwVUUxWFJsUk5lbVJ2V1hsek0xcFljR3hhVjBaU1UxVlNRbFZWUmtOaWVsSktVV3hDVlZFd1RrSldSM1F6VWtWR1dsSkdXbE5OUmxKQ1ZWVm5kbEZyUmtwa01FWkZVVmRhUTFvd05WZFRSazVPVWxWa1JWRldaRzVSYkVaVVZYcEJlbEpYYTNaVFZtTjNXak5TTm1GV1NrTlRNMmhWVFVWVmVVOUhlRVJrYkZKRFl6QktibG96U2tOYU1GWkhVV3hHYWxGclJsSlZiV1JPVW1wU00xUnRaRnBUVlhRelYxVktRMVZXVmtsVVZVWk1VakIwZEdGRVFtdFRSVVV5VkVock5XUXlSWGxoTTFaaFVqSjRkVmxXYUZOaVIwcEpWVzV3VFdKV1dqWlVSRUpUVTJ4SmQySkdWbE5XV0doV1ZsUkNSMUpXV25KWk0yaE5ZbFUxTlZwRlVrSmhNRXB1V2pOS1Exb3dWa2RSYkVacVpEQkdXbGRXYkdoVFJrbDNXVEJTZG1ScmQzbFBWM0JxVFRCR01WZHJaSE5pYlVaWlZXMTRhVk5HU2paVVJ6RlhaV3N4UWs5RlpFUlZNMDVJVVZaR1ZsSnJTalpSVlVwRFZWWkdSRkZzUmtKa01HaFNWMVZTVjFWcVFuTlJhMHBhWkRCYVFsZFZiRXhrTVd4RFVXeEdWbE5GUmpOVFZXUkVVVE5PU0ZGV1JsWlNhMG96VkZWV1RsSklUa2hSVkVaV1drVm9NMVZVUWs1U1JXd3pWRlYwUW1SWE9VUmxWV1JNWWxkbmQxcEZhRUpPYTNnMVQxZHdhbUpZWkRGWk1HUXdZMFY0ZEZWdVFtRk5iWGQzVjJ4a05FMUhUalZPVjNocVpWUnNSbFpyV2s5UmJIQkpWMnRTVWxaWFRqUlVSekZQWlZkS1JWRlhVa05hTURWWFUwWkZNRkpWV201VlZsWkxVa2hhVkdSWVNrbFNSMW8wVkdsMFQyRlhPRFZVUmtKSFVXdHdjbFZyVGsxaWVtZ3pVa2RrV2xKR1dsTk5Sa0pDVlZWbmRsRnJSbEpTUlVadVdXdEdUbEZVUWtoUk1VNTRVakZPU2xscVRrVlZWVlpEVWtaR1ZsRlZSVEJUVlVwQ1ZWVkdZVkpFYkU1WFZscE1XbGM0TTFSRVRucGlhMlEyVkVaQ1IxUkVXbkJYU0U1b1dsVldUMVZWVWs1aU1VSnRVMWhDVDFOSFRYWk5NMHBOVWxaa1dWbHJUak5TZWs1RVVUTm9lbUpHVm5GVmJXdDNZMGN4U2xWclJqUlVNbmcyVERGU1VFMXVZekZOUm1zMVYwZDBTR0Z1VlRSa1NHZzFaRVZPZVZScVZYbGFNM0JTWWpCa05tUnJXa3RaV0VFd1VqQlJjbGw2Vms1T1Jsb3pWVmRaTVZveWFFVmxWa1pIVjFaU2NGVXhjRUpOVmxJMllWVnNUR0pxYUV4VmFsWkNXbTEwYTJWWFVrUlNWV3hSVldwRk5GUnFiRFZqUlZGNlpVWkZNVk5WTURWaFZUazBaRWR3UjFGWFZuVlZNbTk0VjBoa1dGUXlSVEJpU0doVlZWVlNkbEpFUm1oT2FUbElZMVpPYTFSRk1USlpNa1Y1VTJ4c01tTlVSbkJPV0VaVFpGTTViR05xVWpKVWJFWlpZVEExVkdWdGJHRlVTRVoyVmxWa2MyTkhUbTVqTWxaMFRWUlNkbFl6VGxSYVJGcG9XVzE0WVZwcmVFMWlSMUYyVlc1U1JtUllVa2hQVldoaFlWTjBhMUpYTlhWYU1qRkRWWHBPV0UxcE9WWk5SWGd4V20xYU1GVkVXa2hOYm1SR1dWWk9jR1F3VmxCYU1VWnVWVWRTUldSWGRHMU1NV3hXWWpOR2QyVkdXVEZoTTBaeFVtNUthazR3ZEV0a1JFSjRWbTF3TlU5SWJFMVVSazVvVWxOS1pHWlJMbVY1U25wa1YwbHBUMmxLYTJGWFVUWmhNbFkxVDI1d1JXSnRSbXhWTW1SeFZHcG9WV05WWkVoalYxWjVVbFV4VldKWWJIWmxWbFpFVW0xemVtUlZkRzVOTVdoSFZXMW9lazB3VGpSVE1rNUxVak5XVFZGcloybE1RMHAxV1cxWmFVOXFSVE5OUkZGM1QxUlpkMDFFUVhOSmJXeDZZM2xKTmtsdFVuQmFSSEJzWWtoT2NFOXNXa0pXUlZaVVRGWkZkMDFFUVhkTlJFRjNVMmxKYzBsdFZqUmpRMGsyVFZSamVrNVVXVFJQVkZVd1RVTjNhV0ZYUmpCSmFtOTRUbnBCTUUxRWF6Sk5SRUYzVEVOS01sbDVTVFpsZVVwQldUSTVkV1JIVmpSa1EwazJWM2xLYjJSSVVuZGplbTkyVEROa00yUjVOVE5OZVRWMlkyMWpkbUp1VFhaWk0wcHNXa2RXZFdSSGJHaGlTRTEyWkdwSmFVeERTbTlrU0ZKM1kzcHZka3d6WkROa2VUVnNaRzFzYTFwWE5XcGFWM2hzV2tka2JHTnBOV3hrVXpoNVRVUkplVXd5VG5sYVYxSnNZbTVTY0ZsWGVIcE1NakZvV1RKb2NHSnRWWFprYWtWcFdGTjNhV0ZYVVdsUGFVazBXWHBrYUU1cVNYaE5lVEF4VGtSU2EweFVVVEZOUjFGMFQwZFZlbHBETVdsT1JFWnRXVlJyZDAxRWEzaFBWR2RwVEVOS01HVllRbXhKYW5CaVNXeGFiR050YkcxaFYwWnBZa2RXUkdOdFZtdGFWelV3WVZkR2MwbHBkMmxVUlZaQ1ZXdE9lVnBYVW14aWJsSndXVmQ0VGxsWFRtOWhWelZzU1d3d2MwbHRiSHBqTTFac1kybEpObVY1U25CYVEwazJTVzFTY0ZwRWNHeGlTRTV3VDJ4YVFsWkZWbFJNVmtWM1RVUkJkMDFFUVhkVGFVbzVURU5LY0dNelRqRlpWelZxV2xWU2FHUkhWV2xQYVVsNVRVUkpNRXhVUVhoTVZFRjRWa1JCTkU5cVFYZFBha0YzVEdwQmQwMUVRWGROUkVGM1RVWnZhVXhEU2pKWlYzaHdXa1ZhZVdJeU1HbFBhVWw1VFVSSk1FeFVRWGhNVkVGNFZrUkJORTlxUVhkUGFrRjNUR3BCZDAxRVFYZE5SRUYzVFVadmFVeERTbXhsU0VKd1kyMUdNR0ZYT1hWU1IwWXdXbE5KTmtscVNYZE5hbEYwVFZSSmRFMTZSbFZOYWswMlRsUnJOazFFUVhWTlJFRjNUVVJCZDAxRVFYZFhhVWx6U1cxT2VWcFhVbXhpYmxKd1dWZDRWR1JYU25GYVYwNHdTV3B3TjBsdE1XaGliVkpvWkVkVmFVOXVjMmxoVjFGcFQybEpNMWx0V1RGT1YxRjVXbE13TVUxcVVUTk1WRkV6VFZSUmRFOVVSbXROVXpBMFdsUktiVTlIVG1sT2VrMTNXa1JGYVV4RFNuTmhWMXBzV0ROT2QxbFhOR2xQYm5OcFl6TlNhR051VWtWWldGSnNWa2RzZEZwVFNUWkpha2wzVFdwUmRFMUVSWFJOUkVaVlRVUm5OazFFUVRaTlJFRjFUVVJCZDAxRVFYZE5SRUYzVjJsSmMwbHRWblZhUlZKb1pFZFdWV0ZYTVd4SmFtOXBUV3BCZVU1RE1IaE5hVEI2VFZaUmVVMTZiekZQVkc5M1RVTTBkMDFFUVhkTlJFRjNUVVJDWVVsdU1ITkpiVEZvWW0xU2FHUkhWbXhKYW5BM1NXMXNhMGxxYjJsYVIyeHJUMjEwYkdWVWNEWlNSelZvV2xaT2JtRnJORFJXU0VaSVVqTkdiR05yVms1V1J6RTFZak5zVmxFd1duSk5NMVpNV25wT1dWSnNTbTlqZWs1RVpVVjBhbE5yWkRGVVJVcEpTV2wzYVdNeVZubGtiV3hxV2xVMWFHSlhWV2xQYVVwS1l6Tk9NVnBZU2tKVlJXdHBURU5LZWxwWVNqSmhWMDVzVmtoc2QxcFRTVFpKYTBaUlUxTkNWRnBZU2pKYVdFbHBURU5LTWxwWVNucGhWemwxU1dwdmFXUnFSWFZOUTBselNXMVNkbUpYUm5CaWFVazJTVzFvTUdSSVFucFBhVGgyWVZoT2VtUlhWbmxNYlZKMllsZFZkR0pYUm5saE1sWXdZMGQ0YUZreVZYVmlNMHB1U1dsM2FXRllRa0phUjFKNVdsaE9la2xxYjJsTlZFa3pUR3BCZFUxRE5IaEphWGRwV2tkV2Vsa3pTbkJqU0ZKd1lqSTBhVTlwU2tKVlJXdG5aRWM0WjJGWVRucGtWMVZuVm0xV2VXRlhXbkJaVjBweldsTkNSR050Vm10YVZ6VXdZVmRHYzJONVNYTkpiVTUyWW01U2FGa3pVV2xQYm5OcFdsY3hhR0ZYZDJsUGFVcHJZakl4YkdNelZuZGpSemw1WkVWQ2NHSnFTWFZhV0UxcFRFTktkMkZIT1hWYVUwazJTV2x6ZWs1RWF6VlBWR3MxVDFSck5VOVRTamxtVTNkcFlsZEdkVnBIUmpCaU0wbHBUMjV6YVZreU9YUmlWemwxVkcxR2RGcFRTVFpKYWxVeVRsUlpNVTVxVlRKVlEwSkxXbGhPTVdONVFsTmtWMncyU1dsM2FWa3lPVEZpYmxKNVpWTkpOa2xyVmxSSmFYZHBXbGN4YUdGWGVFSmFSMUo1V2xoT2VrbHFiMmxoYlZaNlpGaE5kV051Vm5CbGEwSndZbXBKZFZwWVRXbE1RMHAyWTIxa2FHSnRiRFpaV0ZKd1lqSTBhVTlwU2twVWFrbHpTVVZzZFZveVZuVmhWMVo1ZHpZeGFFbEhVbXhKUjNob1NVVnNkVnB0T1hsaVYwWnFZV05QZW1KcGQyZFZlVFZOVEdsSmMwbHRPWGxhTWtaMVlWaHdhR1JIYkhaaWEyeHJXbGMxTUdGWFduQmFXRWxwVDJsS1YxRldVa1pWZVRGU1RVUkJkMDFFUVhkTlJXOXBURU5LZWxwWVNuQlpWM2hQWkZjeGFWcFlTV2xQYVVwS1VrVk9SbFY1TURGT2FsVXlUbFJaTVU1c1FXbG1VM2RwWTBjNU0xcFlTV2xQYkhRM1NXMXNhMGxxYjJsTlYwVjVUbXBaTkU1cVZYUlBWMDVyV1ZNd01FMXRUVEJNVkdjMFRrZFpkRmx0VVhoUFIwVXpUMWRWTkZsdFdtdEphWGRwV2tjNWRGbFhiSFZKYW05cFVrVTVUbEpUU1hOSmJWb3hZbTFPTUdGWE9YVkphbTlwVkVjNWJtRlhOR2xNUTBwb1dUTlNjR0l5TkdsUGFVcDJZVmRTYWxneU1IbGlVMG81VEVoemFXRlhVV2xQYVVwb1RrZGFhMDVFU20xYVV6RnFXbGRTYkV4VVVteE9SR2QwVDBSR2FFMVRNREJQVkZreVdrZFZlVTFxUm1wT2FrRnBURU5LYTJJeU1XaGhWelJwVDJsS1JWUXdNVVpKYVhkcFdtNVdkVmt6VW5CaU1qUnBUMmxLUkZwWVNqQmhWMXB3V1RKR01HRlhPWFZKYVhkcFdWZE9NR0ZYT1hWSmFtOXBZMGM1ZW1SR09USmFXRXB3V20xc2FGbHRlR3hZTWs1c1kyNVNjRnB0YkdwWldGSndZakkwYVdaVGVEZEpiV3hyU1dwdmFVNHlXVEZOYWxFMFRtcGpkRTVIUm1sYVF6QXdXVlJyTVV4WFJUSlBSRkYwVFRKUmVWcEVUbWhQUjFWNVdXcEtha2xwZDJsYVJ6bDBXVmRzZFVscWIybFNSVGxPVWxOSmMwbHRXakZpYlU0d1lWYzVkVWxxYjJsVFdFNTZaRmRHZFZreVZXbE1RMHBvV1ROU2NHSXlOR2xQYVVwd1l6Tk9NVnBXT1RKWmVVbzVXRk4zYVdNeWJHNWliVlo1U1dwd04wbHRUblppVnpGMlltczFhR0pYVldsUGFVa3hUbXBWTWs1VVdURk9iRUZuVTIxV2VtUllUV2RWYmxad1pXbEpjMGx0VG5aa1Z6VXdZMjVyYVU5cFNrWlZlVWx6U1cxV2RGbFhiSE5SVjFKclkyMVdlbU41U1RaSmJYQnNZek5XZWt4dVNqRmhXSEJCWVZjMGVVeHRWbnBKYVhkcFlqTktibGxYTlhCbGJVWXdZVmM1ZFVscWIybFRWVFI1VEVOQ1NtSnRaR3hpYld4c1kzTlBkRmxUUW10YVUwSnpXVk5DU21KdFduWmpiVEZvV1RKdVJITXlOSE5KUmsxMVZFTTBhVXhEU25aamJXUm9ZbTFzTmxsWVVuQmlNalZLV2tkV2RXUkhiRzFoVjFaNVNXcHZhVlpyUmxWU1ZrMTBWVlJCZDAxRVFYZE5SRUpMU1dsM2FXTXlWbmxoVjBaelZHNVdkRmx0Vm5sSmFtOXBVMVZTUkZKV1RYUk9WRmt4VG1wVk1rNVVXbEZKYmpFNVpsZ3djMGx0Y0RCaFUwazJTV3BqTUU1VWEzcFBWMDEzVEZSU2ExbHRXWFJPUkZWNFdXa3hhRTFxUlhwTVZFMTVXa2RaTUZwcVFUTlpla2t5VGtOS09TNURNemR3WDFkTGNrczFURnAxTFRsRkxXdEdkV0pWWm01NlFXeHBZM2RoVDJFeFJXRjBaV3hHV1hkYVVERjFiV1ZRTldGWmJYVnhibW96YjFoaU1tOVRSbEp6VERnelNtaHpSVGhsYW5obVJ6VnBiR3RCWnlKZGZTd2laWGh3SWpveE56SXdNRE13TURBekxDSnBZWFFpT2pFM01UYzBNemd3TURNc0ltcDBhU0k2SWpReFlXTmhaR0V6TFRZM1lqUXRORGswWlMxaE5tVXpMV1V3T1RZMk5EUTVaakkxWkNKOS5FMmZfOTQtdUNPQ3doaXRVV0UtWTVmZ05yUHlDQThmclFtODlRUjE4eGczOC1lWHdBM2hRS0xfT0IteldUcGhvZEhFOHBvVF85S2FTRXUxLTlHU1pkZyIsImlzcyI6ImRpZDprZXk6ekRuYWVlWjY2SkJoazJoNnMxeXJBd2gyUVVIVXU4bzNucFAybWNiMmtXOFBZcWo5WSIsImV4cCI6MTc2MDA3OTEzNCwiaWF0IjoxNzI1OTUxMTM0LCJqdGkiOiI2YmVkZTU1Ny00NmQzLTRhNmEtODM3ZC0yMDgwZTRjMzgyMjIifQ.Cr8GcZFB1nR5IMDBx7vxiHqCikFrEuT0zb7FYyEvySmeTQcyqsojEcyA5RsvHd7LWs9k2WKcNiT9iHCHdtkrlw";

        //TODO Mirar respuesta OAuth2AccessTokenAuthenticationToken
        return oauth2VerifierWebClient.post()
                .uri(verifierProperties.paths().tokenPath())
                .header(CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED_FORM)
                .bodyValue(getM2MFormUrlEncodeBodyValue(clientId,clientAssertion))
                .retrieve()
                .bodyToMono(VerifierOauth2AccessToken.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Error fetching token", e)));
    }

    private String getM2MFormUrlEncodeBodyValue(String didKey, String clientAssertion) {
        Map<String, String> parameters = Map.of(
                OAuth2ParameterNames.GRANT_TYPE, CLIENT_CREDENTIALS_GRANT_TYPE_VALUE,
                OAuth2ParameterNames.CLIENT_ID, didKey,
                OAuth2ParameterNames.CLIENT_ASSERTION_TYPE, CLIENT_ASSERTION_TYPE_VALUE,
                OAuth2ParameterNames.CLIENT_ASSERTION, clientAssertion
        );

        return parameters.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    private Mono<String> getTokenFromCurrentSession() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    JwtAuthenticationToken token = (JwtAuthenticationToken) ctx.getAuthentication();
                    return token.getToken().getTokenValue();
                });
    }
}
