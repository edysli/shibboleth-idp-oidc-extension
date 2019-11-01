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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.geant.idpextension.oidc.messaging.JSONSuccessResponse;
import org.geant.idpextension.oidc.profile.api.OIDCSecurityConfiguration;
import org.geant.idpextension.oidc.security.impl.CredentialConversionUtil;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.EncryptionConfiguration;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Function;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Action that forms outbound message containing keyset. Keys of the keyset are located from security configuration.
 * Formed message is set to {@link ProfileRequestContext#getOutboundMessageContext()}.
 */
@SuppressWarnings("rawtypes")
public class FormOutboundKeySetResponseMessage extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull
    private Logger log = LoggerFactory.getLogger(FormOutboundKeySetResponseMessage.class);

    /**
     * Strategy used to locate the {@link RelyingPartyContext} associated with a given {@link ProfileRequestContext}.
     */
    @Nonnull
    private Function<ProfileRequestContext, RelyingPartyContext> relyingPartyContextLookupStrategy;

    /** Security configuration we look for keys to publish. */
    @Nonnull
    OIDCSecurityConfiguration secConfiguration;

    /** Constructor. */
    public FormOutboundKeySetResponseMessage() {
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
    }

    /**
     * Set the strategy used to locate the {@link RelyingPartyContext} associated with a given
     * {@link ProfileRequestContext}.
     * 
     * @param strategy strategy used to locate the {@link RelyingPartyContext} associated with a given
     *            {@link ProfileRequestContext}
     */
    public void setRelyingPartyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, RelyingPartyContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        relyingPartyContextLookupStrategy =
                Constraint.isNotNull(strategy, "RelyingPartyContext lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }

        final RelyingPartyContext rpCtx = relyingPartyContextLookupStrategy.apply(profileRequestContext);
        if (rpCtx == null) {
            log.debug("{} No relying party context associated with this profile request", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_RELYING_PARTY_CTX);
            return false;
        }

        if (rpCtx.getProfileConfig() == null) {
            log.debug("{} No profile configuration associated with this profile request", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_RELYING_PARTY_CTX);
            return false;
        }

        if (!(rpCtx.getProfileConfig().getSecurityConfiguration() instanceof OIDCSecurityConfiguration)) {
            log.debug("{} No security configuration associated with the profile configuration of the profile request",
                    getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_RELYING_PARTY_CTX);
            return false;
        }
        secConfiguration = (OIDCSecurityConfiguration) rpCtx.getProfileConfig().getSecurityConfiguration();
        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        final List<JWK> publishList = new ArrayList<JWK>();
        final SignatureSigningConfiguration signingConfig = secConfiguration.getSignatureSigningConfiguration();
        if (signingConfig != null) {
            convertAndPublishToList(signingConfig.getSigningCredentials(), publishList);
        }
        final EncryptionConfiguration encryptionConfig = secConfiguration.getRequestObjectDecryptionConfiguration();
        if (encryptionConfig != null) {
            convertAndPublishToList(encryptionConfig.getKeyTransportEncryptionCredentials(), publishList);
        }
        final JWKSet keySet = new JWKSet(publishList);
        profileRequestContext.getOutboundMessageContext().setMessage(new JSONSuccessResponse(keySet.toJSONObject()));
    }
    
    /**
     * Converts the given credentials into JWK and adds all the successfully converted JWKs to the given list.
     * 
     * @param credentials The list of credentials to be converted to JWKs.
     * @param publishList The list where the successfully converted JWKs are put.
     */
    protected void convertAndPublishToList(final List<Credential> credentials, final List<JWK> publishList) {
        if (credentials != null) {
            for (final Credential credential : credentials) {
                final JWK jwk = CredentialConversionUtil.credentialToKey(credential);
                if (jwk != null) {
                    publishList.add(jwk);
                }
            }
        }
    }

}