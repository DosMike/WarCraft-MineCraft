package de.dosmike.sponge.WarCraftMC.serializer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.reflect.TypeToken;

import de.dosmike.sponge.WarCraftMC.races.Skill;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public class SkillSerializer implements TypeSerializer<Skill> {
	@Override
	public void serialize(TypeToken<?> arg0, Skill arg1, ConfigurationNode arg2) throws ObjectMappingException {
		throw new RuntimeException("Serializing Skills is not supported"); //I really don't want to do that
		//All i care about is that i can read the config with one line of code in the end... that's all
	}
	
	@SuppressWarnings("serial")
	TypeToken<List<String>> ttsl = new TypeToken<List<String>>() {}; 
	@SuppressWarnings("serial")
	TypeToken<List<List<Double>>> ttdaa = new TypeToken<List<List<Double>>>() {};
	
	@Override
	public Skill deserialize(TypeToken<?> arg0, ConfigurationNode arg1) throws ObjectMappingException {
		Skill.Builder builder = Skill.builder(arg1.getNode("id").getString("default_id"));
		builder.setName(arg1.getNode("name").getString("Unnamed Skill"));
		builder.setDescription(arg1.getNode("desc").getString(""));
		builder.setSkillNeeded(arg1.getNode("skillNeeded").getInt(0));
		builder.setCooldown(arg1.getNode("cooldown").getDouble(0.0));
		
		//construct fallback value
		List<List<Double>> fallBack = new ArrayList<>();
		List<Double> fallBack2 = new ArrayList<>();
		fallBack2.add(1.0);
		fallBack.add(fallBack2);
		//read config value
		List<List<Double>> matrix = arg1.getNode("parameter").getValue(ttdaa, fallBack);
		//convert lists to array and write to skill
		builder.setParameterMap(convert(matrix)); //.getValue(ttdaa, new double[][]{ { 1.0 } }));
		List<String> d = arg1.getNode("effects").getValue(ttsl, new LinkedList<String>());
		builder.setActions(d.toArray(new String[d.size()]));
		return builder.build();
	}
	
	double[][] convert(List<List<Double>> matrix) {
		if (matrix.isEmpty()) return new double[][]{{1.0}};
		int rows = matrix.size();
		int columns = matrix.get(0).size();
		if (columns == 0) return new double[][]{{1.0}};
		double[][] result = new double[matrix.size()][matrix.get(0).size()];
		for (int row = 0; row < rows; row++) {
			if (matrix.get(row).size()!=columns) return new double[][]{{1.0}};
			for (int column = 0; column < columns; column++)
				result[row][column]=matrix.get(row).get(column);
		}
		return result;
	}
}
