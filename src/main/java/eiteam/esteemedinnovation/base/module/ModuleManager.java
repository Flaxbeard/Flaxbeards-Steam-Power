package eiteam.esteemedinnovation.base.module;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashMap;
import java.util.Map;

public class ModuleManager {
	Map<String, Module> modules;
	
	public ModuleManager() {
		modules = new HashMap<>();
	}
	
	public void registerModule(Module module) {
		modules.put(module.getName(), module);
	}
	
	public void setup(final FMLCommonSetupEvent event) {
		modules.forEach((name, module) -> module.setup(event));
	}
	
	public void setupClient(final FMLClientSetupEvent event) {
		modules.forEach((name, module) -> module.setupClient(event));
	}
}