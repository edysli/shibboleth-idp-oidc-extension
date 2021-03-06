/*
 * Copyright (c) 2017 - 2020, GÉANT
 *
 * Licensed under the Apache License, Version 2.0 (the “License”); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.geant.idpextension.oidc.config.logic;

import javax.annotation.Nullable;

import org.geant.idpextension.oidc.config.AbstractOIDCFlowAwareProfileConfiguration;
import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.logic.AbstractRelyingPartyPredicate;

/**
 * A predicate implementation that forwards to {@link AbstractOIDCProtocolConfiguration#getImplicitFlowEnabled()}.
 */
public class ImplicitFlowEnabledPredicate extends AbstractRelyingPartyPredicate {
    
    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean apply(@Nullable final ProfileRequestContext input) {
        final RelyingPartyContext rpc = getRelyingPartyContextLookupStrategy().apply(input);
        if (rpc != null) {
            final ProfileConfiguration pc = rpc.getProfileConfig();
            if (pc != null && pc instanceof AbstractOIDCFlowAwareProfileConfiguration) {
                return ((AbstractOIDCFlowAwareProfileConfiguration) pc).getImplicitFlowEnabled().apply(input);
            }
        }
        return false;
    }
}
