# IC2 Machine Texture Style Reference

`IC2_MACHINE_TEXTURE_STYLE_REFERENCE.png` is the visual direction reference
for Astra's IC2 Classic texture overrides. The shipped resources are generated
deterministically by `development/tools/generate_astra_texture_overrides.py`;
the reference image is not loaded by Minecraft.

## Generation prompt

> Modernize the supplied IC2 Classic machine sprite atlas so it visually
> belongs beside the supplied Industrial Civilization first-party machine
> textures. Produce a coherent pixel-art reference sheet, not a photorealistic
> render. Use dark blue-gray housings, brushed-silver perimeter rails and
> corner fasteners, orange mechanical/process accents, and cyan
> electrical/status indicators. Preserve recognizable furnace vents,
> macerator rollers, compressor plates, extractor apertures, electrical ports,
> and active-state differences. Use strong tiny-scale silhouettes and
> restrained wear. Avoid text, logos, soft gradients, excessive bloom,
> isometric perspective, and changing machines into unrelated objects.

## Compatibility rule

Every generated IC2 sheet preserves the source atlas dimensions and exact
alpha occupancy. Machine markings and active-state layouts remain in their
original coordinates so the result reads as a texture pack rather than a new
set of machines. Inventory block renders receive the same coherent materials
and higher-contrast faces without introducing mismatched identities.
