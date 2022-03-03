package muon.app.ui.components.settings;

import muon.app.App;

public enum SettingsPageName {
	General(App.getValue("setting.tab.general"), 0),
	Terminal(App.getValue("setting.tab.terminal"), 1),
	Editor(App.getValue("setting.tab.editor"), 2),
	Display(App.getValue("setting.tab.display"), 3),
	Security(App.getValue("setting.tab.security"), 4);

	public final String name;
	public final int index;

	private SettingsPageName(String name, int index) {
		this.name = name;
		this.index = index;
	}
}
