package de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.impl;

import de.dosmike.sponge.WarCraftMC.data.DataKeys;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.ImmutableProjectileWeaponData;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.ProjectileWeaponData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Optional;

public class ProjectileWeaponDataImpl extends AbstractData<ProjectileWeaponData, ImmutableProjectileWeaponData> implements ProjectileWeaponData {

    private ItemStackSnapshot weapon;

    public ProjectileWeaponDataImpl(ItemStackSnapshot weapon) {
        this.weapon = weapon;
        this.registerGettersAndSetters();
    }
    public ProjectileWeaponDataImpl() {
        this(null);
    }

    @Override
    public Value<ItemStackSnapshot> weapon() {
        return Sponge.getRegistry().getValueFactory().createValue(DataKeys.PROJECTILE_WEAPON, weapon, ItemStackSnapshot.NONE);
    }

    public ItemStackSnapshot getWeapon() {
        return weapon;
    }

    public void setWeapon(ItemStackSnapshot weapon) {
        this.weapon = weapon;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerKeyValue(DataKeys.PROJECTILE_WEAPON, this::weapon);
        registerFieldGetter(DataKeys.PROJECTILE_WEAPON, this::getWeapon);
        registerFieldSetter(DataKeys.PROJECTILE_WEAPON, this::setWeapon);
    }

    @Override
    public int getContentVersion() {
        return ProjectileWeaponBuilder.CONTENT_VERSION;
    }

    @Override
    public ImmutableProjectileWeaponData asImmutable() {
        return new ImmutableProjectileWeaponDataImpl(weapon);
    }

    @Override
    public Optional<ProjectileWeaponData> fill(DataHolder dataHolder, MergeFunction overlap) {
        ProjectileWeaponData merged = overlap.merge(this, dataHolder.get(ProjectileWeaponData.class).orElse(null));
        this.weapon = merged.weapon().get();
        return Optional.of(merged);
    }

    @Override
    public Optional<ProjectileWeaponData> from(DataContainer container) {
        if (!container.contains(DataKeys.PROJECTILE_WEAPON)) return Optional.empty();
        this.weapon = container.getSerializable(DataKeys.PROJECTILE_WEAPON.getQuery(), ItemStackSnapshot.class).get();
        return Optional.of(this);
    }

    @Override
    public ProjectileWeaponData copy() {
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
