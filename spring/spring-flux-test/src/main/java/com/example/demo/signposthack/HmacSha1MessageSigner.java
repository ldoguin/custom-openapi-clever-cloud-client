package com.example.demo.signposthack;

import oauth.signpost.OAuth;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpParameters;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.reactive.function.client.ClientRequest;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Iterator;

/* this class has been copied from signpost-core and modified to support spring's ClientRequest */
public class HmacSha1MessageSigner implements Serializable {

    private static final long serialVersionUID = 4445779788786131202L;

    private transient Base64 base64;

    private String consumerSecret;

    private String tokenSecret;

    public HmacSha1MessageSigner() {
        this.base64 = new Base64();
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    protected String base64Encode(byte[] b) {
        return new String(base64.encode(b));
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.base64 = new Base64();
    }

    private static final String MAC_NAME = "HmacSHA1";

    public String getSignatureMethod() {
        return "HMAC-SHA1";
    }

    public String sign(ClientRequest request, HttpParameters requestParams)
            throws OAuthMessageSignerException {
        try {
            String keyString = OAuth.percentEncode(getConsumerSecret()) + '&'
                    + OAuth.percentEncode(getTokenSecret());
            byte[] keyBytes = keyString.getBytes(OAuth.ENCODING);

            SecretKey key = new SecretKeySpec(keyBytes, MAC_NAME);
            Mac mac = Mac.getInstance(MAC_NAME);
            mac.init(key);

            String sbs = generate(request, requestParams);
            OAuth.debugOut("SBS", sbs);
            byte[] text = sbs.getBytes(OAuth.ENCODING);

            return base64Encode(mac.doFinal(text)).trim();
        } catch (GeneralSecurityException e) {
            throw new OAuthMessageSignerException(e);
        } catch (UnsupportedEncodingException e) {
            throw new OAuthMessageSignerException(e);
        }
    }

    public String generate(ClientRequest request, HttpParameters requestParams) throws OAuthMessageSignerException {

        try {
            String normalizedUrl = normalizeRequestUrl(request);
            String normalizedParams = normalizeRequestParameters(requestParams);

            return request.method().toString() + '&' + OAuth.percentEncode(normalizedUrl) + '&'
                    + OAuth.percentEncode(normalizedParams);
        } catch (Exception e) {
            throw new OAuthMessageSignerException(e);
        }
    }

    public String normalizeRequestUrl(ClientRequest request) throws URISyntaxException {
        URI uri = new URI(request.url().toString());
        String scheme = uri.getScheme().toLowerCase();
        String authority = uri.getAuthority().toLowerCase();
        boolean dropPort = (scheme.equals("http") && uri.getPort() == 80)
                || (scheme.equals("https") && uri.getPort() == 443);
        if (dropPort) {
            // find the last : in the authority
            int index = authority.lastIndexOf(":");
            if (index >= 0) {
                authority = authority.substring(0, index);
            }
        }
        String path = uri.getRawPath();
        if (path == null || path.length() <= 0) {
            path = "/"; // conforms to RFC 2616 section 3.2.2
        }
        // we know that there is no query and no fragment here.
        return scheme + "://" + authority + path;
    }

    public String normalizeRequestParameters(HttpParameters requestParameters) throws IOException {
        if (requestParameters == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = requestParameters.keySet().iterator();

        for (int i = 0; iter.hasNext(); i++) {
            String param = iter.next();

            if (OAuth.OAUTH_SIGNATURE.equals(param) || "realm".equals(param)) {
                continue;
            }

            if (i > 0) {
                sb.append("&");
            }

            // fix contributed by Stjepan Rajko
            // since param should already be encoded, we supply false for percentEncode
            sb.append(requestParameters.getAsQueryString(param, false));
        }
        return sb.toString();
    }
}