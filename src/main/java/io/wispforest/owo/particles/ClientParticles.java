package io.wispforest.owo.particles;

import D;
import io.wispforest.owo.util.VectorRandomUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A wrapper for vanilla's terrible particle system that allows for easier
 * and more complex multi-particle operations
 */
@Environment(EnvType.CLIENT)
public final class ClientParticles {

    private static int particleCount = 1;
    private static boolean persist = false;

    private static Vec3 velocity = new Vec3(0, 0, 0);
    private static boolean randomizeVelocity = false;
    private static double randomVelocityScalar = 0;
    private static Direction.Axis randomizationAxis = null;

    private ClientParticles() {}

    /**
     * Marks the values set by {@link ClientParticles#setParticleCount(int)} and {@link ClientParticles#setVelocity(Vec3)} to be persistent
     */
    public static void persist() {
        ClientParticles.persist = true;
    }

    /**
     * How many particles to spawn per operation
     * <br><b>
     * Volatile unless {@link ClientParticles#persist()} is called before the next operation
     * </b>
     */
    public static void setParticleCount(int particleCount) {
        ClientParticles.particleCount = particleCount;
    }

    /**
     * The velocity added to each spawned particle
     * <br><b>
     * Volatile unless {@link ClientParticles#persist()} is called before the next operation
     * </b>
     */
    public static void setVelocity(Vec3 velocity) {
        ClientParticles.velocity = velocity;
    }

    /**
     * Makes the system use a random velocity for each particle
     * <br><b>
     * Volatile unless {@link ClientParticles#persist()} is called before the next operation
     * </b>
     *
     * @param scalar The scalar to use for the generated velocities which
     *               nominally range from -0.5 to 0.5 on each axis
     */
    public static void randomizeVelocity(double scalar) {
        randomizeVelocity = true;
        randomVelocityScalar = scalar;
        randomizationAxis = null;
    }

    /**
     * Makes the system use a random velocity for each particle
     * <br><b>
     * Volatile unless {@link ClientParticles#persist()} is called before the next operation
     * </b>
     *
     * @param scalar The scalar to use for the generated velocities which
     *               nominally range from -0.5 to 0.5 on each axis
     * @param axis   The axis on which to apply random velocity
     */
    public static void randomizeVelocityOnAxis(double scalar, Direction.Axis axis) {
        randomizeVelocity = true;
        randomVelocityScalar = scalar;
        randomizationAxis = axis;
    }

    /**
     * Forces a reset of velocity and particleCount
     */
    public static void reset() {
        persist = false;
        clearState();
    }

    private static void clearState() {
        if (persist) return;

        particleCount = 1;
        velocity = new Vec3(0, 0, 0);

        randomizeVelocity = false;
    }

    private static void addParticle(ParticleOptions particle, Level world, Vec3 location) {
        if (randomizeVelocity) {
            if (randomizationAxis == null) {
                velocity = VectorRandomUtils.getRandomOffset(world, Vec3.ZERO, randomVelocityScalar);
            } else {
                final var stopIt_getSomeHelp = (world.random.nextDouble() * 2 - 1) * randomVelocityScalar;
                velocity = switch (randomizationAxis) {
                    case X -> new Vec3(stopIt_getSomeHelp, 0, 0);
                    case Y -> new Vec3(0, stopIt_getSomeHelp, 0);
                    case Z -> new Vec3(0, 0, stopIt_getSomeHelp);
                };
            }
        }

        world.addParticle(particle, location.x, location.y, location.z, velocity.x, velocity.y, velocity.z);
    }

    /**
     * Spawns particles with a maximum offset of {@code deviation} from the center of {@code pos}
     *
     * @param particle  The particle to spawn
     * @param world     The world to spawn the particles in, must be {@link net.minecraft.client.multiplayer.ClientLevel}
     * @param pos       The block to center on
     * @param deviation The maximum deviation from the center of pos
     */
    public static void spawnCenteredOnBlock(ParticleOptions particle, Level world, BlockPos pos, double deviation) {
        Vec3 location;

        for (int i = 0; i < particleCount; i++) {
            location = VectorRandomUtils.getRandomCenteredOnBlock(world, pos, deviation);
            addParticle(particle, world, location);
        }

        clearState();
    }

    /**
     * Spawns particles randomly distributed within {@code pos}
     *
     * @param particle The particle to spawn
     * @param world    The world to spawn the particles in, must be {@link net.minecraft.client.multiplayer.ClientLevel}
     * @param pos      The block to spawn particles in
     */
    public static void spawnWithinBlock(ParticleOptions particle, Level world, BlockPos pos) {
        Vec3 location;

        for (int i = 0; i < particleCount; i++) {
            location = VectorRandomUtils.getRandomWithinBlock(world, pos);
            addParticle(particle, world, location);
        }

        clearState();
    }

    /**
     * Spawns particles with a maximum offset of {@code deviation} from {@code pos + offset}
     *
     * @param particle  The particle to spawn
     * @param world     The world to spawn the particles in, must be {@link net.minecraft.client.multiplayer.ClientLevel}
     * @param pos       The base position
     * @param offset    The offset from {@code pos}
     * @param deviation The scalar for random distribution
     */
    public static void spawnWithOffsetFromBlock(ParticleOptions particle, Level world, BlockPos pos, Vec3 offset, double deviation) {
        Vec3 location;
        offset = offset.add(Vec3.atLowerCornerOf(pos));

        for (int i = 0; i < particleCount; i++) {
            location = VectorRandomUtils.getRandomOffset(world, offset, deviation);

            addParticle(particle, world, location);
        }

        clearState();
    }

