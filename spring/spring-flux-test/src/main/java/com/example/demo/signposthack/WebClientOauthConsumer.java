package com.example.demo.signposthack;

import oauth.signpost.OAuth;
import oauth.signpost.basic.UrlStringRequestAdapter;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpParameters;
import oauth.signpost.http.HttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

/* this class has been copied from signpost-core and modified to support spring's ClientRequest */
public class WebClientOauthConsumer {

    private static final long serialVersionUID = 1L;

    private String consumerKey, consumerSecret;

    private String token;

    private HmacSha1MessageSigner messageSigner;

    // these are params that may be passed to the consumer directly (i.e.
    // without going through the request object)
    private HttpParameters additionalParameters;

    // these are the params which will be passed to the message signer
    private HttpParameters requestParameters;

    private boolean sendEmptyTokens;

    final private Random random = new Random(System.nanoTime());

    public WebClientOauthConsumer(String consumerKey, String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        setMessageSigner(new HmacSha1MessageSigner());
    }

    public void setMessageSigner(HmacSha1MessageSigner messageSigner) {
        this.messageSigner = messageSigner;
        messageSigner.setConsumerSecret(consumerSecret);
    }


    public void setAdditionalParameters(HttpParameters additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    public synchronized ClientRequest sign(ClientRequest request) throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException {
        if (consumerKey == null) {
            throw new OAuthExpectationFailedException("consumer key not set");
        }
        if (consumerSecret == null) {
            throw new OAuthExpectationFailedException("consumer secret not set");
        }

        requestParameters = new HttpParameters();
        try {
            if (additionalParameters != null) {
                requestParameters.putAll(additionalParameters, false);
            }
            collectHeaderParameters(request, requestParameters);
            collectQueryParameters(request, requestParameters);
            collectBodyParameters(request, requestParameters);

            // add any OAuth params that haven't already been set
            completeOAuthParameters(requestParameters);

            requestParameters.remove(OAuth.OAUTH_SIGNATURE);

        } catch (IOException e) {
            throw new OAuthCommunicationException(e);
        }

        String signature = messageSigner.sign(request, requestParameters);
        OAuth.debugOut("signature", signature);

        String authHeader = writeSignature(signature, requestParameters);
        OAuth.debugOut("Request URL", request.url().toString());
        return ClientRequest.from(request).header(HttpHeaders.AUTHORIZATION, authHeader).build();
    }

    public String writeSignature(String signature, HttpParameters requestParameters) {
        StringBuilder sb = new StringBuilder();

        sb.append("OAuth ");

        // add the realm parameter, if any
        if (requestParameters.containsKey("realm")) {
            sb.append(requestParameters.getAsHeaderElement("realm"));
            sb.append(", ");
        }

        // add all (x_)oauth parameters
        HttpParameters oauthParams = requestParameters.getOAuthParameters();
        oauthParams.put(OAuth.OAUTH_SIGNATURE, signature, true);

        Iterator<String> iter = oauthParams.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            sb.append(oauthParams.getAsHeaderElement(key));
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }

        String header = sb.toString();
        OAuth.debugOut("Auth Header", header);

        return header;
    }

    public synchronized String sign(String url) throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException {
        HttpRequest request = new UrlStringRequestAdapter(url);
        // TODO: Implement
        // switch to URL signing
//        SigningStrategy oldStrategy = this.signingStrategy;
//        this.signingStrategy = new QueryStringSigningStrategy();
//
//        sign(request);
//
//        // revert to old strategy
//        this.signingStrategy = oldStrategy;

        return request.getRequestUrl();
    }

    public void setTokenWithSecret(String token, String tokenSecret) {
        this.token = token;
        messageSigner.setTokenSecret(tokenSecret);
    }

    public String getToken() {
        return token;
    }

    public String getTokenSecret() {
        return messageSigner.getTokenSecret();
    }

    public String getConsumerKey() {
        return this.consumerKey;
    }

    public String getConsumerSecret() {
        return this.consumerSecret;
    }

    /**
     * <p>
     * Helper method that adds any OAuth parameters to the given request
     * parameters which are missing from the current request but required for
     * signing. A good example is the oauth_nonce parameter, which is typically
     * not provided by the client in advance.
     * </p>
     * <p>
     * It's probably not a very good idea to override this method. If you want
     * to generate different nonces or timestamps, override
     * {@link #generateNonce()} or {@link #generateTimestamp()} instead.
     * </p>
     *
     * @param out
     *        the request parameter which should be completed
     */
    protected void completeOAuthParameters(HttpParameters out) {
        if (!out.containsKey(OAuth.OAUTH_CONSUMER_KEY)) {
            out.put(OAuth.OAUTH_CONSUMER_KEY, consumerKey, true);
        }
        if (!out.containsKey(OAuth.OAUTH_SIGNATURE_METHOD)) {
            out.put(OAuth.OAUTH_SIGNATURE_METHOD, messageSigner.getSignatureMethod(), true);
        }
        if (!out.containsKey(OAuth.OAUTH_TIMESTAMP)) {
            out.put(OAuth.OAUTH_TIMESTAMP, generateTimestamp(), true);
        }
        if (!out.containsKey(OAuth.OAUTH_NONCE)) {
            out.put(OAuth.OAUTH_NONCE, generateNonce(), true);
        }
        if (!out.containsKey(OAuth.OAUTH_VERSION)) {
            out.put(OAuth.OAUTH_VERSION, OAuth.VERSION_1_0, true);
        }
        if (!out.containsKey(OAuth.OAUTH_TOKEN)) {
            if (token != null && !token.equals("") || sendEmptyTokens) {
                out.put(OAuth.OAUTH_TOKEN, token, true);
            }
        }
    }

    public HttpParameters getRequestParameters() {
        return requestParameters;
    }

    public void setSendEmptyTokens(boolean enable) {
        this.sendEmptyTokens = enable;
    }

    /**
     * Collects OAuth Authorization header parameters as per OAuth Core 1.0 spec
     * section 9.1.1
     */
    protected void collectHeaderParameters(ClientRequest request, HttpParameters out) {
        HttpParameters headerParams = OAuth.oauthHeaderToParamsMap(request.headers().getFirst(OAuth.HTTP_AUTHORIZATION_HEADER));
        out.putAll(headerParams, false);
    }

    /**
     * Collects x-www-form-urlencoded body parameters as per OAuth Core 1.0 spec
     * section 9.1.1
     */
    protected void collectBodyParameters(ClientRequest request, HttpParameters out)
            throws IOException {

        // collect x-www-form-urlencoded body params
        String contentType = request.headers().getContentType().toString();
        if (contentType != null && contentType.startsWith(OAuth.FORM_ENCODED)) {
            // TODO: Implement
//            InputStream payload = request.getMessagePayload();
//            out.putAll(OAuth.decodeForm(payload), true);
        }
    }

    /**
     * Collects HTTP GET query string parameters as per OAuth Core 1.0 spec
     * section 9.1.1
     */
    protected void collectQueryParameters(ClientRequest request, HttpParameters out) {

        String url = request.url().toString();
        int q = url.indexOf('?');
        if (q >= 0) {
            // Combine the URL query string with the other parameters:
            out.putAll(OAuth.decodeForm(url.substring(q + 1)), true);
        }
    }

    protected String generateTimestamp() {
        return Long.toString(System.currentTimeMillis() / 1000L);
    }

    protected String generateNonce() {
        return Long.toString(random.nextLong());
    }
}
