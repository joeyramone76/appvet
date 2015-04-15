package gov.nist.appvet.shared.appvetparameters;


/** Enumeration of possible incoming servlet parameters for both GET and POST
 * HTTP requests.
 */
public enum AppVetParameter {
	USERNAME ("username"),
	PASSWORD ("password"),
	SESSIONID ("sessionid"),
	COMMAND ("command"),
	APPID ("appid"),
	REPORT ("report"),
	TOOLID ("toolid"),
	TOOLRISK ("toolrisk"),
	APPNAME("appname"),
	APPVERSION("appversion"),
	APPOS("appos");

	public String value;
	
    private AppVetParameter(String value) {
        this.value = value;
    }

}
