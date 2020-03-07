package de.dosmike.sponge.WarCraftMC.data;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

public class DataKeys {

    public static Key<Value<ItemStackSnapshot>> PROJECTILE_WEAPON = DummyObjectProvider.createExtendedFor(Key.class, "PROJECTILE_WEAPON");

}
