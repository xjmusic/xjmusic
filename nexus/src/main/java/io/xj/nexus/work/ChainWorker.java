package io.xj.nexus.work;

/**
 ChainWorker reads the entity store for Segments that require work, and gets the work done
 */
@FunctionalInterface
public interface ChainWorker extends Runnable {
}

