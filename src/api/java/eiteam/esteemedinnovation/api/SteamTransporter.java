package eiteam.esteemedinnovation.api;

import eiteam.esteemedinnovation.api.steamnet.SteamNetwork;
import eiteam.esteemedinnovation.api.util.Coord4;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Set;

public interface SteamTransporter {

    /**
     * The pressure of the device
     */
    float getPressure();

    /**
     * Sets the pressure of the device.
     * @param pressure
     */
    void setPressure(float pressure);

    /**
     * How resistant the device is to pressure meltdowns
     */
    float getPressureResistance();

    /**
     * How much steam the device can store
     */
    int getCapacity();

    int getSteamShare();

    /**
     * Explodes the transporter and removes it from the steamnet. This should handle any reductions in steamnet steam
     * or pressure. You do not have to actually make an explosion if you don't want to. This is called if
     * {@link #shouldExplode()} has been called. It must be called during the block/tile entity's update/tick.
     */
    void explode();

    /**
     * Queues up the block for an explosion.
     */
    void shouldExplode();

    /**
     * @param amount How much steam can be inserted per
     * @param face The side of the device
     */
    void insertSteam(int amount, Direction face);

    void decrSteam(int i);

    boolean doesConnect(Direction face);

    /**
     * Called to ensure that the device can have a steam gauge put on it to check how much steam it has
     *
     * @param face - The side of the device
     *
     * @return true if steam gauges can be put on it
     */
    boolean acceptsGauge(Direction face);

    Set<Direction> getConnectionSides();

    Level getLevelObj();

    void setNetworkName(String name);

    SteamNetwork getNetwork();

    void setNetwork(SteamNetwork steamNetwork);

    void refresh();

    Coord4 getCoords();

    ResourceLocation getDimension();

    int getSteam();

    void updateSteam(int steam);

    String getName();

    void wasAdded();

    /**
     * @return Whether steam can pass through this transporter. Returns false for closed valve pipes, for example.
     */
    default boolean canSteamPassThrough() {
        return true;
    }
}
