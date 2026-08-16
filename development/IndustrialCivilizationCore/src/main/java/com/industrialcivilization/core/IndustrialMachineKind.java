package com.industrialcivilization.core;

public enum IndustrialMachineKind {
    RESEARCH_STATION("research_station", 100000, 32, 600, 0, WorkClass.ENERGY_LIMITED),
    EXPERIMENT_MODULE("orbital_experiment_module", 80000, 32, 600, 600, WorkClass.TIME_LIMITED),
    ELECTRIC_FABRICATOR("electric_fabricator", 40000, 32, 160, 0, WorkClass.ENERGY_LIMITED),
    PROGRAMMABLE_ASSEMBLER("programmable_assembler", 120000, 128, 240, 0, WorkClass.ENERGY_LIMITED),
    CAR_WORKSHOP("car_workshop", 300000, 128, 600, 0, WorkClass.ENERGY_LIMITED),
    GUN_FACTORY("gun_factory", 500000, 512, 800, 0, WorkClass.ENERGY_LIMITED),
    ROBOTIC_CELL("robotic_manufacturing_cell", 400000, 512, 320, 0, WorkClass.ENERGY_LIMITED),
    MATTER_REPLICATOR("matter_replicator", 8000000, 2048, 2000, 0, WorkClass.ENERGY_LIMITED),
    FUSION_RESEARCH_CORE("fusion_research_core", 40000000, 8192, 4000, 600, WorkClass.TIME_LIMITED),
    CARGO_CONTROLLER("interplanetary_cargo_controller", 4000000, 512, 600, 0, WorkClass.ENERGY_LIMITED),
    MEGASTRUCTURE_CONTROLLER("orbital_megastructure_controller", 24000000, 8192, 2000, 400, WorkClass.TIME_LIMITED),
    COLONY_BEACON("autonomous_colony_beacon", 8000000, 2048, 1200, 400, WorkClass.TIME_LIMITED);

    public enum WorkClass { ENERGY_LIMITED, TIME_LIMITED }

    public final String id;
    public final int capacity;
    public final int voltage;
    public final int duration;
    public final int minimumTicks;
    public final WorkClass workClass;

    IndustrialMachineKind(String id, int capacity, int voltage, int duration,
            int minimumTicks, WorkClass workClass) {
        this.id = id;
        this.capacity = capacity;
        this.voltage = voltage;
        this.duration = duration;
        this.minimumTicks = minimumTicks;
        this.workClass = workClass;
    }

    public long totalWorkEU() { return (long) voltage * duration; }

    public int tier() {
        if (voltage <= 32) return 1;
        if (voltage <= 128) return 2;
        if (voltage <= 512) return 3;
        if (voltage <= 2048) return 4;
        return 5;
    }
}
