package de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon;

import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public interface ImmutableProjectileWeaponData extends ImmutableDataManipulator<ImmutableProjectileWeaponData, ProjectileWeaponData> {

    ImmutableValue<ItemStackSnapshot> weapon();

}
