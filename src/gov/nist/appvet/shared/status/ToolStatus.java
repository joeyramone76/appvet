package gov.nist.appvet.shared.status;


/** $Id */
public enum ToolStatus {
	NA,
	SUBMITTED,
	// Tool had an error processing the app.
	ERROR,
	// Tool assessed app as high-risk .
	FAIL,
	// Tool assessed app as moderate-risk.
	WARNING,
	// Tool assessed app as low-risk.
	PASS;

	private ToolStatus() {
	}
	
	public synchronized static ToolStatus getStatus(String name) {
		if (name != null) {
			for (final ToolStatus s : ToolStatus.values()) {
				if (name.equalsIgnoreCase(s.name())) {
					return s;
				}
			}
		}
		return null;
	}




	

    



}
