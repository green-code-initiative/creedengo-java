package org.greencodeinitiative.creedengo.java.integration.tests.profile;

public class RuleMetadata {
	private String key;
	private String type;
	private String defaultSeverity;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefaultSeverity() {
		return defaultSeverity;
	}

	public void setDefaultSeverity(String defaultSeverity) {
		this.defaultSeverity = defaultSeverity;
	}

	@Override
	public String toString() {
		return "RuleMetadata{" +
				"key='" + key + '\'' +
				", type='" + type + '\'' +
				", defaultSeverity='" + defaultSeverity + '\'' +
				'}';
	}
}
