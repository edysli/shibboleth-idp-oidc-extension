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

package org.geant.idpextension.oidc.profile.impl;

import javax.annotation.Nonnull;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;

/**
 * Action that adds auth_time claim to a {@link IDTokenClaimsSet}.
 * 
 * auth_time Time when the End-User authentication occurred. Its value is a JSON
 * number representing the number of seconds from 1970-01-01T0:0:0Z as measured
 * in UTC until the date/time. When a max_age request is made or when auth_time
 * is requested as an Essential Claim, then this Claim is REQUIRED; otherwise,
 * its inclusion is OPTIONAL. (The auth_time Claim semantically corresponds to
 * the OpenID 2.0 PAPE [OpenID.PAPE] auth_time response parameter.)
 *
 *
 */
@SuppressWarnings("rawtypes")
public class AddAuthTimeToIDToken extends AbstractOIDCResponseAction {

    /** Class logger. */
    @Nonnull
    private Logger log = LoggerFactory.getLogger(AddAuthTimeToIDToken.class);

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        if (getOidcResponseContext().getIDToken() == null) {
            log.error("{} No id token", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_MSG_CTX);
            return;
        }
        if (getOidcResponseContext().getAuthTime() != null) {
            log.debug("{} Setting auth_time to id token", getLogPrefix());
            getOidcResponseContext().getIDToken().setClaim(IDTokenClaimsSet.AUTH_TIME_CLAIM_NAME,
                    getOidcResponseContext().getAuthTime().getTime() / 1000);
            log.debug("{} Updated token {}", getLogPrefix(), getOidcResponseContext().getIDToken().toJSONObject()
                    .toJSONString());
        }

    }

}