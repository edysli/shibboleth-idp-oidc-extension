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

package org.geant.idpextension.oidc.profile.context.navigate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.claims.ACR;

import junit.framework.Assert;

public class DefaultRequestedAcrLookupFunctionTest extends BaseDefaultRequestLookupFunctionTest {

    private DefaultRequestedAcrLookupFunction lookup;

    @BeforeMethod
    protected void setUp() throws Exception {
        lookup = new DefaultRequestedAcrLookupFunction();
    }

    @Test
    public void testSuccessNoReqObject() {
        List<ACR> acrValues = new ArrayList<ACR>();
        acrValues.add(new ACR("1"));
        acrValues.add(new ACR("2"));
        AuthenticationRequest req =
                new AuthenticationRequest.Builder(new ResponseType("code"), new Scope("openid"), new ClientID("000123"),
                        URI.create("https://example.com/callback")).acrValues(acrValues).state(new State()).build();
        msgCtx.setMessage(req);
        Assert.assertTrue(lookup.apply(prc).contains(new ACR("1")));
        Assert.assertTrue(lookup.apply(prc).contains(new ACR("2")));
        Assert.assertEquals(2, lookup.apply(prc).size());
    }

    @Test
    public void testSuccessReqObject() {
        List<ACR> acrValues = new ArrayList<ACR>();
        acrValues.add(new ACR("1"));
        JWTClaimsSet ro = new JWTClaimsSet.Builder().claim("acr_values", "1 2").build();
        AuthenticationRequest req = new AuthenticationRequest.Builder(new ResponseType("code"), new Scope("openid"),
                new ClientID("000123"), URI.create("https://example.com/callback")).acrValues(acrValues)
                        .state(new State()).requestObject(new PlainJWT(ro)).build();
        msgCtx.setMessage(req);
        oidcCtx.setRequestObject(req.getRequestObject());
        Assert.assertTrue(lookup.apply(prc).contains(new ACR("1")));
        Assert.assertTrue(lookup.apply(prc).contains(new ACR("2")));
        Assert.assertEquals(2, lookup.apply(prc).size());
    }

}