package de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.impl;

import de.dosmike.sponge.WarCraftMC.data.DataKeys;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.ImmutableProjectileWeaponData;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.ProjectileWeaponData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Optional;

public class ProjectileWeaponBuilder extends AbstractDataBuilder<ProjectileWeaponData> implements DataManipulatorBuilder<ProjectileWeaponData, ImmutableProjectileWeaponData> {

    public static final int CONTENT_VERSION = 1;

    public ProjectileWeaponBuilder() {
        super(ProjectileWeaponData.class, CONTENT_VERSION);
    }

    @Override
    public ProjectileWeaponData create() {
        return new ProjectileWeaponDataImpl();
    }

    @Override
    public Optional<ProjectileWeaponData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    @Override
    protected Optional<ProjectileWeaponData> buildContent(DataView container) throws InvalidDataException {
        if (!container.contains(DataKeys.PROJECTILE_WEAPON)) return Optional.empty();
        ProjectileWeaponData data = new ProjectileWeaponDataImpl();
        container.getSerializable(DataKeys.PROJECTILE_WEAPON.getQuery(), ItemStackSnapshot.class).ifPresent(item->{
            data.set(DataKeys.PROJECTILE_WEAPON, item);
        });
        return Optional.of(data);
    }



}
