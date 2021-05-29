package io.xj.nexus.work;

/**
 Boss Worker reads the entity store for Chains that require work, and makes sure each has a ChainWorker going
 */
@FunctionalInterface
public interface BossWorker extends Runnable {
}

