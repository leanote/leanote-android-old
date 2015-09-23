package com.leanote.android.networking;

public class OAuthAuthenticatorFactoryDefault implements OAuthAuthenticatorFactoryAbstract {
    public OAuthAuthenticator make() {
        return new OAuthAuthenticator();
    }
}
