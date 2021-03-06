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
package org.geant.idpextension.oidc.profile.impl;

import java.util.Date;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.action.EventIds;
import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.claims.ACR;

/** {@link AddAcrToIDToken} unit test. */
public class AddAcrToIDTokenTest extends BaseOIDCResponseActionTest {

    private AddAcrToIDToken action;

    private void init() throws ComponentInitializationException {
        action = new AddAcrToIDToken();
        action.initialize();
    }

    /**
     * Test that action copes with no id token in response context.
     * 
     * @throws ComponentInitializationException
     */
    @Test
    public void testNoCtx() throws ComponentInitializationException {
        init();
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_MSG_CTX);

    }
    
    /**
     * Test that action handles case of no acr.
     * 
     * @throws ComponentInitializationException
     * @throws ParseException
     */
    @Test
    public void testSuccess() throws ComponentInitializationException, ParseException {
        init();
        setIdTokenToResponseContext("iss", "sub", "aud", new Date(), new Date());
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNull(respCtx.getIDToken().getACR());
    }
    

    /**
     * Test that action is able to copy acr to idtoken from context.
     * 
     * @throws ComponentInitializationException
     * @throws ParseException
     */
    @Test
    public void testSuccess2() throws ComponentInitializationException, ParseException {
        init();
        setIdTokenToResponseContext("iss", "sub", "aud", new Date(), new Date());
        respCtx.setAcr("acrValue");
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(respCtx.getIDToken().getACR(), new ACR("acrValue"));
    }

}