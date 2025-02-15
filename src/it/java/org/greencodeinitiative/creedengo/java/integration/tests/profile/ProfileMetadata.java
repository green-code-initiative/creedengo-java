package org.greencodeinitiative.creedengo.java.integration.tests.profile;

import java.util.List;

public class ProfileMetadata {
	private String name;
	private String language;
	private List<String> ruleKeys;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public List<String> getRuleKeys() {
		return ruleKeys;
	}

	public void setRuleKeys(List<String> ruleKeys) {
		this.ruleKeys = ruleKeys;
	}

	@Override
	public String toString() {
		return "ProfileMetadata{" +
				"name='" + name + '\'' +
				", language='" + language + '\'' +
				", ruleKeys=" + ruleKeys +
				'}';
	}
}
