# macOS Rendering Profile

The test machine is an 8 GB, seven-GPU-core M1 MacBook Air. Minecraft 1.12.2 uses the legacy x86_64 Java/LWJGL 2 OpenGL path through Rosetta; the runtime log reports Apple M1, OpenGL 2.1 over Metal. The pack currently loads 162 JARs after the private-test ICBM addition, roughly 190 Forge mod IDs, and more than 42,000 texture sprites.

OptiFine is cross-platform, but it is intentionally not installed. It overlaps Nothirium, VintageFix, CensoredASM, Entity Culling, and the Forge model pipeline; the current CensoredASM configuration also explicitly disables an optimization when OptiFine is present. This stack already includes Nothirium, VintageFix, Entity Culling, Particle Culling, Phosphor, CensoredASM, Universal Tweaks, and RandomPatches.

The M1 profile uses:

- four-chunk render distance and a 60 FPS cap;
- Fast graphics, VBOs, no clouds, no ambient occlusion, no entity shadows, no mipmaps, and minimal particles;
- Better Foliage disabled;
- Entity Culling's CPU raytrace cache sized for four chunks, because Apple's available OpenGL path does not meet its OpenGL 4.4 GPU-culling requirement;
- Smooth Font performance mode with anisotropic filtering and font mipmaps disabled.

Test four chunks first. If it cannot sustain a consistent 60 FPS before machinery is built, capture an F3 screenshot while standing still and while turning, then profile client ticks/rendering before removing content. If it is stable, increase only render distance to six. Avoid raising the 60 FPS cap on the fanless MacBook Air.
