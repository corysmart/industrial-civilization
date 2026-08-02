<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Progression Graph

The full graph is `progression/progression-graph.json`; every individual edge is stored on its milestone. The condensed critical-gate graph is:

```mermaid
flowchart LR
    workshop["Secure Workshop"] --> electrical["Stable Electrical Workshop"]
    electrical --> automation["Automated Industry"]
    automation --> heavy["Heavy Industry"]
    heavy --> programmable["Programmable Manufacturing"]
    programmable --> nuclear["Monitored Nuclear Power"]
    nuclear --> orbit["Functional Orbital Station"]
    orbit --> orbitalArchive["Orbital Research Archive"]
    orbitalArchive --> moon["Moon Access"]
    moon --> lunarBase["Functional Lunar Base"]
    lunarBase --> lunarArchive["Lunar Engineering Archive"]
    lunarArchive --> quantum["Quantum Technology"]
    quantum --> authorization["Mars Mission Authorization"]
    authorization --> mars["Functional Martian Base"]
    mars --> autonomy["Martian Autonomy Archive"]
    autonomy --> lite["Lite Matter Engineering"]
    lite --> ai["AI Age"]
    ai --> ae2["Applied Energistics"]
    ae2 --> replication["UU-Matter and Replication"]
    replication --> continuous["Continuous Civilization"]
```

Static validation rejects missing references, duplicate IDs, cycles, unreachable milestones, and violated hard gates.
