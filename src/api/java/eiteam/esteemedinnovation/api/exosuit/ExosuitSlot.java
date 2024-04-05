package eiteam.esteemedinnovation.api.exosuit;

import net.minecraft.world.entity.EquipmentSlot;

public enum ExosuitSlot {
    BOOTS_FEET(EquipmentSlot.FEET, 3),
    BOOTS_TOP(EquipmentSlot.FEET, 2),
    BODY_FRONT(EquipmentSlot.CHEST, 2),
    BODY_HAND(EquipmentSlot.CHEST, 3),
    BODY_TANK(EquipmentSlot.CHEST, 4),
    HEAD_GOGGLES(EquipmentSlot.HEAD, 3),
    HEAD_HELM(EquipmentSlot.HEAD, 2),
    LEGS_HIPS(EquipmentSlot.LEGS, 2),
    LEGS_LEGS(EquipmentSlot.LEGS, 3),
    VANITY(EquipmentSlot.HEAD, 1);

    private final int slot;
    private final EquipmentSlot armor;

    ExosuitSlot(EquipmentSlot armor, int slot) {
        this.armor = armor;
        this.slot = slot;
    }

    public int getEngineeringSlot() {
        return slot;
    }

    public EquipmentSlot getArmorPiece() {
        return armor;
    }
}