    /**
     * Spawns particles at the given location with a maximum offset of {@code deviation}
     *
     * @param particle  The particle to spawn
     * @param world     The world to spawn the particles in, must be {@link net.minecraft.client.multiplayer.ClientLevel}
     * @param pos       The base position
     * @param deviation The scalar from random distribution
     */
    public static void spawn(ParticleOptions particle, Level world, Vec3 pos, double deviation) {
        Vec3 location;

        for (int i = 0; i < particleCount; i++) {
            location = VectorRandomUtils.getRandomOffset(world, pos, deviation);
            addParticle(particle, world, location);
        }

        clearState();
    }

    /**
     * Spawns particles at the given location with a maximum offset of {@code deviation}
     *
     * @param particle   The particle to spawn
     * @param world      The world to spawn the particles in, must be {@link net.minecraft.client.multiplayer.ClientLevel}
     * @param pos        The base position
     * @param deviationX The scalar from random distribution on x
     * @param deviationY The scalar from random distribution on y
     * @param deviationZ The scalar from random distribution on z
     */
    public static void spawnPrecise(ParticleOptions particle, Level world, Vec3 pos, double deviationX, double deviationY, double deviationZ) {
        Vec3 location;

        for (int i = 0; i < particleCount; i++) {
            location = VectorRandomUtils.getRandomOffsetSpecific(world, pos, deviationX, deviationY, deviationZ);
            addParticle(particle, world, location);
        }

        clearState();
    }

    /**
     * Spawns enchant particles travelling from origin to destination
     *
     * @param world       The world to spawn the particles in, must be {@link net.minecraft.client.multiplayer.ClientLevel}
     * @param origin      The origin of the particle stream
     * @param destination The destination of the particle stream
     * @param deviation   The scalar for random distribution around {@code origin}
     */
    public static void spawnEnchantParticles(Level world, Vec3 origin, Vec3 destination, float deviation) {

        Vec3 location;
        Vec3 particleVector = origin.subtract(destination);

        for (int i = 0; i < particleCount; i++) {
            location = VectorRandomUtils.getRandomOffset(world, particleVector, deviation);
            world.addParticle(ParticleTypes.ENCHANT, destination.x, destination.y, destination.z, location.x, location.y, location.z);
        }

        clearState();
    }

    /**
     * Spawns a particle at the given location with a lifetime of {@code maxAge}
     *
     * @param particleType The type of the particle to spawn
     * @param pos          The position to spawn at
     * @param maxAge       The maxAge to set for the spawned particle
     */
    @SuppressWarnings("ConstantConditions")
    public static <T extends ParticleOptions> void spawnWithMaxAge(T particleType, Vec3 pos, int maxAge) {
        var particle = Minecraft.getInstance().particleEngine.createParticle(particleType, pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
        if (particle == null) {
            return;
        }

        particle.setLifetime(maxAge);
        clearState();
    }

    /**
     * Spawns a line of particles going from {@code start} to {@code end}
     *
     * @param particle  The particle to spawn
     * @param world     The world to spawn the particles in, must be {@link net.minecraft.client.multiplayer.ClientLevel}
     * @param start     The line's origin
     * @param end       The line's end point
     * @param deviation A random offset from the line that particles can have
     */
    public static void spawnLine(ParticleOptions particle, Level world, Vec3 start, Vec3 end, float deviation) {
        spawnLineInner(particle, world, start, end, deviation);
        clearState();
    }

    /**
     * Spawns a cube outline starting at {@code origin} and expanding by {@code size} in positive
     * direction on all axis
     *
     * @param particle  The particle to spawn
     * @param world     The world to spawn the particles in, must be {@link net.minecraft.client.multiplayer.ClientLevel}
     * @param origin    The cube's origin
     * @param size      The cube's side length
     * @param deviation A random offset from the line that particles can have
     */
    public static void spawnCubeOutline(ParticleOptions particle, Level world, Vec3 origin, float size, float deviation) {

        spawnLineInner(particle, world, origin, origin.add(size, 0, 0), deviation);
        spawnLineInner(particle, world, origin.add(size, 0, 0), origin.add(size, 0, size), deviation);

        spawnLineInner(particle, world, origin, origin.add(0, 0, size), deviation);
        spawnLineInner(particle, world, origin.add(0, 0, size), origin.add(size, 0, size), deviation);

        origin = origin.add(0, size, 0);

        spawnLineInner(particle, world, origin, origin.add(size, 0, 0), deviation);
        spawnLineInner(particle, world, origin.add(size, 0, 0), origin.add(size, 0, size), deviation);

        spawnLineInner(particle, world, origin, origin.add(0, 0, size), deviation);
        spawnLineInner(particle, world, origin.add(0, 0, size), origin.add(size, 0, size), deviation);

        spawnLineInner(particle, world, origin, origin.add(0, -size, 0), deviation);
        spawnLineInner(particle, world, origin.add(size, 0, 0), origin.add(size, -size, 0), deviation);
        spawnLineInner(particle, world, origin.add(0, 0, size), origin.add(0, -size, size), deviation);
        spawnLineInner(particle, world, origin.add(size, 0, size), origin.add(size, -size, size), deviation);

        clearState();
    }

    private static void spawnLineInner(ParticleOptions particle, Level world, Vec3 start, Vec3 end, float deviation) {
        Vec3 increment = end.subtract(start).scale(1f / (float) particleCount);

        for (int i = 0; i < particleCount; i++) {
            start = VectorRandomUtils.getRandomOffset(world, start, deviation);
            addParticle(particle, world, start);
            start = start.add(increment);
        }
    }

}
