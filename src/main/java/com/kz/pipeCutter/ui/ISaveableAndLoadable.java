package com.kz.pipeCutter.ui;

import java.io.IOException;

public interface ISaveableAndLoadable {
	public void save() throws IOException;

	public void load() throws IOException;

}
