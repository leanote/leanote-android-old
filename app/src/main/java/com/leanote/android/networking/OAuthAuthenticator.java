package com.leanote.android.networking;


import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.Blog;
import com.leanote.android.util.StringUtils;

public class OAuthAuthenticator implements Authenticator {
    @Override
    public void authenticate(final AuthenticatorRequest request) {
        String siteId = request.getSiteId();
        String token = AccountHelper.getDefaultAccount().getAccessToken();

        if (siteId != null) {
            // Get the token for a Jetpack site if needed
            //Blog blog = Leanote.leaDB.getBlogForDotComBlogId(siteId);
            Blog blog = null;
            if (blog != null) {
                String jetpackToken = blog.getApi_key();

                // valid OAuth tokens are 64 chars
                if (jetpackToken != null && jetpackToken.length() == 64 && !blog.isDotcomFlag()) {
                    token = jetpackToken;
                }
            }
        }

        request.sendWithAccessToken(StringUtils.notNullStr(token));
    }
}
