package project.temp.control;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;

public class Main {
	public static void main(String[] args) {
        PlatformConfiguration   config  = PlatformConfiguration.getDefaultNoGui();

        config.addComponent("project.temp.control.MaintainTempBDI.class");
        Starter.createPlatform(config).get();
    }
}
