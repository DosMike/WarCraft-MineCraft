package de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.impl;

import de.dosmike.sponge.WarCraftMC.data.DataKeys;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.ImmutableProjectileWeaponData;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.ProjectileWeaponData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class ImmutableProjectileWeaponDataImpl extends AbstractImmutableData<ImmutableProjectileWeaponData, ProjectileWeaponData> implements ImmutableProjectileWeaponData {

    private final ItemStackSnapshot weapon;
    private final ImmutableValue<ItemStackSnapshot> weaponValue;

    public ImmutableProjectileWeaponDataImpl(ItemStackSnapshot weapon) {
        this.weapon = weapon;
        this.weaponValue = Sponge.getRegistry().getValueFactory()
                .createValue(DataKeys.PROJECTILE_WEAPON, weapon)
                .asImmutable();
        this.registerGetters();
    }
    public ImmutableProjectileWeaponDataImpl() {
        this(null);
    }

    @Override
    public ImmutableValue<ItemStackSnapshot> weapon() {
        return weaponValue;
    }

    public ItemStackSnapshot getWeapon() {
        return weapon;
    }

    @Override
    protected void registerGetters() {
        registerKeyValue(DataKeys.PROJECTILE_WEAPON, this::weapon);
        registerFieldGetter(DataKeys.PROJECTILE_WEAPON, this::getWeapon);
    }

    @Override
    public int getContentVersion() {
        return ProjectileWeaponBuilder.CONTENT_VERSION;
    }

    @Override
    public ProjectileWeaponData asMutable() {
        return new ProjectileWeaponDataImpl(weapon);
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        if (weapon != null)
            container.set(DataKeys.PROJECTILE_WEAPON, weapon);
        return container;
    }
}
