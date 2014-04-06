package gov.nist.appvet.shared.status;

/** $$Id */
public enum AppStatus {
	REGISTERING,
	PENDING,
	PROCESSING,
	// App experienced an error or could not be processed by at least one tool.
	ERROR,
	// At least one tool assessed app as high-risk.
	FAIL,
	// At least one tool assessed app as moderate-risk but no tool assessed
	// as high-risk.
	WARNING,
	// All tools assessed app as low-risk.
	PASS;

	private AppStatus() {
	}
	
	public synchronized static AppStatus getStatus(String name) {
		if (name != null) {
			for (final AppStatus s : AppStatus.values()) {
				if (name.equalsIgnoreCase(s.name())) {
					return s;
				}
			}
		}
		return null;
	}
}
