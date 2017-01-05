/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.oauth2.client.authentication.nimbus;


import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.AuthorizationCodeGrantAuthenticationToken;
import org.springframework.security.oauth2.client.config.ClientConfiguration;
import org.springframework.security.oauth2.core.*;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * @author Joe Grandja
 */
public class NimbusAuthorizationCodeGrantTokenExchanger implements AuthorizationGrantTokenExchanger<AuthorizationCodeGrantAuthenticationToken> {

	@Override
	public Tokens exchange(AuthorizationCodeGrantAuthenticationToken authorizationGrant) {

		ClientConfiguration configuration = authorizationGrant.getConfiguration();

		// Build the authorization code grant request for the token endpoint
		AuthorizationCode authorizationCode = new AuthorizationCode(authorizationGrant.getAuthorizationCode());
		URI redirectUri = toURI(configuration.getRedirectUri());
		AuthorizationGrant authorizationCodeGrant = new AuthorizationCodeGrant(authorizationCode, redirectUri);
		URI tokenUri = toURI(configuration.getTokenUri());

		// Set the credentials to authenticate the client at the token endpoint
		ClientID clientId = new ClientID(configuration.getClientId());
		Secret clientSecret = new Secret(configuration.getClientSecret());
		ClientAuthentication clientAuthentication = new ClientSecretBasic(clientId, clientSecret);

		TokenResponse tokenResponse;
		try {
			// Send the Access Token request
			TokenRequest tokenRequest = new TokenRequest(tokenUri, clientAuthentication, authorizationCodeGrant);
			HTTPRequest httpRequest = tokenRequest.toHTTPRequest();
			httpRequest.setAccept(MediaType.APPLICATION_JSON_VALUE);
			tokenResponse = TokenResponse.parse(httpRequest.send());
		} catch (ParseException pe) {
			// TODO Throw OAuth2-specific exception for downstream handling
			throw new OAuth2Exception(pe.getMessage(), pe);
		} catch (IOException ioe) {
			// TODO Throw OAuth2-specific exception for downstream handling
			throw new OAuth2Exception(ioe.getMessage(), ioe);
		}

		if (!tokenResponse.indicatesSuccess()) {
			// TODO Throw OAuth2-specific exception for downstream handling
			TokenErrorResponse tokenErrorResponse = (TokenErrorResponse) tokenResponse;
			throw new OAuth2Exception(tokenErrorResponse.getErrorObject().getDescription());
		}

		AccessTokenResponse accessTokenResponse = (AccessTokenResponse) tokenResponse;

		String accessTokenValue = accessTokenResponse.getTokens().getAccessToken().getValue();
		AccessTokenType accessTokenType = null;
		if (AccessTokenType.BEARER.value().equals(accessTokenResponse.getTokens().getAccessToken().getType().getValue())) {
			accessTokenType = AccessTokenType.BEARER;
		} else if (AccessTokenType.MAC.value().equals(accessTokenResponse.getTokens().getAccessToken().getType().getValue())) {
			accessTokenType = AccessTokenType.MAC;
		}
		long expiresIn = accessTokenResponse.getTokens().getAccessToken().getLifetime();
		List<String> scope = Collections.emptyList();
		if (!CollectionUtils.isEmpty(accessTokenResponse.getTokens().getAccessToken().getScope())) {
			scope = accessTokenResponse.getTokens().getAccessToken().getScope().toStringList();
		}
		AccessToken accessToken = new AccessToken(accessTokenType, accessTokenValue, expiresIn, scope);

		RefreshToken refreshToken = null;
		if (accessTokenResponse.getTokens().getRefreshToken() != null) {
			refreshToken = new RefreshToken(accessTokenResponse.getTokens().getRefreshToken().getValue());
		}

		return new Tokens(accessToken, refreshToken);
	}

	private URI toURI(String uriStr) {
		try {
			return new URI(uriStr);
		} catch (URISyntaxException ex) {
			throw new IllegalArgumentException(ex);
		}
	}
}