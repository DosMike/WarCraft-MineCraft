package de.dosmike.sponge.WarCraftMC.Manager;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.wcSkill;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;
import de.dosmike.sponge.WarCraftMC.skills.ActiveSkills;
import de.dosmike.sponge.WarCraftMC.skills.SkillEffects;
import de.dosmike.sponge.WarCraftMC.skills.StatusSkills;

public class SkillManager {
	static Map<String, Method> autoRegistry = new HashMap<>();
	//Register default skills
	static { 
		registerSkills(SkillManager.class); //usually holds wip skills 
		registerSkills(ActiveSkills.class); 
		registerSkills(StatusSkills.class); 
		registerSkills(SkillEffects.class); 
		WarCraft.l("Loaded Skill handler: "+Arrays.toString(autoRegistry.keySet().toArray()));
	}
	public static void registerSkills(Class<?> skillProvider) { 
		for (Method m : skillProvider.getDeclaredMethods()) {
			if (m.isAnnotationPresent(wcSkill.class))
				if (!m.getReturnType().equals(SkillResult.class)) {
					throw new RuntimeException("Illegal Skill Action '"+m.getAnnotation(wcSkill.class).value().toLowerCase()+"' needs returntype SkillResult, has " + m.getReturnType().getName());
				} else {
					autoRegistry.put(m.getAnnotation(wcSkill.class).value().toLowerCase(), m);
				}
		}
	}
	public static Method getSkill(String name) {
		Method result = autoRegistry.get(name.toLowerCase());
		if (result == null) throw new RuntimeException("No skill handler for: "+name.toUpperCase());
		return result;
	}
	
}
