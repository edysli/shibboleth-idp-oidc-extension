/*
 * GÉANT BSD Software License
 *
 * Copyright (c) 2017 - 2020, GÉANT
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the GÉANT nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * Disclaimer:
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.geant.idpextension.oidc.profile.context.navigate;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.oauth2.sdk.TokenRequest;

/**
 * A function that returns redirect uri of the request via a lookup function.
 * This default lookup locates uri from oidc token request if available. If
 * information is not available, null is returned.
 */
public class TokenRequestRedirectURILookupFunction extends AbstractTokenRequestLookupFunction<URI> {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(TokenRequestRedirectURILookupFunction.class);

    @Override
    URI doLookup(TokenRequest req) {
        // TODO: constant to replace redirect_uri key
        if (req.getAuthorizationGrant().toParameters().get("redirect_uri") == null) {
            log.warn("No redirect_uri parameter");
            return null;
        }
        URI uri = null;
        try {
            uri = new URI(req.getAuthorizationGrant().toParameters().get("redirect_uri"));
        } catch (URISyntaxException e) {
            log.error("Unable to parse uri from token request redirect_uri {}",
                    req.getAuthorizationGrant().toParameters().get("redirect_uri"));
        }
        return uri;

    }
}