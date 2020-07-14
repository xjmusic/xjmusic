package io.xj.service.nexus.work;

/**
 Medic Worker reads the entity store for Chains that require work, and makes sure each has a ChainWorker going
 */
@FunctionalInterface
public interface MedicWorker extends Runnable {
}

