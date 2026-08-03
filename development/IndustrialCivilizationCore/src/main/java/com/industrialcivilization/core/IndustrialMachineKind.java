package com.industrialcivilization.core;

public enum IndustrialMachineKind {
    RESEARCH_STATION("research_station", 100000, 32, 600),
    EXPERIMENT_MODULE("orbital_experiment_module", 80000, 32, 600),
    ELECTRIC_FABRICATOR("electric_fabricator", 40000, 32, 160),
    PROGRAMMABLE_ASSEMBLER("programmable_assembler", 120000, 128, 240),
    ROBOTIC_CELL("robotic_manufacturing_cell", 400000, 512, 320),
    MATTER_REPLICATOR("matter_replicator", 8000000, 2048, 2000),
    FUSION_RESEARCH_CORE("fusion_research_core", 40000000, 8192, 4000),
    CARGO_CONTROLLER("interplanetary_cargo_controller", 4000000, 512, 600),
    MEGASTRUCTURE_CONTROLLER("orbital_megastructure_controller", 24000000, 8192, 2000),
    COLONY_BEACON("autonomous_colony_beacon", 8000000, 2048, 1200);

    public final String id;
    public final int capacity;
    public final int voltage;
    public final int duration;

    IndustrialMachineKind(String id, int capacity, int voltage, int duration) {
        this.id = id;
        this.capacity = capacity;
        this.voltage = voltage;
        this.duration = duration;
    }

    public int tier() {
        if (voltage <= 32) return 1;
        if (voltage <= 128) return 2;
        if (voltage <= 512) return 3;
        if (voltage <= 2048) return 4;
        return 5;
    }
}
