package org.opentripplanner.ext.tnc.updater.lyft;

public class LyftAuthenticationRequestBody {

    public String grant_type;
    public String scope;

    public LyftAuthenticationRequestBody(String grant_type, String scope) {
        this.grant_type = grant_type;
        this.scope = scope;
    }
}
