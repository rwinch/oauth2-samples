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
package org.springframework.security.oauth2.core.protocol;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ResponseType;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Joe Grandja
 */
public class AuthorizationRequestAttributes implements Serializable {
	private final AuthorizationGrantType authorizationGrantType;
	private final ResponseType responseType;
	private final String clientId;
	private final String redirectUri;
	private final List<String> scope;
	private final String state;

	// TODO Remove after re-factor AuthorizationRequestUriBuilder to include authorizeUri as parameter
	private String authorizeUri;


	public AuthorizationRequestAttributes(AuthorizationGrantType authorizationGrantType, ResponseType responseType,
											 String clientId, String redirectUri, List<String> scope, String state) {

		Assert.notNull(authorizationGrantType, "authorizationGrantType cannot be null");
		Assert.isTrue(AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationGrantType) ||
				AuthorizationGrantType.IMPLICIT.equals(authorizationGrantType), "authorizationGrantType must be either 'authorization_code' or 'implicit'");
		this.authorizationGrantType = authorizationGrantType;

		Assert.notNull(responseType, "responseType cannot be null");
		if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationGrantType)) {
			Assert.isTrue(ResponseType.CODE.equals(responseType), "responseType must be 'code' for grant type 'authorization_code'");
		} else if (AuthorizationGrantType.IMPLICIT.equals(authorizationGrantType)) {
			Assert.isTrue(ResponseType.TOKEN.equals(responseType), "responseType must be 'token' for grant type 'implicit'");
		}
		this.responseType = responseType;

		Assert.notNull(clientId, "clientId cannot be null");
		this.clientId = clientId;

		this.redirectUri = redirectUri;
		this.scope = Collections.unmodifiableList((scope != null ? scope : Collections.emptyList()));
		this.state = state;
	}

	// TODO Remove after re-factor AuthorizationRequestUriBuilder to include authorizeUri as parameter
	public static AuthorizationRequestAttributes authorizationCodeGrant(String authorizeUri, String clientId, String redirectUri, List<String> scope, String state) {
		AuthorizationRequestAttributes authorizationRequest = new AuthorizationRequestAttributes(AuthorizationGrantType.AUTHORIZATION_CODE, ResponseType.CODE, clientId, redirectUri, scope, state);
		authorizationRequest.authorizeUri = authorizeUri;
		return authorizationRequest;
	}

	public static AuthorizationRequestAttributes authorizationCodeGrant(String clientId, String redirectUri, List<String> scope, String state) {
		return new AuthorizationRequestAttributes(AuthorizationGrantType.AUTHORIZATION_CODE, ResponseType.CODE, clientId, redirectUri, scope, state);
	}

	public static AuthorizationRequestAttributes implicitGrant(String clientId, String redirectUri, List<String> scope, String state) {
		return new AuthorizationRequestAttributes(AuthorizationGrantType.IMPLICIT, ResponseType.TOKEN, clientId, redirectUri, scope, state);
	}

	public final AuthorizationGrantType getGrantType() {
		return this.authorizationGrantType;
	}

	public final ResponseType getResponseType() {
		return this.responseType;
	}

	public final String getClientId() {
		return this.clientId;
	}

	public final String getRedirectUri() {
		return this.redirectUri;
	}

	public final List<String> getScope() {
		return this.scope;
	}

	public final String getState() {
		return this.state;
	}

	// TODO Remove after re-factor AuthorizationRequestUriBuilder to include authorizeUri as parameter
	public String getAuthorizeUri() {
		return authorizeUri;
	}

	public final boolean isAuthorizationCodeGrantType() {
		return AuthorizationGrantType.AUTHORIZATION_CODE.equals(this.getGrantType());
	}

	public final boolean isImplicitGrantType() {
		return AuthorizationGrantType.IMPLICIT.equals(this.getGrantType());
	}
}