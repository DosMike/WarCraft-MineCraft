package de.dosmike.sponge.WarCraftMC.serializer;

import com.google.common.reflect.TypeToken;
import de.dosmike.sponge.WarCraftMC.races.Race;
import de.dosmike.sponge.WarCraftMC.races.Skill;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.LinkedList;
import java.util.List;

public class RaceSerializer implements TypeSerializer<Race> {
	@Override
	public void serialize(TypeToken<?> arg0, Race arg1, ConfigurationNode arg2) throws ObjectMappingException {
		throw new RuntimeException("Serializing Skills is not supported"); //I really don't want to do that
		//All i care about is that i can read the config with one line of code in the end... that's all
	}
	
	@SuppressWarnings("serial")
	TypeToken<List<Long>> ttla = new TypeToken<List<Long>>(){};
	@SuppressWarnings("serial")
	TypeToken<List<Skill>> ttls = new TypeToken<List<Skill>>(){};
	
	@Override
	public Race deserialize(TypeToken<?> arg0, ConfigurationNode arg1) throws ObjectMappingException {
		Race.Builder builder = Race.builder(arg1.getNode("id").getString("default_id"));
		builder.setName(arg1.getNode("name").getString("Unnamed Race"));
		builder.setDescription(arg1.getNode("description").getString("No Description"));
		builder.setRequiredLevel(arg1.getNode("requiredLevel").getInt(0));
		builder.setStartSkill(arg1.getNode("startSkill").getInt(0));
//		List<Long> list = new ArrayList<>();
//		list = arg1.getNode("levelXP").getValue(ttla, list);
//		builder.setLevelXP(list);
		builder.setLevelXP(arg1.getNode("levelXP").getString("100*level"));
		builder.setSkills(arg1.getNode("skills").getValue(ttls, new LinkedList<Skill>()));
		return builder.build();
	}
}
