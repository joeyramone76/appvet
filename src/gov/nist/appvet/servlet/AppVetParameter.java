package gov.nist.appvet.servlet;

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
	APPNAME ("appname"),
	TOOLID ("toolid"),
	TOOLRISK ("toolrisk");

	String value;
	
    private AppVetParameter(String value) {
        this.value = value;
    }

}
