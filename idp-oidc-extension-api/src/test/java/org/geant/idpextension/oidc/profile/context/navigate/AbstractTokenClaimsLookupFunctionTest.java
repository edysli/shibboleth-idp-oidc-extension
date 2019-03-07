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

import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;

import java.net.URI;
import java.util.Date;

import org.geant.idpextension.oidc.messaging.context.OIDCAuthenticationResponseContext;
import org.geant.idpextension.oidc.token.support.AccessTokenClaimsSet;
import org.geant.idpextension.oidc.token.support.TokenClaimsSet;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;

import junit.framework.Assert;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AbstractTokenClaimsLookupFunctionTest {

    protected ProfileRequestContext prc;

    protected OIDCAuthenticationResponseContext oidcCtx;

    protected MockSubLookupFunction mock = new MockSubLookupFunction();

    @BeforeMethod
    protected void setUpCtxs() throws Exception {
        final RequestContext requestCtx = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(requestCtx);
        prc.setOutboundMessageContext(new MessageContext());
        oidcCtx = new OIDCAuthenticationResponseContext();
        prc.getOutboundMessageContext().addSubcontext(oidcCtx);
        oidcCtx.setTokenClaimsSet(
                new AccessTokenClaimsSet.Builder(new SecureRandomIdentifierGenerationStrategy(), new ClientID(),
                        "issuer", "userPrin", "subject", new Date(), new Date(System.currentTimeMillis() + 1000),
                        new Date(), new URI("http://example.com"), new Scope()).build());
    }

    @Test
    public void testSubjectSuccess() {
        Assert.assertEquals("subject", mock.apply(prc));
    }

    @Test
    public void testNoCtxts() {
        // No profile context
        Assert.assertNull(mock.apply(null));
        // No out bound message context
        prc.setOutboundMessageContext(null);
        Assert.assertNull(mock.apply(prc));
        // No response context
        prc.setOutboundMessageContext(new MessageContext());
        Assert.assertNull(mock.apply(prc));
        // No token claims set
        prc.getOutboundMessageContext().addSubcontext(new OIDCAuthenticationResponseContext());
        Assert.assertNull(mock.apply(prc));
    }

    class MockSubLookupFunction extends AbstractTokenClaimsLookupFunction {

        @Override
        Object doLookup(TokenClaimsSet tokenClaims) {
            return tokenClaims.getClaimsSet().getClaim("sub");
        }
    }

}