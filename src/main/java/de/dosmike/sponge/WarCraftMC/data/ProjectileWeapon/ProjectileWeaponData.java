package de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon;

import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public interface ProjectileWeaponData extends DataManipulator<ProjectileWeaponData, ImmutableProjectileWeaponData> {

    Value<ItemStackSnapshot> weapon();

}
